package rpm.actors

import scala.collection.JavaConversions.asJavaCollection
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.seqAsJavaList
import akka.actor.Actor
import akka.actor.ActorLogging
import api.SourceFileDetail
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.ArtifactDetail
import pumpkinx.api.ArtifactScmDetail
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import rpm.config.Config
import scala.xml.XML
import scala.io.Source
import pumpkinx.api.ArtifactGroup
import pumpkinx.api.Package
import org.springframework.data.mongodb.core.query.Update

class RpmActor extends Actor with ActorLogging {

  def receive = {
    case configGroup: ArtifactGroup => {
      log.info("Processing RPM for group: " + configGroup)
      val coordinates = getCoordinates(configGroup)
      process(coordinates)
    }
  }

  def process(coordinates: Option[(String, String)]) = {
    if (coordinates.isEmpty == false) {
      var query = new Query
      query.addCriteria(Criteria.where("artifactId").is(coordinates.get._2))
      val allMavenArtifactDetails = Config.getMongoTemplate.find(query, classOf[ArtifactDetail]).filter(_.packages.filter(pkg => pkg.packageType.equals("rpm")).size == 0)
      // check if the version is already loaded.
      allMavenArtifactDetails.foreach(mavenArtifact => {
        val rpmInfo = getRpm(coordinates.get._1, mavenArtifact.version)
        val pkg = Package(rpmInfo._2, rpmInfo._1)
        val existingPackages = mavenArtifact.packages
        mavenArtifact.packages.add(pkg)
        
        // Replace the document
        query = new Query()
        query.addCriteria(Criteria.where("artifactId").is(mavenArtifact.artifactId).and("version").is(mavenArtifact.version))
        Config.getMongoTemplate.remove(query, classOf[ArtifactDetail])
        Config.getMongoTemplate.save(mavenArtifact)
        log.info(" ------------- SAVED RPM FOR ---------------------- "+mavenArtifact)
      })
    }
  }

  // Will return a tuple of (RPM artifact id, Maven artifact id).
  def getCoordinates(configGroup: ArtifactGroup) = {
    val rpmNamePattern = configGroup.members.filter(_.artifactFamily.equals("rpm")).map(_.artifactId)

    if (rpmNamePattern.isEmpty) {
      None
    } else {
      val mavenArtifactId = configGroup.members.filter(_.artifactFamily.equals("maven")).map(_.artifactId)
      Some(rpmNamePattern.get(0), mavenArtifactId.get(0))
    }
  }

  def isNotProcessed(rpmArtifactId: String, versionPattern: String) = {
    val query = new Query
    query.addCriteria(Criteria.where("artifactId").is(rpmArtifactId).and("version").regex(versionPattern).and("groupId").is(null))
    Config.getMongoTemplate.find(query, classOf[ArtifactDetail]).isEmpty()
  }

  def getRpm(artifactId: String, version: String) = {
    try {
      val xml = XML.loadString(Source.fromURL(String.format(Config.getRpmRepositoryRestUrlPattern, artifactId, version)).mkString)
      val nodes = (xml \\ "artifact")
      val commaSeparatedLocations = nodes.map(node => (node \ "resourceURI").text).filter(name => !name.contains("SNAPSHOT")).mkString(",")
      val commaSeparatedRpmNames = commaSeparatedLocations.split(",").map(token => token.substring(token.lastIndexOf("/") + 1)).mkString(",")
      (commaSeparatedLocations, commaSeparatedRpmNames)
    } catch {
      case ex: Throwable =>
        println(ex); println("RPM Not found for " + artifactId + "::" + version + ".0")
        ("", "")
    }
  }
}
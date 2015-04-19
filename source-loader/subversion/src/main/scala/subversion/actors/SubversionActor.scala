package subversion.actors

import java.io.ByteArrayOutputStream
import java.util.ArrayList
import java.util.Collection

import scala.collection.JavaConversions.asJavaCollection
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.seqAsJavaList
import scala.xml.XML

import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.tmatesoft.svn.core.SVNDirEntry
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.SVNNodeKind
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory

import akka.actor.Actor
import akka.actor.ActorLogging
import api.ChangeSet
import api.ChangeSetEntry
import api.SourceFileDetail
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.ArtifactDetail
import pumpkinx.api.ArtifactScmDetail
import subversion.config.Config

class SubversionActor extends Actor with ActorLogging {

  def receive = {
    case artifactScmDetail: ArtifactScmDetail => {
      if (changeSetToBeLoaded(artifactScmDetail)) {
        log.info("Processing SCM Artifact Detail for: " + artifactScmDetail)
        loadChangesets(artifactScmDetail)
      } else if (sourceToBeLoaded(artifactScmDetail)) {
        log.info("Loading the source file details for: " + artifactScmDetail)
        val allFiles = getAllFiles(artifactScmDetail.scmPath)

        // Construct the sourcefile detail.
        val sourceFileDetail = new SourceFileDetail
        sourceFileDetail.filePaths.addAll(allFiles)
        sourceFileDetail.groupId = artifactScmDetail.groupId
        sourceFileDetail.artifactId = artifactScmDetail.artifactId
        sourceFileDetail.version = artifactScmDetail.version
        sourceFileDetail.version = artifactScmDetail.version
        sourceFileDetail.versionSequence = artifactScmDetail.versionSequence
        Config.mongoTemplate.save(sourceFileDetail)
      } else {
        log.info("Already processed: " + artifactScmDetail)
      }
    }

    case artifactConfig: ArtifactConfig => {
      log.info("Processing SCM Version for: " + artifactConfig)
      process(artifactConfig)
    }
  }

  def getAllFiles(svnUrl: String) = {
    val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnUrl))
    repository.setAuthenticationManager(Config.getSvnAuthManager)
    list(repository, "", collection.mutable.Buffer[String]())
  }

  def list(repo: SVNRepository, path: String, accumulated: collection.mutable.Buffer[String]): collection.mutable.Buffer[String] = {
    val dirs = repo.getDir(path, -1, null, null.asInstanceOf[Collection[_]])

    dirs.foreach(dir => {
      val entry = dir.asInstanceOf[SVNDirEntry]
      if (entry.getKind() == SVNNodeKind.DIR) {
        list(repo, if (path.equals("")) entry.getName() else path + "/" + entry.getName(), accumulated)
      } else {
        accumulated.add(entry.getURL().toDecodedString())
      }
    })
    accumulated
  }

  def sourceToBeLoaded(artifactScmDetail: ArtifactScmDetail) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactScmDetail.groupId).and("artifactId").is(artifactScmDetail.artifactId).and("version").is(artifactScmDetail.version))
    isArtifactPresent(artifactScmDetail) && Config.getMongoTemplate.find(query, classOf[SourceFileDetail]).isEmpty == true
  }

  def loadChangesets(artifactScmDetail: ArtifactScmDetail) = {
    val prevArtifactVersionSequence = artifactScmDetail.versionSequence - 1
    if (prevArtifactVersionSequence != -1) {
      val prevArtifactScm = getArtifactScm(artifactScmDetail.groupId, artifactScmDetail.artifactId, prevArtifactVersionSequence)
      if (prevArtifactScm.isDefined) {
        svnLog(getArtifactConfig(artifactScmDetail), artifactScmDetail, prevArtifactScm.get)
      }
    }
  }

  def svnLog(artifactConfig: ArtifactConfig, currArtifactScmDetail: ArtifactScmDetail, prevArtifactScmDetail: ArtifactScmDetail) = {
    try {
      val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(Config.getRealSvnUrl(artifactConfig.scmLocation)))
      repository.setAuthenticationManager(Config.authManager)
      val entries = repository.log(Array(""), null, prevArtifactScmDetail.revision, currArtifactScmDetail.revision, true, true)
      val changeSets = entries.flatMap(entry => handleChangeSets(entry, repository, currArtifactScmDetail))
      changeSets.toList.foreach(changeSet => Config.getMongoTemplate.save(changeSet))
    } catch {
      case ex: Throwable => log.error(ex, " ==> " + currArtifactScmDetail)
    }
  }

  def handleChangeSets(chs: Any, repository: SVNRepository, currArtifactScmDetail: ArtifactScmDetail) = {
    log.info("Handling changeset for ===> "+currArtifactScmDetail.artifactId+" ("+currArtifactScmDetail.version+") ")
    val changeSets = new ArrayList[ChangeSet]
    val svnLog = chs.asInstanceOf[SVNLogEntry]
    val changeSet = ChangeSet()
    changeSet.revision = svnLog.getRevision()
    val userName = svnLog.getAuthor()
    changeSet.committer = userName
    changeSet.commitDate = svnLog.getDate()
    changeSet.commitMessage = svnLog.getMessage()
    changeSet.artifactScmDetail = currArtifactScmDetail

    val entries = svnLog.getChangedPaths().flatMap(entry => {
      val key = entry._1
      val svnLogEntry = entry._2
      val entries = new ArrayList[ChangeSetEntry]
      if (svnLogEntry.getPath().contains(repository.getRepositoryPath(""))) {
        val changeSetEntry = ChangeSetEntry()
        changeSetEntry.file = svnLogEntry.getPath()
        changeSetEntry.changeType = svnLogEntry.getType().toString
        entries.add(changeSetEntry)
      }
      entries
    })

    if (!entries.isEmpty) {
      changeSet.entries.addAll(entries)
      changeSets.add(changeSet)
    }
    changeSets
  }

  def getArtifactScm(groupId: String, artifactId: String, prevArtifactVersionSequence: Int) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(groupId).and("artifactId").is(artifactId).and("versionSequence").is(prevArtifactVersionSequence))
    val result = Config.getMongoTemplate.find(query, classOf[ArtifactScmDetail])
    if (result.isEmpty) None else Some(result.get(0))
  }

  def getArtifactConfig(artifactScmDetail: ArtifactScmDetail) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactScmDetail.groupId).and("artifactId").is(artifactScmDetail.artifactId))
    Config.getMongoTemplate.find(query, classOf[ArtifactConfig]).get(0)
  }

  def changeSetToBeLoaded(artifactScmDetail: ArtifactScmDetail) = {
    val query1 = new Query
    query1.addCriteria(Criteria.where("artifactScmDetail.groupId").is(artifactScmDetail.groupId).and("artifactScmDetail.artifactId").is(artifactScmDetail.artifactId).and("artifactScmDetail.version").is(artifactScmDetail.version))
    println("SAI "+artifactScmDetail.artifactId+" ("+artifactScmDetail.version+" ) "+(isArtifactPresent(artifactScmDetail) && Config.getMongoTemplate.find(query1, classOf[ChangeSet]).isEmpty() == true))
    isArtifactPresent(artifactScmDetail) && Config.getMongoTemplate.find(query1, classOf[ChangeSet]).isEmpty() == true
  }

  def isArtifactPresent(artifactScmDetail: ArtifactScmDetail) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactScmDetail.groupId).and("artifactId").is(artifactScmDetail.artifactId).and("version").is(artifactScmDetail.version))
    Config.getMongoTemplate.find(query, classOf[ArtifactDetail]).isEmpty() == false
  }

  def processSource(artifactScmDetail: ArtifactScmDetail) = {
    val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(artifactScmDetail.scmPath))
    repository.setAuthenticationManager(Config.getSvnAuthManager)
    val dirs = repository.getDir("", -1, null, null.asInstanceOf[Collection[_]])
    dirs.map(toSourceDetail)
  }

  def toSourceDetail(dir: Any) = {

  }

  def process(artifactConfig: ArtifactConfig) = {
    val tagUrl = Config.getRealSvnUrl(artifactConfig.scmLocation.replace("trunk", "tags"))
    val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(tagUrl))
    repository.setAuthenticationManager(Config.getSvnAuthManager)

    val dirs = repository.getDir("", -1, null, null.asInstanceOf[Collection[_]])

    dirs.foreach(dir => {
      try {
        val entry = dir.asInstanceOf[SVNDirEntry]
        if (isNotProcessed(artifactConfig, entry.getURL().toDecodedString) && artifactConfig.artifactFamily.equals("maven")) {
          val pomPath = entry.getPath() + "/pom.xml"
          val out = new ByteArrayOutputStream
          repository.getFile(pomPath, entry.getRevision(), null, out)
          val pom = new String(out.toByteArray())
          val revision = entry.getRevision
          val artifactVersion = getArtifactVersionFromPom(pom)
          val artifactScmDetail = new ArtifactScmDetail
          artifactScmDetail.groupId = artifactConfig.groupId
          artifactScmDetail.artifactId = artifactConfig.artifactId
          artifactScmDetail.version = artifactVersion
          artifactScmDetail.scmPath = entry.getURL().toDecodedString
          artifactScmDetail.specificationFileContent = pom
          artifactScmDetail.versionSequence = getVersionSequence(artifactConfig, artifactVersion).getOrElse(-1)
          artifactScmDetail.revision = revision
          if(artifactScmDetail.versionSequence != -1)
          Config.getMongoTemplate.save(artifactScmDetail)
        } else {
          log.info("Already processed: " + entry.getURL().toDecodedString())
        }
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
    })
  }

  def getArtifactVersionFromPom(pomContents: String) = {
    val xml = XML.loadString(pomContents)
    val versionNodes = xml \\ "version"
    versionNodes.get(0).text
  }

  def getVersionSequence(artifactConfig: ArtifactConfig, version: String) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactConfig.groupId).and("artifactId").is(artifactConfig.artifactId).and("version").is(version))
    val ad = Config.getMongoTemplate.find(query, classOf[ArtifactDetail])
    if (ad.isEmpty()) None else Some(ad.get(0).versionSequence)
  }

  def isNotProcessed(artifactConfig: ArtifactConfig, svnPath: String) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactConfig.groupId).and("artifactId").is(artifactConfig.artifactId).and("scmPath").is(svnPath))
    Config.getMongoTemplate.find(query, classOf[ArtifactScmDetail]).isEmpty()
  }

  def getFilteredArtifactDetail(artifactDetail: ArtifactDetail) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactDetail.groupId).and("artifactId").is(artifactDetail.artifactId))
    Config.getMongoTemplate.find(query, classOf[ArtifactDetail]).sortBy(_.version.replace(".", ""))
  }

  def getPreviousArtifactDetail(artifactDetail: ArtifactDetail) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactDetail.groupId).and("artifactId").is(artifactDetail.artifactId).and("versionSequence").is(artifactDetail.versionSequence - 1))
    val prev = Config.getMongoTemplate.find(query, classOf[ArtifactDetail]).sortBy(_.version.replace(".", ""))
    if (prev.isEmpty) None else Some(prev.get(0))
  }

  def getSvnUrl(artifactDetail: ArtifactDetail) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactDetail.groupId).and("artifactId").is(artifactDetail.artifactId))
    val url = Config.getMongoTemplate.find(query, classOf[ArtifactConfig]).get(0).getScmLocation
    Config.getRealSvnUrl(url)
  }

}
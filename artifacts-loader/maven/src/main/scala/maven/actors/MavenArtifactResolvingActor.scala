package maven.actors

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.seqAsJavaList
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.resolution.ArtifactDescriptorRequest
import org.eclipse.aether.version.Version
import org.springframework.data.mongodb.core.mapping.Document
import akka.actor.Actor
import akka.actor.ActorLogging
import maven.util.Booter
import maven.config.Config
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import pumpkinx.api.FailedArtifact
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.ArtifactDetail
import akka.actor.Props
import akka.routing.RoundRobinRouter
import pumpkinx.api.NotificationRequest


/**
 * @author Sai Kris
 */
class MavenArtifactResolvingActor extends Actor with ActorLogging {

  def receive = {
    case (artifactConfig: ArtifactConfig, version: Version, versionSequence: Int) => {

      if (isNotProcessed(artifactConfig, version) == true && isFailedArtifact(artifactConfig.groupId, artifactConfig.artifactId, version.toString) == false) {
        log.info(s"Resolving now: $artifactConfig, Version: $version ")
        val resolved = resolveDependencies(artifactConfig.groupId, artifactConfig.artifactId, version.toString(), artifactConfig.packaging, artifactConfig.classifier)
        val artifactDetail = resolved.map(transform)
        if (artifactDetail.isDefined) artifactDetail.get.versionSequence = versionSequence
        artifactDetail.map(artifactDetail => {
          Config.getMongoTemplate.save(artifactDetail)
          log.info("Triggering artifact release notification email ")
          Config.getMongoTemplate.save(new NotificationRequest(artifactDetail.groupId, artifactDetail.artifactId, artifactDetail.version, "NewArtifactRelease", null))
        })
      } else {
        log.info(s"Already processed $artifactConfig $version")
      }
    }

    case _ => log.error("I don't know to handle this message!")
  }

  def isNotProcessed(artifactConfig: ArtifactConfig, version: Version) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(artifactConfig.groupId).and("artifactId").is(artifactConfig.artifactId).and("version").is(version.toString))
    Config.getMongoTemplate.find(query, classOf[ArtifactDetail]).isEmpty()
  }

  def isFailedArtifact(groupId: String, artifactId: String, version: String) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(groupId).and("artifactId").is(artifactId).and("version").is(version))
    Config.getMongoTemplate.find(query, classOf[FailedArtifact]).isEmpty() == false
  }

  def transform(dependency: DependencyNode): ArtifactDetail = {
    val artifact = new ArtifactDetail
    artifact.groupId = dependency.getArtifact().getGroupId()
    artifact.artifactId = dependency.getArtifact().getArtifactId()
    artifact.version = dependency.getArtifact().getVersion()
    artifact.classifier = dependency.getArtifact().getClassifier()
    artifact.artifactType = dependency.getArtifact().getExtension()
    artifact.optional = if (dependency.getDependency() != null) dependency.getDependency().getOptional() else false
    artifact.children.addAll(dependency.getChildren().map(transform))
    artifact
  }

  def resolveDependencies(groupId: String, artifactId: String, version: String, extension: String, classifier: String) = {
    val mavenArtifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version)
    try {
      val collectRequest = new CollectRequest
      collectRequest.setRepositories(Booter.getRepositories.toList)

      val descriptorRequest = new ArtifactDescriptorRequest()
      descriptorRequest.setArtifact(mavenArtifact)

      descriptorRequest.setRepositories(Booter.getRepositories.toList);
      val descriptorResult = Booter.getRepositorySystem.readArtifactDescriptor(Booter.getRepositorySystemSession, descriptorRequest);

      collectRequest.setRootArtifact(descriptorResult.getArtifact());
      collectRequest.setDependencies(descriptorResult.getDependencies());
      collectRequest.setManagedDependencies(descriptorResult.getManagedDependencies());
      collectRequest.setRepositories(descriptorRequest.getRepositories());

      val collectResult = Booter.getRepositorySystem.collectDependencies(Booter.getRepositorySystemSession, collectRequest);

      Some(collectResult.getRoot())
    } catch {
      case ex: Throwable => {
        println(ex)
        val failedArtifact = new FailedArtifact
        failedArtifact.groupId = groupId
        failedArtifact.artifactId = artifactId
        failedArtifact.version = version
        Config.getMongoTemplate.save(failedArtifact)
        None
      }
    }
  }

}
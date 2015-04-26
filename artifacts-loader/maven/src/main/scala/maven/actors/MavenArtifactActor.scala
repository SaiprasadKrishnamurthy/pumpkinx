package maven.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import maven.config.Config
import maven.util.VersionUtils
import pumpkinx.api.ArtifactConfig
import java.util.UUID

/**
 * @author Sai Kris
 */
class MavenArtifactActor extends Actor with ActorLogging {

  def receive = {
    case a: ArtifactConfig if (a.artifactFamily.equals("maven")) => {
      val mavenArtifactResolvingActor = context.actorOf(Props.create(classOf[MavenArtifactResolvingActor]), "MavenArtifactResolvingActor"+UUID.randomUUID())
      log.info("Maven artifact actor triggered for " + a.groupId + " | " + a.artifactId + " | " + a.classifier)
      log.info(s"Getting all available versions for $a")
      val allartifactVersions = getAllVersions(a)
      log.debug(allartifactVersions.mkString("\n"))

      val startFromIndex = if (allartifactVersions.size - Config.getLimitVersions <= 0) 0 else allartifactVersions.size - Config.getLimitVersions
      allartifactVersions.indices.foreach(index => if (index >= startFromIndex) mavenArtifactResolvingActor ! (a, allartifactVersions(index), index))
    }
    case x: Any => log.warning(s"I can't handle $x")
  }

  def getAllVersions(artifactConfig: ArtifactConfig) = {
    VersionUtils.getAllAvailableVersions(artifactConfig.groupId, artifactConfig.artifactId)
  }
}
package maven.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.routing.RoundRobinRouter
import maven.config.Config
import scala.collection.JavaConversions._
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.StartArtifactLoad
import java.util.UUID

/**
 * @author Sai Kris
 */
class TriggerActor extends Actor with ActorLogging {

  def receive = {
    case StartArtifactLoad() => {
      val mavenArtifactActor = context.actorOf(Props.create(classOf[MavenArtifactActor]), "MavenArtifactActor_"+UUID.randomUUID())
      
      log.info("Triggered Maven Artifact Loader Actor ")
      val allArtifactConfig = Config.getMongoTemplate.findAll(classOf[ArtifactConfig])
      allArtifactConfig.filter(_.artifactFamily.equals("maven")).foreach(mavenArtifactActor ! _)
    }
  }

}
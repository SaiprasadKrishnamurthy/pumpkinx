package rpm.actors

import scala.collection.JavaConversions._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.routing.RoundRobinRouter
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.ArtifactDetail
import pumpkinx.api.ArtifactScmDetail
import pumpkinx.api.StartArtifactLoad
import rpm.config.Config
import pumpkinx.api.ArtifactGroup
import java.util.UUID

/**
 * @author Sai Kris
 */
class TriggerActor extends Actor with ActorLogging {

  def receive = {
    case StartArtifactLoad() => {
      val rpmActor = context.actorOf(Props.create(classOf[RpmActor]), "RpmActor"+UUID.randomUUID())
      log.info("Triggered Rpm Loader Actor ")
      val allRpmConfigs = Config.getMongoTemplate.findAll(classOf[ArtifactGroup])
      // Filter only the groups that contain rpm family configs.
      allRpmConfigs.filter(_.members.filter(member => member.artifactFamily.equals("rpm")).size > 0).foreach(rpmActor ! _)
    }
  }
}
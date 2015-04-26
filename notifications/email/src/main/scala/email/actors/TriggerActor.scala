package email.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.routing.RoundRobinRouter
import email.config.Config
import scala.collection.JavaConversions._
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.StartArtifactLoad
import pumpkinx.api.NotificationRequest
import java.util.UUID

/**
 * @author Sai Kris
 */
class TriggerActor extends Actor with ActorLogging {

  def receive = {
    case StartArtifactLoad() => {
      val emailActor = context.actorOf(Props.create(classOf[EmailActor]), "EmailActor"+UUID.randomUUID())
      
      log.info("Triggered Email Actor ")
      val allRequests = Config.getMongoTemplate.findAll(classOf[NotificationRequest]).filter(_.notifiedDateTime == null)
      allRequests.foreach(emailActor ! _)
    }
  }
}
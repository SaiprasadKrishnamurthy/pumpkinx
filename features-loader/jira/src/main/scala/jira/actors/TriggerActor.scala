package jira.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.routing.RoundRobinRouter
import scala.collection.JavaConversions._
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.StartArtifactLoad
import jira.config.Config
import java.util.UUID

/**
 * @author Sai Kris
 */
class TriggerActor extends Actor with ActorLogging {

  def receive = {
    case StartArtifactLoad() => {
      log.info("Triggered Jira Actor ")
      val jiraActor = context.actorOf(Props.create(classOf[JiraActor]).withRouter(new RoundRobinRouter(Config.getParallelLoads)), "JiraActor"+UUID.randomUUID())
      jiraActor ! "Start"
    }
  }

}
package base.trigger

import akka.actor.ActorSystem
import akka.actor.Props
import rpm.actors.TriggerActor
import subversion.actors.TriggerActor
import pumpkinx.api.StartArtifactLoad
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import akka.routing.RoundRobinRouter
import akka.routing.RoundRobinGroup
import scala.collection.JavaConversions._

object BaseTriggerActor {

  def main(args: Array[String]): Unit = {
    println(" ------------------- STARTING PUMPKINX ACTOR SYSTEM ------------------- ")
    val system = ActorSystem("pumpkinx-akka")

    import system.dispatcher

    def getRouter = {
      val allActors = List(
        system.actorOf(Props[maven.actors.TriggerActor]),
        system.actorOf(Props[subversion.actors.TriggerActor]),
        system.actorOf(Props[rpm.actors.TriggerActor]),
        system.actorOf(Props[jira.actors.TriggerActor]),
        system.actorOf(Props[email.actors.TriggerActor]))

      val roundRobinGroup = RoundRobinGroup(allActors.map(_.path.toStringWithoutAddress))
      system.actorOf(roundRobinGroup.props)
    }
    
    val roundRobinRouter = getRouter
    system.scheduler.schedule(0 seconds, 5 minutes)(roundRobinRouter ! new StartArtifactLoad)

    system.awaitTermination()
  }
}
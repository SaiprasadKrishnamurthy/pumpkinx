package scratchpad

import akka.actor.ActorSystem
import akka.actor.Props
import pumpkinx.api.StartArtifactLoad
import jira.actors.TriggerActor

object Trigger extends App {
  val system = ActorSystem("pumpkinx-akka")
  val mavenArtifactActor = system.actorOf(Props[TriggerActor], "triggerActor")
  mavenArtifactActor ! new StartArtifactLoad
  system.awaitTermination()
}
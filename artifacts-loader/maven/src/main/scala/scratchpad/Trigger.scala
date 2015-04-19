package scratchpad

import akka.actor.ActorSystem
import akka.actor.Props
import maven.actors.MavenArtifactActor
import maven.actors.TriggerActor
import pumpkinx.api.StartArtifactLoad

object Trigger extends App {
  val system = ActorSystem("pumpkinx-akka")
  val mavenArtifactActor = system.actorOf(Props[TriggerActor], "triggerActor")
  mavenArtifactActor ! new StartArtifactLoad
  system.awaitTermination()
}
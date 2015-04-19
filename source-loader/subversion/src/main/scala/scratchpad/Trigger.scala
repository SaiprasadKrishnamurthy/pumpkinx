package scratchpad

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import pumpkinx.api.StartArtifactLoad
import subversion.actors.TriggerActor

object Trigger extends App {
  val system = ActorSystem("pumpkinx-akka")
  val svnActor = system.actorOf(Props[TriggerActor], "triggerActor")
  svnActor ! new StartArtifactLoad
  system.awaitTermination
}
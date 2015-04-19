package trigger

import akka.actor.ActorSystem
import akka.actor.Props
import rpm.actors.TriggerActor
import pumpkinx.api.StartArtifactLoad

object RpmTrigger {

  def main(args: Array[String]) {
  val system = ActorSystem("pumpkinx-akka")
    val rpmArtifactActor = system.actorOf(Props[TriggerActor], "triggerActor")
    rpmArtifactActor ! new StartArtifactLoad
    system.awaitTermination()
  }

}
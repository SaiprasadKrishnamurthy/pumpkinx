package subversion.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.routing.RoundRobinRouter
import scala.collection.JavaConversions._
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.StartArtifactLoad
import subversion.config.Config
import pumpkinx.api.ArtifactDetail
import pumpkinx.api.ArtifactScmDetail

/**
 * @author Sai Kris
 */
class TriggerActor extends Actor with ActorLogging {

  def receive = {
    case StartArtifactLoad() => {
      val svnActor = context.actorOf(Props.create(classOf[SubversionActor]).withRouter(new RoundRobinRouter(Config.getParallelSourceLoads)), "SubversionActor")
      log.info("Triggered Subversion Loader Actor ")
      val allArtifactDetails = Config.getMongoTemplate.findAll(classOf[ArtifactScmDetail]).sortBy(_.versionSequence).reverse
      val allArtifactConfigs = Config.getMongoTemplate.findAll(classOf[ArtifactConfig])
      // .filter(_.artifactId.equals("watchlist-ws-presentation"))
      allArtifactDetails.foreach(svnActor ! _)
      allArtifactConfigs.foreach(svnActor ! _)
    }
  }
}
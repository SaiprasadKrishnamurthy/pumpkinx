package email.actors

import scala.collection.JavaConversions._
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.mail.javamail.MimeMessageHelper
import akka.actor.Actor
import akka.actor.ActorLogging
import email.config.Config
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.ArtifactDetail
import pumpkinx.api.NotificationRequest
import java.util.Date
import pumpkinx.api.NotificationRequest

/**
 * @author Sai Kris
 */
class EmailActor extends Actor with ActorLogging {

  def receive = {
    case notificationRequest: NotificationRequest if (notificationRequest.notificationType.equals("NewArtifactRelease")) => {
      sendArtifactReleaseEmail(notificationRequest)
      
      // Replace.
      val query = new Query()
      query.addCriteria(Criteria.where("groupId").is(notificationRequest.groupId).andOperator(Criteria.where("artifactId").is(notificationRequest.artifactId), Criteria.where("version").is(notificationRequest.version)))
      Config.getMongoTemplate.findAndRemove(query, classOf[NotificationRequest])
      notificationRequest.notifiedDateTime = new Date
      Config.getMongoTemplate.save(notificationRequest)
    }
  }

  def sendArtifactReleaseEmail(notificationRequest: NotificationRequest) = {
    val followers = getFollowers(notificationRequest)
    val artifact = getArtifact(notificationRequest)

    followers.foreach(follower => {
      val mimeMessageHelper = new MimeMessageHelper(Config.getMailSender.createMimeMessage());
      val subject = String.format(Config.getNewReleaseEmailSubject, artifact)
      mimeMessageHelper.setSubject(subject);
      var body = String.format(Config.getNewReleaseEmailTemplate(artifact), follower.fullName, artifact.toString)
      mimeMessageHelper.setText(body);
      mimeMessageHelper.setFrom(Config.getFromEmail);
      mimeMessageHelper.setReplyTo("Pumpkin");
      mimeMessageHelper.setTo(follower.email);
      val mimeMessageToReturn = mimeMessageHelper.getMimeMessage();
      Config.getMailSender.send(mimeMessageToReturn);
      log.info("Sent message successfully to .... " + follower.email);
    })
  }

  def getFollowers(notificationRequest: NotificationRequest) = {
    val query5 = new Query
    query5.addCriteria(Criteria.where("groupId").is(notificationRequest.groupId).andOperator(Criteria.where("artifactId").is(notificationRequest.artifactId)))
    val artifactConfig = Config.getMongoTemplate.find(query5, classOf[ArtifactConfig]).get(0)
    artifactConfig.followers
  }

  def getArtifact(notificationRequest: NotificationRequest) = {
    val query5 = new Query
    query5.addCriteria(Criteria.where("groupId").is(notificationRequest.groupId).andOperator(Criteria.where("artifactId").is(notificationRequest.artifactId), Criteria.where("version").is(notificationRequest.version)))
    Config.getMongoTemplate.find(query5, classOf[ArtifactDetail]).get(0)
  }
}
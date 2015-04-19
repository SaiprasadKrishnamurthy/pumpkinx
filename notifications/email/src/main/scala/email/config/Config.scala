package email.config

import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties
import pumpkinx.api.ArtifactDetail
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import com.mongodb.MongoClient

object Config {

  val springJavaMail = new JavaMailSenderImpl()
  val props = new Properties();
  props.setProperty("mail.smtp, connectiontimeout", "10");
  props.setProperty("mail.host", "mailhost");

  springJavaMail.setJavaMailProperties(props);
  springJavaMail.setHost("10.70.101.185")

  def getFromEmail = "saiprasad.krishnamurthy@igt.in"

  def getMailSender = springJavaMail

  def getArtifactUrl(artifactDetail: ArtifactDetail) = s"http://10.70.101.135:8888/pumpkinx/content/viewArtifactDetail.do?groupId=${artifactDetail.groupId}&artifactId=${artifactDetail.artifactId}&version=${artifactDetail.version}"

  def getNewReleaseEmailTemplate(artifact: ArtifactDetail) = s"""Hello %s, \n A new maven artifact %s is released. \n This artifact can be viewed at ${getArtifactUrl(artifact)}"""

  def getNewReleaseEmailSubject = "[PUMPKIN] Artifact Release Notification %s"

  val mongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")

  val mongoTemplate = new MongoTemplate(mongo);

  def getParallelLoads = 10
  
  def getMongoTemplate = {
    mongoTemplate
  }
}
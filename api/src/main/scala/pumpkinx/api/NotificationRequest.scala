package pumpkinx.api

import scala.beans.BeanProperty
import org.springframework.data.mongodb.core.mapping.Document
import java.util.Date

@Document(collection="notification_request")
class NotificationRequest(@BeanProperty var groupId: String, @BeanProperty var artifactId: String, @BeanProperty var version: String, @BeanProperty var notificationType: String = "NewArtifactRelease", @BeanProperty var notifiedDateTime: Date)
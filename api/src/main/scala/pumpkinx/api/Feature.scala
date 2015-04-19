package pumpkinx.api

import org.joda.time.DateTime
import scala.beans.BeanProperty
case class Feature(@BeanProperty featureId: String, @BeanProperty featureTitle: String, @BeanProperty featureCreatedDate: String, @BeanProperty featureCreatedBy: String, @BeanProperty featureStatus: String)
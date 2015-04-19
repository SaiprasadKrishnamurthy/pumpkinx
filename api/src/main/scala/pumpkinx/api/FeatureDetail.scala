package api

import scala.beans.BeanProperty
import org.springframework.data.mongodb.core.index._
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.eclipse.aether.artifact.Artifact
import org.springframework.data.annotation.Id
import scala.collection.JavaConversions._
import pumpkinx.api.Feature

@Document(collection = "feature_detail")
class FeatureDetail {

  @BeanProperty
  @Id
  var id: String = _

  @BeanProperty
  var groupId: String = _

  @BeanProperty
  var artifactId: String = _

  @BeanProperty
  var version: String = _

  @BeanProperty
  var versionSequence: Int = _
 
  @BeanProperty
  var features: java.util.ArrayList[Feature] = new java.util.ArrayList[Feature]

  override def toString = groupId + "|" + artifactId + "|" + version + "|"

}
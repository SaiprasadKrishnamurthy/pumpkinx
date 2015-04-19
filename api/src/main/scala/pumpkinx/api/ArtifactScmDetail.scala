package pumpkinx.api

import org.springframework.data.mongodb.core.mapping.Document
import scala.beans.BeanProperty

@Document(collection = "artifact_scm_detail")
class ArtifactScmDetail {

  @BeanProperty
  var groupId: String = _

  @BeanProperty
  var artifactId: String = _

  @BeanProperty
  var version: String = _

  @BeanProperty
  var scmPath: String = _

  @BeanProperty
  var specificationFileContent: String = _
  
  @BeanProperty
  var versionSequence: Int = _
  
  @BeanProperty
  var revision: Long = _
  
  override def toString = s"$groupId:$artifactId:$version ($scmPath)"

}
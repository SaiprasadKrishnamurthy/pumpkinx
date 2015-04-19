package pumpkinx.api

import java.util
import org.springframework.data.mongodb.core.mapping.Document
import scala.beans.BeanProperty
import java.util.ArrayList

@Document(collection = "artifact_detail")
class ArtifactDetail {

  @BeanProperty
  var groupId: String = _

  @BeanProperty
  var artifactId: String = _

  @BeanProperty
  var version: String = _

  @BeanProperty
  var packaging: String = _

  @BeanProperty
  var classifier: String = _

  @BeanProperty
  var artifactType: String = _

  @BeanProperty
  var artifactLocation: String = _

  @BeanProperty
  var children: java.util.List[ArtifactDetail] = new java.util.ArrayList

  @BeanProperty
  var scope: String = _

  @BeanProperty
  var optional: Boolean = _

  @BeanProperty
  var followers: java.util.ArrayList[User] = new util.ArrayList[User]()
  
  @BeanProperty
  var issues: java.util.ArrayList[Issue] = new util.ArrayList[Issue]()

  @BeanProperty
  var versionSequence: Int = _
  
  @BeanProperty
  var specificationFile: String = _
  
  @BeanProperty
  var ratingCount: Int = _

  @BeanProperty
  var rating: Int = _

   @BeanProperty
  var ratersCount: Int = _

  @BeanProperty
  var raters: java.util.List[User] = new ArrayList[User]()
  
  @BeanProperty
  var packages: java.util.ArrayList[Package] = new util.ArrayList[Package]()

  override def toString = artifactId + " (" + version + ") "

}

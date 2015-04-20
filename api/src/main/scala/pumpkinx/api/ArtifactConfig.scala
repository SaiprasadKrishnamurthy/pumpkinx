package pumpkinx.api

import java.util
import org.springframework.data.annotation.Id
import scala.beans.BeanProperty
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by Kumar on 30/03/2015.
 */
@Document(collection="artifact_config")
class ArtifactConfig {

  @Id
  var id: String = _

  @BeanProperty 
  var name: String = _ 

  @BeanProperty
  var summary: String = _

  @BeanProperty
  var owner: String = _

  @BeanProperty
  var groupId: String = _

  @BeanProperty
  var artifactId: String = _

  @BeanProperty
  var packaging: String = _

  @BeanProperty
  var classifier: String = _

  @BeanProperty
  var scmLocation: String = _

  // eg: svn
  @BeanProperty
  var scmType: String = _

  // eg: maven
  @BeanProperty
  var artifactFamily: String = _

  // eg: utility, webapp, etc.
  @BeanProperty
  var artifactCategory: String = _

  @BeanProperty
  var licence: String = _

  @BeanProperty
  var licenceUrl: String = _

  @BeanProperty
  var licenceText: String = _

  @BeanProperty
  var buildServerUrl: String = _

  // eg: jenkins
  @BeanProperty
  var buildServerType: String = _

  @BeanProperty
  var followers: java.util.ArrayList[User] = new util.ArrayList[User]()

  @BeanProperty
  var codeQualityMetricsUrl: String = _

  // eg: sonar
  @BeanProperty
  var codeQualityMetricsType: String = _
  
  override def toString = groupId + ":"+artifactId + s" ($artifactFamily)"
}


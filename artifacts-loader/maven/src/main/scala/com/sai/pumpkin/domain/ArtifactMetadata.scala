package com.sai.pumpkin.domain

import scala.beans.BeanProperty
import org.springframework.data.annotation.Id

class ArtifactMetadata(_groupId: String, _artifactId: String, _packaging: String) {

  @Id
  var id: String = _

  def this() = this("", "", "")
  @BeanProperty
  var groupId: String = _groupId

  @BeanProperty
  var artifactId: String = _artifactId

  @BeanProperty
  var version: String = _

  @BeanProperty
  var packaging: String = _packaging

  @BeanProperty
  var rpmName: String = _

  @BeanProperty
  var yumLocation: String = _

  @BeanProperty
  var svnLocation: String = _

  @BeanProperty
  var artifactCatgory: String = _

  @BeanProperty
  var goBackUpto: Int = _

  @BeanProperty
  var interfaceAnalysis: Boolean = _

  @BeanProperty
  var topLevelXmlSchema: String = _

  @BeanProperty
  var distributionKey: String = _
  
  @BeanProperty
  var klocworkJobName: String = _

  override def toString = groupId + "|" + artifactId + "|" + artifactCatgory + "|"
}
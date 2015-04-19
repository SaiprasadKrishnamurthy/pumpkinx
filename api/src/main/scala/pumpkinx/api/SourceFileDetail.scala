package api

import scala.beans.BeanProperty
import org.springframework.data.mongodb.core.index._
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.eclipse.aether.artifact.Artifact
import org.springframework.data.annotation.Id
import scala.collection.JavaConversions._

@Document(collection = "source_file_detail")
class SourceFileDetail {

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
  var svnPath: String = _

  @BeanProperty
  var svnRevision: String = _

  @BeanProperty
  var filePaths: java.util.ArrayList[String] = new java.util.ArrayList[String]

  override def toString = groupId + "|" + artifactId + "|" + version + "|" + svnPath + "|" + svnRevision + "|"

}
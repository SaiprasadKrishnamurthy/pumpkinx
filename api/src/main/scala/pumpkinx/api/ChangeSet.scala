package api

import java.util.HashSet
import java.util.Set
import scala.beans.BeanProperty
import java.util.LinkedHashSet
import java.util.Date
import org.springframework.data.mongodb.core.mapping.Document
import java.util.ArrayList
import org.springframework.data.annotation.Id
import pumpkinx.api.ArtifactScmDetail

@Document(collection = "changeset")
case class ChangeSet {
  
  @BeanProperty
  @Id
  var id: String = _

  @BeanProperty
  var committer: String = _

  @BeanProperty
  var revision: Long = _

  @BeanProperty
  var commitMessage: String = ""

  @BeanProperty
  var commitDate: Date = _

  @BeanProperty
  var entries: java.util.List[ChangeSetEntry] = new ArrayList[ChangeSetEntry]()
  
  @BeanProperty
  var artifactScmDetail: ArtifactScmDetail = _

  override def toString = s"Id: [$revision]: $commitMessage \n $entries"
}
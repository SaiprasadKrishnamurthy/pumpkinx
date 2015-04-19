package api

import java.util.HashSet
import java.util.Set
import scala.beans.BeanProperty
import org.springframework.data.mongodb.core.mapping.Document

case class ChangeSetEntry {

  @BeanProperty
  var file: String = ""

  @BeanProperty
  var changeType: String = ""

  override def toString = s"Id: [$changeType]: $file"
}
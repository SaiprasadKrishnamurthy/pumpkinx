package pumpkinx.api

import java.util.HashSet
import java.util.Set
import scala.beans.BeanProperty
import java.util.LinkedHashSet
import java.util.Date
import org.springframework.data.mongodb.core.mapping.Document
import java.util.ArrayList
import org.springframework.data.annotation.Id
import pumpkinx.api._

@Document(collection = "release_notes")
class ReleaseNotes extends Ordered[ReleaseNotes] {
  
  @BeanProperty
  var projectArtifact: ArtifactDetail = _
  
  @BeanProperty
  var date: Date = _
  
  @BeanProperty
  var notes: String = _
  
  @BeanProperty
  var components: java.util.List[ArtifactDetail] = new java.util.ArrayList[ArtifactDetail]
  
  @BeanProperty
  var releaseName: String = _
  
  @BeanProperty
  var releaseLead: String = _
  
  override def compare(that: ReleaseNotes) = {
    this.releaseName.trim.toUpperCase.compareTo(that.releaseName.trim.toUpperCase)
  }
}
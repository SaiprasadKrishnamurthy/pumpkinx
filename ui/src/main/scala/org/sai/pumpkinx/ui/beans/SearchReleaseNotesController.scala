package org.sai.pumpkinx.ui.beans

import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.sai.pumpkinx.ui.config.Config
import pumpkinx.api.ArtifactConfig
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import java.util.ArrayList
import pumpkinx.api.ArtifactDetail
import org.springframework.data.domain.Sort
import pumpkinx.api.ReleaseNotes

class SearchReleaseNotesController {
  val mongo = Config.getMongoTemplate

  @BeanProperty
  var searchedArtifact: String = _

  @BeanProperty
  var found: Boolean = _

  @BeanProperty
  var allReleaseNames: java.util.List[String] = _

  @BeanProperty
  var allReleaseNotes: java.util.List[ReleaseNotes] = new ArrayList

  val allArtifactConfigs = allConfigs

  @BeanProperty
  var searchedConfig: String = _

  def onQuery(query: String) = {
    new ArrayList(allArtifactConfigs.filter(a => a.releaseName.toLowerCase.contains(query.toLowerCase)).map(c => c.releaseName))
  }

  def allConfigs(): java.util.List[ReleaseNotes] = {
    mongo.findAll(classOf[ReleaseNotes])
  }

  def search() = {
    found = false
    allReleaseNotes.clear
    if (searchedConfig != null) {
      val query5 = new Query()
      query5.addCriteria(Criteria.where("releaseName").is(searchedConfig))
      val artifacts = mongo.find(query5, classOf[ReleaseNotes])
      allReleaseNotes.addAll(artifacts)
      found = true
    }
  }

} 
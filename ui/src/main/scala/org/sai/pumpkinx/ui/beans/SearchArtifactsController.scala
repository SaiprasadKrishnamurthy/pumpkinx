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
import org.sai.pumpkinx.ui.model.ArtifactDependeeCountTuple
import api.ChangeSet
import javax.faces.context.FacesContext
import javax.faces.application.FacesMessage
import api.ChangeSet

class SearchArtifactsController {
  val mongo = Config.getMongoTemplate

  @BeanProperty
  var searchedArtifact: String = _

  @BeanProperty
  var found: Boolean = _

  @BeanProperty
  var allArtifacts: java.util.List[String] = _

  @BeanProperty
  var allArtifactDetails: java.util.List[ArtifactDetail] = new ArrayList

  @BeanProperty
  var results: java.util.List[ArtifactDependeeCountTuple] = new ArrayList

  val allArtifactConfigs = allConfigs

  @BeanProperty
  var searchedConfig: String = _

  def onQuery(query: String) = {
    new ArrayList(allArtifactConfigs.filter(a => a.artifactId.toLowerCase.contains(query.toLowerCase)).map(c => c.artifactId + " (" + c.artifactFamily + " artifact) "))
  }

  def allConfigs(): java.util.List[ArtifactConfig] = {
    mongo.findAll(classOf[ArtifactConfig])
  }

  def search() = {
    found = false
    allArtifactDetails.clear
    results.clear
    if (searchedConfig != null) {
      val selectedArtifactid = searchedConfig.substring(0, searchedConfig.indexOf("(")).trim
      val query5 = new Query()
      query5.addCriteria(Criteria.where("artifactId").is(selectedArtifactid))
      query5.`with`(new Sort(Sort.Direction.DESC, "versionSequence"));
      val artifacts = mongo.find(query5, classOf[ArtifactDetail])
      allArtifactDetails.addAll(artifacts)

      artifacts.foreach(a => {
        val query8 = new Query()
        query8.addCriteria(Criteria.where("children.artifactId").is(a.artifactId).andOperator(Criteria.where("children.version").is(a.version), Criteria.where("children.groupId").is(a.groupId)))
        val dependees = mongo.find(query8, classOf[ArtifactDetail])
        results.add(ArtifactDependeeCountTuple(a, dependees.size))
      })

      found = true
    }
  }
} 
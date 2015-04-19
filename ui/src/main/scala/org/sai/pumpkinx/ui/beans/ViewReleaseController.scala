package org.sai.pumpkinx.ui.beans

import java.util.ArrayList
import scala.beans.BeanProperty
import org.sai.pumpkinx.ui.config.Config
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import pumpkinx.api.ArtifactDetail
import org.springframework.data.mongodb.core.query.Query
import org.apache.taglibs.standard.lang.jstl.AndOperator
import org.springframework.data.mongodb.core.query.Criteria
import pumpkinx.api.ReleaseNotes
import javax.servlet.http.HttpServletRequest
import javax.faces.context.FacesContext
import pumpkinx.api.User
import javax.servlet.http.HttpServletResponse
import org.primefaces.context.RequestContext
import javax.faces.application.FacesMessage
import org.sai.pumpkinx.ui.model.ReleasedArtifact
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.ArtifactScmDetail
import org.sai.pumpkinx.ui.model.ReleasedArtifact
import org.sai.pumpkinx.ui.model.ReleasedArtifact
import api.FeatureDetail

class ViewReleaseController {

  val mongo = Config.getMongoTemplate

  @BeanProperty
  var release: ReleaseNotes = _

  @BeanProperty
  var artifacts: java.util.List[ReleasedArtifact] = _

  val allArtifacts = mongo.findAll(classOf[ArtifactDetail])

  val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
  val httpResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse().asInstanceOf[HttpServletResponse]
  val releaseName = httpRequest.getParameter("releaseName")

  if (releaseName == null) {
    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "'releaseName' parameter is mandatory!")
  }
  var query5 = new Query()
  query5.addCriteria(Criteria.where("releaseName").is(releaseName))
  val _release = mongo.find(query5, classOf[ReleaseNotes])

  if (_release.isEmpty) {
    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "No release found for '" + releaseName + "'")
  } else {

    artifacts = new ArrayList
    release = _release(0)

    // Filter the aggregators first.
    val aggregators = release.components.filter(isAggregator)

    println("aggregators: " + aggregators)
    val childrenOfAggregators = aggregators.flatMap(agg => agg.children)

    println("Children of aggregators: " + childrenOfAggregators)

    artifacts.addAll(childrenOfAggregators.map(a => {
      val scm = getArtifactScmDetail(a)
      val feature = getFeatureDetail(a)
      val fresh = getFresh(a)
      if (scm.isEmpty == false && feature.isEmpty == false) {
        ReleasedArtifact(getFresh(a), scm.get, feature.get)
      } else {
        ReleasedArtifact(getFresh(a), new ArtifactScmDetail, new FeatureDetail)
      }
    }))

    // Add the direct ones now.
    artifacts.addAll(release.components.map(a => {
      val scm = getArtifactScmDetail(a)
      if (scm.isEmpty == false) {
        val feature = getFeatureDetail(a)
        ReleasedArtifact(getFresh(a), scm.get, feature.get)
      } else {
        ReleasedArtifact(getFresh(a), new ArtifactScmDetail, new FeatureDetail)
      }
    }))
  }

  def getFresh(art: ArtifactDetail) = {
    val query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(art.groupId).andOperator(Criteria.where("artifactId").is(art.artifactId), Criteria.where("version").is(art.version)))
    val a = mongo.find(query5, classOf[ArtifactDetail])
    if(a.isEmpty) art else a(0)
  }
  
  def isAggregator(art: ArtifactDetail) = {
    val query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(art.groupId).andOperator(Criteria.where("artifactId").is(art.artifactId)))
    val artifactConfig = mongo.find(query5, classOf[ArtifactConfig])
    if (artifactConfig.isEmpty == false) {
      artifactConfig(0).artifactCategory.equals(Config.getAggregatorArtifactCategory)
    } else {
      false
    }
  }

  def getArtifactScmDetail(art: ArtifactDetail) = {
    val query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(art.groupId).andOperator(Criteria.where("artifactId").is(art.artifactId), Criteria.where("version").is(art.version)))
    val _artifactScmDetail = mongo.find(query5, classOf[ArtifactScmDetail])
    if (_artifactScmDetail.isEmpty) None else Some(_artifactScmDetail(0))
  }

  def getFeatureDetail(art: ArtifactDetail) = {
    val query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(art.groupId).andOperator(Criteria.where("artifactId").is(art.artifactId), Criteria.where("version").is(art.version)))
    val _artifactScmDetail = mongo.find(query5, classOf[FeatureDetail])
    if (_artifactScmDetail.isEmpty) None else Some(_artifactScmDetail(0))
  }
}

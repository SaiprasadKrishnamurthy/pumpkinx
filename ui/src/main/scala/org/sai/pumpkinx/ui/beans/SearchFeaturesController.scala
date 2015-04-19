package org.sai.pumpkinx.ui.beans

import java.util.ArrayList
import scala.beans.BeanProperty
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import org.sai.pumpkinx.ui.config.Config
import api.FeatureDetail
import pumpkinx.api.ArtifactDetail
import scala.collection.JavaConversions._
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria

class SearchFeaturesController {
  val mongo = Config.getMongoTemplate

  @BeanProperty
  var searchedFeature: String = _

  @BeanProperty
  var found: Boolean = _

  @BeanProperty
  var allArtifactDetails: java.util.List[ArtifactDetail] = new ArrayList

  val allFeatures = getAllFeatures

  def onQuery(query: String) = {
    new ArrayList(new ArrayList(allFeatures.flatMap(_.features).filter(feature => feature.featureId.toLowerCase.contains(query.toLowerCase) || feature.featureTitle.toLowerCase.contains(query.toLowerCase))).map(feature => feature.featureId + " | " + feature.featureTitle).distinct)
  }

  def getAllFeatures() = {
    mongo.findAll(classOf[FeatureDetail])
  }

  def search() = {
    found = false
    allArtifactDetails.clear
    found = true

    val featureId = searchedFeature.substring(0, searchedFeature.indexOf("|")).trim
    println(featureId)
    val query8 = new Query()
    query8.addCriteria(Criteria.where("features.featureId").is(featureId))
    val allFeatureDetails = mongo.find(query8, classOf[FeatureDetail])

    allFeatureDetails.foreach(fd => {
      val query = new Query()
      query.addCriteria(Criteria.where("groupId").is(fd.groupId).andOperator(Criteria.where("artifactId").is(fd.artifactId), Criteria.where("version").is(fd.version)))
      allArtifactDetails.addAll(mongo.find(query, classOf[ArtifactDetail]))
    })
    allArtifactDetails.sortBy(-_.versionSequence)
  }
} 
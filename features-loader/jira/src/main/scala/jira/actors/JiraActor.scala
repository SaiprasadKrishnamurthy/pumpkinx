package jira.actors

import scala.collection.JavaConversions._

import org.json.JSONObject
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.core.util.Base64

import akka.actor.Actor
import akka.actor.ActorLogging
import api.ChangeSet
import api.FeatureDetail
import jira.config.Config
import pumpkinx.api.Feature
import pumpkinx.api.UnresolvedFeature

class JiraActor extends Actor with ActorLogging {

  def receive = {
    case _ => {
      log.info("Processing JIRA Calls ")
      process
    }
  }

  def process(): Unit = {
    val allChangeSets = Config.mongoTemplate.findAll(classOf[ChangeSet])
    val grouped = allChangeSets.groupBy(_.artifactScmDetail.toString)

    val auth = new String(Base64.encode(Config.getJiraCredentials))
    val client = Client.create()

    grouped.foreach(tuple => {
      val artifactScm = tuple._2.get(0).artifactScmDetail
      if (isAlreadyLoaded(artifactScm.groupId, artifactScm.artifactId, artifactScm.version) == false) {
        val allJiraCallsForThisVersion = tuple._2.flatMap(changeSet => Config.getJiraCallId(changeSet.commitMessage)).toSet.filter(_.startsWith("-") == false)
        val featureDetail = new FeatureDetail
        featureDetail.groupId = artifactScm.groupId
        featureDetail.artifactId = artifactScm.artifactId
        featureDetail.version = artifactScm.version

        val result = allJiraCallsForThisVersion.flatMap(getFeatureInfoFromJira)
        featureDetail.features.addAll(result.map(tuple => Feature(tuple._1.toString, tuple._2.toString, tuple._4.toString, tuple._3.toString, "")))

        if (featureDetail.features.size != 0) {
          Config.getMongoTemplate.save(featureDetail)
          println("Saved: " + tuple._1)
        }
      }

    })

    def getFeatureInfoFromJira(featureId: String) = {
      if (isUnresolved(featureId) == false) {
        try {
          val webResource = client.resource(Config.getJiraRestUrl + featureId)
          val response = webResource.header("Authorization", "Basic " + auth).`type`("application/json").accept("application/json").get(classOf[ClientResponse])
          val responseJson = response.getEntity(classOf[String])
          val projectArray = new JSONObject(responseJson)
          val featureKey = projectArray.get("key")
          val featureSummary = projectArray.get("fields").asInstanceOf[JSONObject].get("summary")
          val reporterName = projectArray.get("fields").asInstanceOf[JSONObject].get("reporter").asInstanceOf[JSONObject].get("name")
          val created = projectArray.get("fields").asInstanceOf[JSONObject].get("created")
          Some(featureKey, featureSummary, reporterName, created)
        } catch {
          case ex: Throwable => {
            val unresolved = new UnresolvedFeature
            unresolved.featureKey = featureId.trim
            Config.getMongoTemplate.save(unresolved)
            println(featureId + " ==> " + ex + "Marked as unresolved")
            None
          }
        }
      } else {
        log.info("Jira: " + featureId + " is marked as cannot be resolved ")
        None
      }
    }
  }

  def isAlreadyLoaded(groupId: String, artifactId: String, version: String) = {
    val query = new Query
    query.addCriteria(Criteria.where("groupId").is(groupId).and("artifactId").is(artifactId).and("version").is(version))
    val featureDetail = Config.getMongoTemplate.find(query, classOf[FeatureDetail])
    if (featureDetail.isEmpty) false else if (featureDetail.get(0).features.isEmpty) false else true
  }

  def isUnresolved(key: String) = {
    val query1 = new Query
    query1.addCriteria(Criteria.where("featureKey").is(key))
    val unresolved = Config.getMongoTemplate.find(query1, classOf[UnresolvedFeature])
    unresolved.isEmpty == false
  }
}
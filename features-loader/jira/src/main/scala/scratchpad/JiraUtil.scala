package scratchpad

import jira.config.Config
import api.ChangeSet
import scala.collection.JavaConversions._
import com.sun.jersey.core.util.Base64
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import org.json.JSONArray
import org.json.JSONObject
import api.FeatureDetail
import pumpkinx.api.Feature

object JiraUtil {

  def main(args: Array[String]): Unit = {
    val allChangeSets = Config.mongoTemplate.findAll(classOf[ChangeSet])
    val grouped = allChangeSets.groupBy(_.artifactScmDetail.toString)

    val auth = new String(Base64.encode("skrishnamurthy:galaxy123"))
    val client = Client.create()

    grouped.foreach(tuple => {
      val artifactScm = tuple._2.get(0).artifactScmDetail
      val allJiraCallsForThisVersion = tuple._2.flatMap(changeSet => Config.getJiraCallId(changeSet.commitMessage)).toSet.filter(_.startsWith("-") == false)
      val featureDetail = new FeatureDetail
      featureDetail.groupId = artifactScm.groupId
      featureDetail.artifactId = artifactScm.groupId
      featureDetail.version = artifactScm.version

      val result = allJiraCallsForThisVersion.flatMap(getFeatureInfoFromJira)
      featureDetail.features.addAll(result.map(tuple => Feature(tuple._1.toString, tuple._2.toString, tuple._4.toString, tuple._3.toString, "")))

      Config.getMongoTemplate.save(featureDetail)
      println("Saved: "+tuple._1)

    })

    def getFeatureInfoFromJira(featureId: String) = {
      try {
        val webResource = client.resource("http://jira.qif.sita.aero/rest/api/latest/issue/" + featureId)
        val response = webResource.header("Authorization", "Basic " + auth).`type`("application/json").accept("application/json").get(classOf[ClientResponse])
        val responseJson = response.getEntity(classOf[String])
        val projectArray = new JSONObject(responseJson)
        val featureKey = projectArray.get("key")
        val featureSummary = projectArray.get("fields").asInstanceOf[JSONObject].get("summary")
        val reporterName = projectArray.get("fields").asInstanceOf[JSONObject].get("reporter").asInstanceOf[JSONObject].get("name")
        val created = projectArray.get("fields").asInstanceOf[JSONObject].get("created")
        Some(featureKey, featureSummary, reporterName, created)
      } catch {
        case ex: Throwable =>
          println(featureId + " ==> " + ex)
          None
      }
    }
  }

}
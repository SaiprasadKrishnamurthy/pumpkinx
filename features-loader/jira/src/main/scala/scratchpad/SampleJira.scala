package scratchpad

import com.sun.jersey.api.client.Client
import org.json.JSONObject
import com.sun.jersey.core.util.Base64
import com.sun.jersey.api.client.ClientResponse
import scala.collection.JavaConversions._
import org.json.JSONArray

object SampleJira {

  def main(args: Array[String]): Unit = {

    val auth = new String(Base64.encode("skrishnamurthy:galaxy123"))
    val client = Client.create()

    def getFeatureInfoFromJira(featureId: String) = {
      try {
        val webResource = client.resource("http://jira.qif.sita.aero/rest/api/latest/issue/" + featureId)
        val response = webResource.header("Authorization", "Basic " + auth).`type`("application/json").accept("application/json").get(classOf[ClientResponse])
        val responseJson = response.getEntity(classOf[String])
        val projectArray = new JSONObject(responseJson)
        val featureKey = projectArray.get("key")
        val issueLinks = projectArray.get("fields").asInstanceOf[JSONObject].get("issuelinks").asInstanceOf[JSONArray]
        
        println(" Issue links for "+featureId+"---> "+issueLinks.length())
        
        val range = (0 until issueLinks.length())
        range.toList.indices.foreach(index => {
          val issue = issueLinks.get(index).asInstanceOf[JSONObject]
          
          if(issue.has("outwardIssue")) {
            val outwardIssue = issue.get("outwardIssue").asInstanceOf[JSONObject]
            val id = outwardIssue.get("key")
            val summary =  outwardIssue.get("fields").asInstanceOf[JSONObject].get("summary")
            val issueType =  outwardIssue.get("fields").asInstanceOf[JSONObject].get("issuetype").asInstanceOf[JSONObject].get("name")
            val statusType =  outwardIssue.get("fields").asInstanceOf[JSONObject].get("status").asInstanceOf[JSONObject].get("statusCategory").asInstanceOf[JSONObject].get("name")
            
            println(id + " | "+summary +" | "+issueType + " | "+statusType)
          }
          
        })
        
        
        
      } catch {
        case ex: Throwable =>
          println(featureId + " ==> " + ex)
          None
      }
    }

    val feature = "OMAN-196"
    val result = getFeatureInfoFromJira(feature)


  }

}
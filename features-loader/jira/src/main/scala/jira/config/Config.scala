package jira.config

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory

import com.mongodb.MongoClient

/**
 * Created by Sai on 30/03/2015.
 */
object Config {

  val mongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")

  val mongoTemplate = new MongoTemplate(mongo);

  def getParallelLoads = 100
  
  def getJiraCredentials = "pumpkin:asdqwe123"

  def getMongoTemplate = mongoTemplate
  
  def getJiraRestUrl = "http://jira.qif.sita.aero/rest/api/latest/issue/"

  val jiraCallsPattern = "[A-Z]*-[0-9]*.*?".r
  
  val hierarchyForRecentVersions = 3

  def getJiraCallId(message: String) = {
    jiraCallsPattern.findAllMatchIn(message).filter(_.toString.split("-").length > 1).map(_.toString).toList.filter(_.split("-").length > 1)
  }
}

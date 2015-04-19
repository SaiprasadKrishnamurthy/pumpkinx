package rpm.config

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory

import com.mongodb.MongoClient

/**
 * Created by Sai on 30/03/2015.
 */
object Config {

  def getRpmRepositoryRestUrlPattern = {
    "http://10.70.101.186/nexus/service/local/data_index?a=%s*&v=%s*"
  }

  def getLimitVersions() = 8
  
  def getParallelArtifactLoads = 10

  val mongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")

  val mongoTemplate = new MongoTemplate(mongo);

  def getMongoTemplate = {
    mongoTemplate
  }
}

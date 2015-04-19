package maven.config

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory

import com.mongodb.MongoClient

/**
 * Created by Kumar on 30/03/2015.
 */
object Config {

  def getMavenRepoUrls = {
    //    Array("http://central.maven.org/maven2/")
    "http://10.70.101.186/nexus/content/repositories/snapshots,http://10.70.101.186/nexus/content/repositories/thirdparty,http://10.70.101.186/nexus/content/repositories/releases".split(",")
  }

  def getLimitVersions() = 20
  
  def getParallelArtifactLoads = 10

  val mongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")

  val mongoTemplate = new MongoTemplate(mongo);

  def getMongoTemplate = {
    mongoTemplate
  }

}

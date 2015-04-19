package base.trigger

import rpm.config.Config
import scala.collection.JavaConversions._
import pumpkinx.api.ArtifactConfig
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import pumpkinx.api.ArtifactGroup
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import pumpkinx.api.ArtifactConfig
import scala.collection.JavaConversions._
import com.mongodb._

object LoadRpmConfigs {

  def main(args: Array[String]): Unit = {
    
    val localMongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")
    val localmongoTemplate = new MongoTemplate(localMongo)
    
    
    val remoteMongo = new SimpleMongoDbFactory(new MongoClient("10.70.101.135"), "pumpkinx")
    val mongo = new MongoTemplate(remoteMongo)
    val allMeta = localmongoTemplate.findAll(classOf[ArtifactMetadata])

    allMeta.foreach(meta => {
      val query5 = new Query()
      query5.addCriteria(Criteria.where("artifactId").is(meta.artifactId))
      val artifactConfig = mongo.find(query5, classOf[ArtifactConfig])

      val rpmConfig = new ArtifactConfig
      rpmConfig.artifactId = meta.rpmName + "*"
      rpmConfig.artifactCategory = meta.artifactCatgory
      rpmConfig.artifactFamily = "rpm"

      if (artifactConfig.isEmpty == false && meta.rpmName != null && meta.rpmName.length > 0) {
        val artifactGroup = new ArtifactGroup
        artifactGroup.members.add(artifactConfig(0))
        artifactGroup.members.add(rpmConfig)
        mongo.save(artifactGroup)
      }
    })
    println("Done..")
  }

}
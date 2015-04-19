package base.trigger

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import com.mongodb.MongoClient
import pumpkinx.api.ArtifactConfig
import scala.collection.JavaConversions._

object CopyArtifactConfig {

  def main(args: Array[String]): Unit = {

    val localMongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")
    val localmongoTemplate = new MongoTemplate(localMongo)
    

    val remoteMongo = new SimpleMongoDbFactory(new MongoClient("10.70.101.135"), "pumpkinx")
    val remoteMongoTemplate = new MongoTemplate(remoteMongo)
    localmongoTemplate.findAll(classOf[ArtifactConfig]).foreach(remoteMongoTemplate.save)
    println("Finished!")

  }

}
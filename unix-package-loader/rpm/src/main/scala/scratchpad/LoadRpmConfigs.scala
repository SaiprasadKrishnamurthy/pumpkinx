package scratchpad

import rpm.config.Config
import pumpkinx.api.ArtifactMetadata
import scala.collection.JavaConversions._
import pumpkinx.api.ArtifactConfig
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import pumpkinx.api.ArtifactGroup

object LoadRpmConfigs {

  def main(args: Array[String]): Unit = {
    val mongo = Config.getMongoTemplate
    val allMeta = mongo.findAll(classOf[ArtifactMetadata])

    allMeta.foreach(meta => {
      val query5 = new Query()
      query5.addCriteria(Criteria.where("artifactId").is(meta.artifactId))
      val artifactConfig = mongo.find(query5, classOf[ArtifactConfig])

      val rpmConfig = new ArtifactConfig
      rpmConfig.artifactId = meta.rpmName+"*"
      rpmConfig.artifactCategory = meta.artifactCatgory
      rpmConfig.artifactFamily = "rpm"
      
      if (artifactConfig.isEmpty == false && meta.rpmName != null && meta.rpmName.length > 0) {
        val artifactGroup = new ArtifactGroup
        artifactGroup.members.add(artifactConfig(0))
        artifactGroup.members.add(rpmConfig)
        mongo.save(artifactGroup)
      }
    })
  }

}
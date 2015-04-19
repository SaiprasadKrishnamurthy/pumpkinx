package maven.util

import maven.config.Config
import com.sai.pumpkin.domain.ArtifactMetadata
import scala.collection.JavaConversions._
import pumpkinx.api.ArtifactConfig

object DBUtil {

  def main(args: Array[String]): Unit = {
    val allmeta = Config.getMongoTemplate.findAll(classOf[ArtifactMetadata])

    val allConfig = allmeta.map(meta => {
      val artifactConfig = new ArtifactConfig
      artifactConfig.groupId = meta.groupId
      artifactConfig.artifactId = meta.artifactId
      artifactConfig.artifactCategory = meta.artifactCatgory
      artifactConfig.artifactFamily = "maven"
      artifactConfig.buildServerType = "jenkins"
      artifactConfig.licence = "SITA proprietary"
      artifactConfig.licenceText = """This code contains copyright information which is the proprietary property
                                      of SITA Advanced Travel Solutions. No part of this code may be reproduced,
                                      stored or transmitted in any form without the prior written permission of
                                      SITA Advanced Travel Solutions.

                                      Copyright SITA Advanced Travel Solutions 2013
                                      All rights reserved."""
      artifactConfig.scmLocation = meta.svnLocation
      artifactConfig.scmType = "Subversion"
      artifactConfig.codeQualityMetricsUrl = meta.klocworkJobName
      artifactConfig.codeQualityMetricsType = "Klocwork"
      artifactConfig
    }).foreach(Config.getMongoTemplate.save)
  }

}
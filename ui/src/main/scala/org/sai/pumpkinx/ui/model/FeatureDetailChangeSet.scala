package org.sai.pumpkinx.ui.model

import api.FeatureDetail
import api.ChangeSet
import scala.beans.BeanProperty

case class FeatureDetailChangeSet(@BeanProperty featureDetail: FeatureDetail, @BeanProperty changeSets: java.util.ArrayList[ChangeSet])
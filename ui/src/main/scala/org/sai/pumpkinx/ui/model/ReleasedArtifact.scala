package org.sai.pumpkinx.ui.model

import pumpkinx.api.ArtifactDetail
import pumpkinx.api.ArtifactScmDetail
import scala.beans.BeanProperty
import api.FeatureDetail

case class ReleasedArtifact (@BeanProperty artifact: ArtifactDetail, @BeanProperty artifactScmDetail: ArtifactScmDetail, @BeanProperty featureDetail: FeatureDetail)
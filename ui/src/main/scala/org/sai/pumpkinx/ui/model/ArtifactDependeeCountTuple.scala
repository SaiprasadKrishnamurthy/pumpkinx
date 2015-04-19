package org.sai.pumpkinx.ui.model

import pumpkinx.api.ArtifactDetail
import scala.beans.BeanProperty

case class ArtifactDependeeCountTuple (@BeanProperty artifact: ArtifactDetail, @BeanProperty dependeeCount: Int)
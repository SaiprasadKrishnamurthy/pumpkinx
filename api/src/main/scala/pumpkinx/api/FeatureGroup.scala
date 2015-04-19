package pumpkinx.api

import java.util.ArrayList

import scala.beans.BeanProperty

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "feature_group")
class FeatureGroup {

  @BeanProperty
  @Id
  var id: String = _

  @BeanProperty
  var feature: Feature = _

  @BeanProperty
  var outwardDependencies: java.util.List[Feature] = new ArrayList

  @BeanProperty
  var inwardDependencies: java.util.List[Feature] = new ArrayList

}
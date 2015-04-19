package pumpkinx.api

import java.util.ArrayList
import scala.beans.BeanProperty
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="unresolved_feature")
class UnresolvedFeature {
  @BeanProperty
  var featureKey: String = _
}
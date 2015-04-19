package pumpkinx.api

import java.util
import org.springframework.data.annotation.Id
import scala.beans.BeanProperty
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by Kumar on 30/03/2015.
 */
@Document(collection="artifact_group")
class ArtifactGroup {

  @Id
  var id: String = _

  @BeanProperty
  var members: java.util.ArrayList[ArtifactConfig] = new util.ArrayList[ArtifactConfig]()

}


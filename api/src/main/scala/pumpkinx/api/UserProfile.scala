package pumpkinx.api

import org.springframework.data.mongodb.core.mapping.Document
import scala.beans.BeanProperty

@Document(collection="user_profile")
class UserProfile {

  @BeanProperty
  var user: User = _
  
  @BeanProperty
  var phoneNumber: String = _
  
  @BeanProperty
  var location: String = _
  
  @BeanProperty
  var teamName: String = _
  
  @BeanProperty
  var projectName: String = _
  
  @BeanProperty
  var department: String = _
  
  @BeanProperty
  var scmUserId: String = _
  
  @BeanProperty
  var featureRepositoryId: String = _
}
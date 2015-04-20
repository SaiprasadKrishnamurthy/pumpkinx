package org.sai.pumpkinx.ui.beans

import java.util.ArrayList
import scala.beans.BeanProperty
import org.sai.pumpkinx.ui.config.Config
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import pumpkinx.api.ArtifactDetail
import org.springframework.data.mongodb.core.query.Query
import org.apache.taglibs.standard.lang.jstl.AndOperator
import org.springframework.data.mongodb.core.query.Criteria
import pumpkinx.api.ReleaseNotes
import javax.servlet.http.HttpServletRequest
import javax.faces.context.FacesContext
import pumpkinx.api.User
import javax.servlet.http.HttpServletResponse
import org.primefaces.context.RequestContext
import javax.faces.application.FacesMessage
import pumpkinx.api.UserProfile

class UserController {

  val mongo = Config.getMongoTemplate
  val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
  val httpResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse().asInstanceOf[HttpServletResponse]

  @BeanProperty
  var user: UserProfile = _

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

  @BeanProperty
  var fullName: String = _

  @BeanProperty
  var userId: String = _

  @BeanProperty
  var password: String = _

  @BeanProperty
  var email: String = _

  @BeanProperty
  var locations = new ArrayList(Config.locations.toList)

  @BeanProperty
  var message: String = _

  def register() = {
    message = ""
    locations.clear
    locations = new ArrayList(Config.locations.toList)
    val query5 = new Query()
    query5.addCriteria(Criteria.where("userId").is(userId))
    val isUserIdPresent = mongo.find(query5, classOf[User]).isEmpty == false

    if (isUserIdPresent) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "User Id already exists. Please choose a different one."));
    } else {
      val user = User(fullName, userId, password, email)
      val userProfile = new UserProfile
      userProfile.department = department
      userProfile.location = location
      userProfile.phoneNumber = phoneNumber
      userProfile.featureRepositoryId = featureRepositoryId
      userProfile.projectName = projectName
      userProfile.scmUserId = scmUserId
      userProfile.user = user
      userProfile.teamName = teamName
      mongo.save(userProfile)
      mongo.save(user)
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Registration successful"));
    }
  }

  val userInSession = if (httpRequest.getSession(false) != null) httpRequest.getSession(false).getAttribute("user").asInstanceOf[User] else null

  if (userInSession != null) {
    val query5 = new Query()
    query5.addCriteria(Criteria.where("user.userId").is(userInSession.userId))
    user = mongo.find(query5, classOf[UserProfile]).get(0)
    email = user.user.email
    location = user.location
    teamName = user.teamName
    department = user.department
    fullName = user.user.fullName
    projectName = user.projectName
    scmUserId = user.scmUserId
    featureRepositoryId = user.featureRepositoryId
  }

  def save = {
    if (userInSession == null) {
      httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "User not logged in")
    }
    val updatedUser = User(fullName, user.user.userId, userInSession.password, email)
    val userProfile = new UserProfile
    userProfile.department = department
    userProfile.location = location
    userProfile.teamName = teamName
    userProfile.phoneNumber = phoneNumber
    userProfile.featureRepositoryId = featureRepositoryId
    userProfile.projectName = projectName
    userProfile.scmUserId = scmUserId
    userProfile.user = updatedUser

    // Delete and insert.
    val query5 = new Query()
    query5.addCriteria(Criteria.where("user.userId").is(userInSession.userId))
    mongo.remove(query5, classOf[UserProfile])

    val query6 = new Query()
    query6.addCriteria(Criteria.where("userId").is(userInSession.userId))
    mongo.remove(query6, classOf[User])

    mongo.save(userProfile)
    mongo.save(updatedUser)

    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Profile updated."));

  }
}
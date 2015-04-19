package org.sai.pumpkinx.ui.beans

import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.sai.pumpkinx.ui.config.Config
import pumpkinx.api.ArtifactConfig
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import java.util.ArrayList
import pumpkinx.api.ArtifactDetail
import org.springframework.data.domain.Sort
import javax.faces.context.FacesContext
import javax.servlet.http.HttpServletRequest
import pumpkinx.api.ArtifactScmDetail
import org.primefaces.model.mindmap._
import api.ChangeSet
import java.util.Date
import api.ChangeSet
import org.primefaces.model.chart.PieChartModel
import api.FeatureDetail
import pumpkinx.api.Feature
import api.SourceFileDetail
import org.primefaces.context.RequestContext
import pumpkinx.api.User
import javax.servlet.http.HttpServletResponse

class LoginController {
  val mongo = Config.getMongoTemplate
  
  @BeanProperty
  var userId: String = _
  
  @BeanProperty
  var password: String = _
  
  def login = {
     val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
     val httpResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse().asInstanceOf[HttpServletResponse]
     
     val query5 = new Query()
     query5.addCriteria(Criteria.where("userId").is(userId).andOperator(Criteria.where("password").is(password)))
     val user = mongo.find(query5, classOf[User]).get(0)
     httpRequest.getSession().setAttribute("user", user)
     RequestContext.getCurrentInstance().addCallbackParam("loggedIn", true)
  }
  
  def logout = {
     val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
     val httpResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse().asInstanceOf[HttpServletResponse]
     if(httpResponse.isCommitted == false) {
         httpRequest.getSession().invalidate()
     }
     httpResponse.sendRedirect("welcome.do")
  }
} 
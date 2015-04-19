Temporary Fudge
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

class CreateReleaseController {

  val mongo = Config.getMongoTemplate

  @BeanProperty
  var releaseDate: java.util.Date = _

  @BeanProperty
  var releaseDescription: String = _

  @BeanProperty
  var releaseName: String = _

  @BeanProperty
  var artifacts: java.util.List[ArtifactDetail] = new ArrayList

  @BeanProperty
  var selectedArtifactId: String = _

  @BeanProperty
  var allSelected: java.util.List[String] = new ArrayList

  @BeanProperty
  var artifactIds: java.util.List[String] = new ArrayList

  val allArtifacts = mongo.findAll(classOf[ArtifactDetail])

  val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
  val httpResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse().asInstanceOf[HttpServletResponse]
  val user = httpRequest.getSession().getAttribute("user").asInstanceOf[User]

  if (user == null) {
    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN)
  }

  def onQuery(query: String) = {
    val filtered = allArtifacts.filter(a => a.artifactId.contains(query) || a.version.contains(query)).map(a => a.artifactId + " (" + a.version + ") ")
    new java.util.ArrayList(filtered)
  }

  def save = {
    println("Save called ==> " + artifactIds)
    artifacts = artifactIds.map(art => {
      val selectedArtifactid = art.substring(0, art.indexOf("(")).trim
      val version = art.substring(art.indexOf("(") + 1, art.indexOf(")"))
      var query5 = new Query()
      query5.addCriteria(Criteria.where("artifactId").is(selectedArtifactid).andOperator(Criteria.where("version").is(version)))
      val _artifact = mongo.find(query5, classOf[ArtifactDetail])
      _artifact(0)
    })

    val release = new ReleaseNotes
    release.releaseName = releaseName
    release.releaseLead = user.fullName
    release.date = releaseDate
    release.notes = releaseDescription
    release.components.addAll(artifacts)

    mongo.save(release)

    RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Release notes for "+releaseName+" saved successfully."))

  }

  def add = {
    if (selectedArtifactId != null) {
      println("Add called")
      allSelected.add(selectedArtifactId)
    }
  }
}
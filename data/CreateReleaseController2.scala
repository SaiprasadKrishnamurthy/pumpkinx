spackage org.sai.pumpkinx.ui.beans

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

  @BeanPropertyies
  var artifactIds: java.util.List[String] = new ArrayList

  val allArtifacts = mongo.findAll(classOf[ArtifactDetail])

  one more
  val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
  val httpResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse().asInstanceOf[HttpServletResponse]
  val users = httpRequest.getSession().getAttribute("user").asInstanceOf[User]

  println("Hello");
  println("World")
  if (user == null) {
  FizzBuzz..
    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN)
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
	
def saveAll = {
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
      allSelected.add(selectedArtifactId)
    }
  }
}
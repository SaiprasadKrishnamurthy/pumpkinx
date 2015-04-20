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
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.User
import pumpkinx.api.ArtifactGroup

class RegisterArtifactController {

  val mongo = Config.getMongoTemplate

  @BeanProperty
  var name: String = _

  @BeanProperty
  var summary: String = _

  @BeanProperty
  var owner: String = _

  @BeanProperty
  var groupId: String = _

  @BeanProperty
  var artifactId: String = _

  @BeanProperty
  var packaging: String = _

  @BeanProperty
  var classifier: String = _

  @BeanProperty
  var scmLocation: String = _

  // eg: svn
  @BeanProperty
  var scmType: String = _

  // eg: maven
  @BeanProperty
  var artifactFamily: String = _

  // eg: utility, webapp, etc.
  @BeanProperty
  var artifactCategory: String = _

  @BeanProperty
  var licence: String = _

  @BeanProperty
  var licenceUrl: String = _

  @BeanProperty
  var licenceText: String = _

  @BeanProperty
  var buildServerUrl: String = _

  // eg: jenkins
  @BeanProperty
  var buildServerType: String = _

  @BeanProperty
  var codeQualityMetricsUrl: String = _

  @BeanProperty
  var packages: String = _

  @BeanProperty
  var existingFound: Boolean = _

  var allArtifacts = mongo.findAll(classOf[ArtifactConfig])
  val allUsers = mongo.findAll(classOf[User])

  val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
  val httpResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse().asInstanceOf[HttpServletResponse]
  val user = httpRequest.getSession().getAttribute("user").asInstanceOf[User]

  if (user == null) {
    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN)
  }

  def onGroupId(query: String) = {
    new java.util.ArrayList(allArtifacts.filter(a => a.groupId != null && a.groupId.contains(query)).map(_.groupId).distinct)
  }

  def onOwner(query: String) = {
    new java.util.ArrayList(allUsers.filter(a => a.fullName.toLowerCase.contains(query.toLowerCase)).map(_.userId))
  }

  def onLicence(query: String) = {
    new java.util.ArrayList(allArtifacts.filter(a => a.licence != null && a.licence.toLowerCase.contains(query.toLowerCase)).map(_.licence).distinct)
  }

  def onPackaging(query: String) = {
    new java.util.ArrayList(allArtifacts.filter(a => a.packaging != null && a.packaging.toLowerCase.contains(query.toLowerCase)).map(_.packaging).distinct)
  }

  def onCategory(query: String) = {
    new java.util.ArrayList(allArtifacts.filter(a => a.artifactCategory != null && a.artifactCategory.toLowerCase.contains(query.toLowerCase)).map(_.artifactCategory).distinct)
  }

  def onArtifactId = {
    existingFound = false
    println("GID: " + groupId)
    println("AID: " + artifactId)
    existingFound = checkExists
  }

  def checkExists = {
    allArtifacts = mongo.findAll(classOf[ArtifactConfig])
    if (allArtifacts.filter(a => a.groupId != null && groupId != null && a.groupId.equals(groupId) && a.artifactId != null && artifactId != null && a.artifactId.equals(artifactId)).isEmpty == false) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Artifact coordinates already exists.(groupId, artifactId combination must be unique). Please contact the administrator if you wish to update the existing version."))
      true
    } else {
      false
    }
  }

  def loadExisting = {
    val existingConfig = allArtifacts.filter(a => a.groupId.contains(groupId) && a.artifactId.contains(artifactId)).get(0)
    name = existingConfig.name
    summary = existingConfig.summary
    artifactCategory = existingConfig.artifactCategory
    artifactFamily = existingConfig.artifactFamily
    artifactId = existingConfig.artifactId
    groupId = existingConfig.groupId
    licence = existingConfig.licence
    licenceText = existingConfig.licenceText
    licenceUrl = existingConfig.licenceUrl
    buildServerUrl = existingConfig.buildServerUrl
    codeQualityMetricsUrl = existingConfig.codeQualityMetricsUrl
    owner = existingConfig.owner

    var query5 = new Query()
    query5.addCriteria(Criteria.where("members.groupId").is(groupId).andOperator(Criteria.where("members.artifactId").is(artifactId)))
    val group = mongo.find(query5, classOf[ArtifactGroup]).get(0)
    packages = group.members.filter(_.artifactFamily.equals("rpm")).map(_.artifactId).mkString(",")
  }

  def save = {

    if (checkExists == false) {
      // replace.
      var existingArtifactGroupQuery = new Query()
      existingArtifactGroupQuery.addCriteria(Criteria.where("members.groupId").is(groupId).andOperator(Criteria.where("members.artifactId").is(artifactId)))

      var existingArtifactConfigQuery = new Query()
      existingArtifactConfigQuery.addCriteria(Criteria.where("groupId").is(groupId).andOperator(Criteria.where("artifactId").is(artifactId)))

      var existingArtifactConfigForPackageQuery = new Query()
      existingArtifactConfigForPackageQuery.addCriteria(Criteria.where("groupId").is(null).andOperator(Criteria.where("artifactId").regex(packages)))

      mongo.remove(existingArtifactGroupQuery, classOf[ArtifactGroup])
      mongo.remove(existingArtifactConfigQuery, classOf[ArtifactConfig])
      mongo.remove(existingArtifactConfigForPackageQuery, classOf[ArtifactConfig])

      // main config.
      val artifact = new ArtifactConfig
      artifact.name = name
      artifact.summary = summary
      artifact.artifactCategory = artifactCategory
      artifact.artifactFamily = artifactFamily
      artifact.artifactId = artifactId
      artifact.groupId = groupId
      artifact.licence = licence
      artifact.licenceText = licenceText
      artifact.licenceUrl = licenceUrl
      artifact.buildServerUrl = buildServerUrl
      artifact.codeQualityMetricsUrl = codeQualityMetricsUrl
      artifact.owner = owner
      artifact.artifactFamily = "maven"
      mongo.save(artifact)

      // packages.
      val pkgs = packages.split(",").map(pkg => {
        val packageArtifact = new ArtifactConfig
        packageArtifact.artifactId = pkg
        packageArtifact.artifactFamily = "rpm"
        packageArtifact
      }).toList
      pkgs.foreach(mongo.save)

      // group.
      val grp = new ArtifactGroup
      grp.members.add(artifact)
      grp.members.addAll(pkgs)
      mongo.save(grp)
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Artifact Registration successful"));
    }
  }
}
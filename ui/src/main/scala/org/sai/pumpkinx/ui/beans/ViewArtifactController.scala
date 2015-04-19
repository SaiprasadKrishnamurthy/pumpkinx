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
import org.primefaces.event.RateEvent
import javax.faces.application.FacesMessage
import pumpkinx.api.User
import pumpkinx.api.User
import pumpkinx.api.Issue
import org.primefaces.extensions.model.timeline.TimelineModel
import org.primefaces.extensions.model.timeline.TimelineEvent
import api.ChangeSet
import api.ChangeSetEntry
import scala.io.Source
import org.primefaces.context.RequestContext
import scala.xml._
import org.sai.pumpkinx.ui.util.DiffGen
import org.sai.pumpkinx.ui.util.SvnUtil

class ViewArtifactController {
  val mongo = Config.getMongoTemplate

  @BeanProperty
  var artifact: ArtifactDetail = _

  @BeanProperty
  var prevArtifact: ArtifactDetail = _

  @BeanProperty
  var dependees: java.util.List[ArtifactDetail] = _

  @BeanProperty
  var features: java.util.List[Feature] = _

  @BeanProperty
  var changeSets: java.util.List[ChangeSet] = _

  @BeanProperty
  var deltas: java.util.List[String] = _

  @BeanProperty
  var artifactConfig: ArtifactConfig = _

  @BeanProperty
  var artifactScmDetail: ArtifactScmDetail = _

  @BeanProperty
  var found: Boolean = _

  @BeanProperty
  var followingThisVersion: String = _

  @BeanProperty
  var followingAnyVersion: String = _

  @BeanProperty
  var canRate: Boolean = _

  @BeanProperty
  var dependencyTree: MindmapNode = _

  @BeanProperty
  var averageReleaseDuration: Int = _

  @BeanProperty
  var latestArtifactVersion: String = _

  @BeanProperty
  var releaseDate: Date = _

  @BeanProperty
  var lagBy: Int = _

  @BeanProperty
  var rating: Int = _

  @BeanProperty
  var pie: PieChartModel = _

  @BeanProperty
  var committers: java.util.List[String] = _

  @BeanProperty
  var followers: java.util.List[User] = _

  @BeanProperty
  var files: java.util.List[String] = _

  @BeanProperty
  var issueId: String = _

  @BeanProperty
  var issueUrl: String = _

  @BeanProperty
  var issueTitle: String = _

  @BeanProperty
  var issueDescription: String = _

  @BeanProperty
  var timelineModel: TimelineModel = _

  @BeanProperty
  var prevContent: String = _

  @BeanProperty
  var currContent: String = _
  
  @BeanProperty
  var diffSource: DiffSource = _
  

  val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
  
  println(" ---> "+httpRequest.getSession(false))
  val user = if(httpRequest.getSession(false) != null) httpRequest.getSession(false).getAttribute("user").asInstanceOf[User] else null

  val groupId = httpRequest.getParameter("groupId")
  val artifactId = httpRequest.getParameter("artifactId")
  val version = httpRequest.getParameter("version")

  var query5 = new Query()
  query5.addCriteria(Criteria.where("groupId").is(groupId).andOperator(Criteria.where("artifactId").is(artifactId), Criteria.where("version").is(version)))
  val _artifact = mongo.find(query5, classOf[ArtifactDetail])
  
  if (_artifact.isEmpty == false) {
    found = true
    artifact = _artifact.get(0)

    query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(groupId).andOperator(Criteria.where("artifactId").is(artifactId)))
    artifactConfig = mongo.find(query5, classOf[ArtifactConfig]).get(0)

    query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(groupId).andOperator(Criteria.where("artifactId").is(artifactId), Criteria.where("version").is(version)))
    val _artifactScmDetail = mongo.find(query5, classOf[ArtifactScmDetail])
    if (_artifactScmDetail.isEmpty == false) {
      artifactScmDetail = _artifactScmDetail.get(0)
    }

    // dependee
    val query8 = new Query()
    query8.addCriteria(Criteria.where("children.artifactId").is(artifactId).andOperator(Criteria.where("children.version").is(version), Criteria.where("children.groupId").is(groupId)))
    dependees = mongo.find(query8, classOf[ArtifactDetail])

    // dependency tree
    dependencyTree = new DefaultMindmapNode(artifact.artifactId + "(" + artifact.version + ")", artifact.toString, "FFCC00", false);

    artifact.children.foreach(child => {
      dependencyTree.addNode(new DefaultMindmapNode(child.artifactId + "(" + child.version + ")", child.toString, "6e9ebf", true))
    })

    // average release duration.
    val query = new Query()
    query.addCriteria(Criteria.where("artifactScmDetail.groupId").is(artifact.groupId).andOperator(Criteria.where("artifactScmDetail.artifactId").is(artifactId)))
    val allChangeSets = mongo.find(query, classOf[ChangeSet])

    val grouped = allChangeSets.groupBy(_.artifactScmDetail.toString)
    val minChangeSets = grouped.map(tuple => tuple._2.minBy(changeSet => changeSet.commitDate))
    val minDates = minChangeSets.map(_.commitDate.getTime).toList

    var runningTotal = 0L
    minDates.reverse.indices.foreach(index => {
      if (index != 0) {
        runningTotal = runningTotal + (minDates.get(index) - minDates.get(index - 1))
      }
    })

    if (minDates.isEmpty == false) {
      val avgReleaseDurationInMillis = runningTotal / minDates.size

      averageReleaseDuration = (avgReleaseDurationInMillis / (1000 * 60 * 60 * 24)).toInt.abs
    }
    // latest version
    val query1 = new Query()
    query1.addCriteria(Criteria.where("groupId").is(artifact.groupId).and("artifactId").is(artifact.artifactId).and("versionSequence").gt(artifact.versionSequence))
    val allNewerArtifacts = mongo.find(query1, classOf[ArtifactDetail])

    if (allNewerArtifacts.isEmpty() == false) {
      latestArtifactVersion = allNewerArtifacts.sortBy(_.versionSequence).reverse.get(0).version
      lagBy = allNewerArtifacts.size()
    } else {
      latestArtifactVersion = "This is the LATEST version"
    }

    // Release date
    val sortedChangesets = allChangeSets.filter(changeSet => changeSet.artifactScmDetail.version.equals(artifact.version)).sortBy(_.commitDate)
    if (sortedChangesets.isEmpty == false)
      releaseDate = sortedChangesets.last.commitDate

    // All changesets.
    var query4 = new Query()
    query4.addCriteria(Criteria.where("artifactScmDetail.groupId").is(artifact.groupId).andOperator(Criteria.where("artifactScmDetail.artifactId").is(artifact.artifactId), Criteria.where("artifactScmDetail.version").is(artifact.version)))
    changeSets = mongo.find(query4, classOf[ChangeSet])

    // author contribution
    pie = new PieChartModel()
    val allChangesetsPerAuthor = changeSets.groupBy(_.committer)
    allChangesetsPerAuthor.map(tuple => pie.set(tuple._1, tuple._2.flatMap(_.entries).size))

    // Features.
    query4 = new Query()
    query4.addCriteria(Criteria.where("groupId").is(artifact.groupId).andOperator(Criteria.where("artifactId").is(artifact.artifactId), Criteria.where("version").is(artifact.version)))
    val featureDetail = mongo.find(query4, classOf[FeatureDetail])
    if (featureDetail.isEmpty() == false) {
      features = featureDetail.get(0).features
    }

    // committers.
    committers = new ArrayList(changeSets.map(_.committer).toSet)

    // source paths
    query4 = new Query()
    query4.addCriteria(Criteria.where("groupId").is(artifact.groupId).andOperator(Criteria.where("artifactId").is(artifact.artifactId), Criteria.where("version").is(artifact.version)))
    val sourceDetail = mongo.find(query4, classOf[SourceFileDetail])
    if (sourceDetail.isEmpty() == false) {
      files = new ArrayList(sourceDetail.get(0).filePaths)
    }
    rating = artifact.rating
  }

  // rating
  canRate = if (user != null && artifact != null && artifact.raters.filter(_.userId.equalsIgnoreCase(user.userId)).size == 0) true else false
  def onrate(rateEvent: RateEvent) {
    var query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(groupId).andOperator(Criteria.where("artifactId").is(artifactId), Criteria.where("version").is(version)))
    val _artifact = mongo.find(query5, classOf[ArtifactDetail]).get(0)
    canRate = if (user != null && _artifact != null && _artifact.raters.filter(_.userId.equalsIgnoreCase(user.userId)).size == 0) true else false
    if (canRate) {
      val message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Rate Event", "You rated:" + rateEvent.getRating().asInstanceOf[Int]);
      FacesContext.getCurrentInstance().addMessage(null, message);

      val ratersCount = _artifact.ratersCount + 1
      _artifact.ratersCount = ratersCount
      _artifact.raters.add(user)
      _artifact.ratingCount = (_artifact.ratingCount + rateEvent.getRating().asInstanceOf[Int])
      _artifact.rating = _artifact.ratingCount / _artifact.ratersCount

      // Replace.
      val query = new Query()
      query.addCriteria(Criteria.where("groupId").is(_artifact.groupId).andOperator(Criteria.where("artifactId").is(_artifact.artifactId), Criteria.where("version").is(_artifact.version)))
      mongo.findAndRemove(query, classOf[ArtifactDetail])
      mongo.save(_artifact)
      artifact = _artifact
    } else {
      val message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You cant rate this artifact as you've already rated.");
      FacesContext.getCurrentInstance().addMessage(null, message);
    }
  }

  // following
  followingAnyVersion = if (user != null && artifactConfig != null && artifactConfig.followers.filter(u => u.userId.equalsIgnoreCase(user.userId)).size > 0) "true" else "false"
  followingThisVersion = if (user != null && artifact != null && artifact.followers.filter(u => u.userId.equalsIgnoreCase(user.userId)).size > 0) "true" else "false"

  setFollowers(artifactConfig, artifact)

  def onFollowAnyVersion = {
    query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(groupId).andOperator(Criteria.where("artifactId").is(artifactId)))
    artifactConfig = mongo.find(query5, classOf[ArtifactConfig]).get(0)
    if (followingAnyVersion.toBoolean.equals(false)) {
      artifactConfig.followers.remove(user)
      val query = new Query()
      query.addCriteria(Criteria.where("groupId").is(artifactConfig.groupId).andOperator(Criteria.where("artifactId").is(artifactConfig.artifactId)))
      mongo.findAndRemove(query, classOf[ArtifactConfig])
      mongo.save(artifactConfig)
    } else {
      artifactConfig.followers.add(user)
      val query = new Query()
      query.addCriteria(Criteria.where("groupId").is(artifactConfig.groupId).andOperator(Criteria.where("artifactId").is(artifactConfig.artifactId)))
      mongo.findAndRemove(query, classOf[ArtifactConfig])
      mongo.save(artifactConfig)
    }
    setFollowers(artifactConfig, artifact)
  }

  def onFollowThisVersion = {
    query5 = new Query()
    query5.addCriteria(Criteria.where("groupId").is(groupId).andOperator(Criteria.where("artifactId").is(artifactId), Criteria.where("version").is(artifact.version)))
    artifact = mongo.find(query5, classOf[ArtifactDetail]).get(0)
    if (followingThisVersion.toBoolean.equals(false)) {
      artifact.followers.remove(user)
      val query = new Query()
      query.addCriteria(Criteria.where("groupId").is(artifact.groupId).andOperator(Criteria.where("artifactId").is(artifact.artifactId), Criteria.where("version").is(artifact.version)))
      mongo.findAndRemove(query, classOf[ArtifactDetail])
      mongo.save(artifact)
    } else {
      artifact.followers.add(user)
      val query = new Query()
      query.addCriteria(Criteria.where("groupId").is(artifact.groupId).andOperator(Criteria.where("artifactId").is(artifact.artifactId), Criteria.where("version").is(artifact.version)))
      mongo.findAndRemove(query, classOf[ArtifactDetail])
      mongo.save(artifact)
    }
    setFollowers(artifactConfig, artifact)
  }

  def setFollowers(artifactConfig: ArtifactConfig, artifact: ArtifactDetail) = {
    followers = new ArrayList((artifactConfig.followers ++ artifact.followers).distinct)
  }

  // dependencies differences.
  val query = new Query()
  val prevSeq = artifact.versionSequence - 1
  query.addCriteria(Criteria.where("groupId").is(artifact.groupId).andOperator(Criteria.where("artifactId").is(artifact.artifactId), Criteria.where("versionSequence").is(prevSeq)))
  val prev = mongo.find(query, classOf[ArtifactDetail])

  if (prev.isEmpty() == false) {
    val currDeps = artifact.children
    val prevDeps = prev.get(0).children
    prevArtifact = prev.get(0)

    // modified
    val modifiedDeps = currDeps.filter(curr => prevDeps.filter(pre => pre.artifactId.equals(curr.artifactId) && !pre.version.equals(curr.version)).size > 0)

    // added.
    val addedDeps = currDeps.filter(curr => prevDeps.filter(pre => pre.artifactId.equals(curr.artifactId)).size == 0)

    // removed.
    val removedDeps = prevDeps.filter(pre => currDeps.filter(curr => curr.artifactId.equals(pre.artifactId)).size == 0)

    deltas = new ArrayList

    deltas.addAll(addedDeps.map(ad => "[DEPENDENCY ADDED] " + ad.groupId + " | " + ad.artifactId + " | " + ad.version))

    deltas.addAll(removedDeps.map(ad => "[DEPENDENCY REMOVED] " + ad.groupId + " | " + ad.artifactId + " | " + ad.version))

    deltas.addAll(modifiedDeps.map(ad => "[DEPENDENCY MODIFIED] " + ad.groupId + " | " + ad.artifactId + " From: " + prevDeps.filter(p => p.groupId.equals(ad.groupId) && p.artifactId.equals(ad.artifactId)).get(0).version + " To:  " + ad.version))
  }

  // report issue
  def reportIssue() = {
    // Replace.
    val query = new Query()
    query.addCriteria(Criteria.where("groupId").is(artifact.groupId).andOperator(Criteria.where("artifactId").is(artifact.artifactId), Criteria.where("version").is(artifact.version)))
    mongo.findAndRemove(query, classOf[ArtifactDetail])

    artifact.issues.add(Issue(issueId, issueUrl, issueTitle, issueDescription, new Date, user.fullName))
    mongo.save(artifact)
  }

  // release timeline
  query5 = new Query
  query5.addCriteria(Criteria.where("artifactScmDetail.artifactId").is(artifact.artifactId))
  val versionMap = mongo.find(query5, classOf[ChangeSet]).toList.groupBy(_.artifactScmDetail.version)
  val sortedByDate = versionMap.mapValues(_.sortBy(_.commitDate).map(_.commitDate))

  timelineModel = new TimelineModel()

  sortedByDate.foreach(tuple => timelineModel.add(new TimelineEvent(tuple._1, tuple._2.get(tuple._2.size - 1))))

  // diff (only svn implemented as of now. TODO improve this.
  def diff(cs: ChangeSet, cse: ChangeSetEntry) = {
    println("Diff ==> "+httpRequest.getServerName())
    val currRevision = cs.revision
    val currFile = cse.file
    val fullFilepath = Config.getSvnBaseUrl + "/" + currFile
    // TODO Get old and new contents.
    val prevRevision = SvnUtil.getPrevRevision(fullFilepath, currRevision)
    
    var prevContentLines = List[String]()
    var currContentLines = List[String]()
    
    if(prevRevision != -1) {
      val prevAndCurrContents = SvnUtil.getPrevAndCurrRevisionFileContents(fullFilepath, prevRevision, currRevision)
      prevContentLines = prevAndCurrContents._1.split("\n").toList
      currContentLines = prevAndCurrContents._2.split("\n").toList
    } else {
      prevContentLines = Source.fromFile("c:/pumpkinx/data/CreateReleaseController1.scala").getLines.toList
      currContentLines = Source.fromFile("c:/pumpkinx/data/CreateReleaseController2.scala").getLines.toList
    }

    println("--- " + diffSource)

    val title = currFile+" (Prev revision: "+prevRevision+", Curr revision: "+currRevision+" )"
    val options = new java.util.HashMap[String, Object]
    options.put("modal", java.lang.Boolean.TRUE);
    options.put("draggable", java.lang.Boolean.TRUE);
    options.put("resizable", java.lang.Boolean.TRUE);
    options.put("contentHeight", "'100%'");
    options.put("contentWidth", "'100%'");
    options.put("maximizable", java.lang.Boolean.TRUE);
    
    options.put("height", "500");
    options.put("width", "700")

    val diff = DiffGen.generateDiffHtmls(prevContentLines, currContentLines)
    prevContent = diff._1
    currContent = diff._2
    diffSource.prevContent = prevContent
    diffSource.currContent =  currContent
    diffSource.prevRevision = prevRevision
    diffSource.currRevision = currRevision
    diffSource.currFile = currFile
    val params = new java.util.HashMap[String, java.util.List[String]]
    RequestContext.getCurrentInstance().openDialog("diffSource", options, new java.util.HashMap);
  }
} 
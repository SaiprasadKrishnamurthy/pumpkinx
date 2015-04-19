package org.sai.pumpkinx.ui.beans

import java.util.ArrayList
import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.sai.pumpkinx.ui.config.Config
import api.FeatureDetail
import api.SourceFileDetail
import pumpkinx.api.ArtifactConfig
import pumpkinx.api.ArtifactDetail
import pumpkinx.api.ArtifactScmDetail
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import org.primefaces.context.RequestContext
import javax.faces.application.FacesMessage
import api.ChangeSet
import api.ChangeSet
import api.ChangeSetEntry
import org.sai.pumpkinx.ui.util.SvnUtil
import org.sai.pumpkinx.ui.util.DiffGen
import api.ChangeSet

class DiffArtifactsController {
  val mongo = Config.getMongoTemplate

  @BeanProperty
  var artifacts: java.util.List[ArtifactDetail] = new ArrayList

  @BeanProperty
  var artifact1: String = _

  @BeanProperty
  var show: Boolean = _

  @BeanProperty
  var artifact2: String = _

  @BeanProperty
  var deltas: java.util.List[String] = _

  @BeanProperty
  var committers: java.util.List[String] = _

  @BeanProperty
  var changeSets: java.util.List[ChangeSet] = new ArrayList

  @BeanProperty
  var entries: java.util.List[String] = new ArrayList

  var firstScmDetail: ArtifactScmDetail = _

  var secondScmDetail: ArtifactScmDetail = _

  @BeanProperty
  var prevContent: String = _

  @BeanProperty
  var currContent: String = _

  @BeanProperty
  var diffSource: DiffSource = _

  val allArtifacts = mongo.findAll(classOf[ArtifactDetail])

  def onQuery(query: String) = {
    val filtered = allArtifacts.filter(a => a.artifactId.toLowerCase.startsWith(query.toLowerCase) || a.version.startsWith(query)).map(a => a.artifactId + " (" + a.version + ") ")
    new java.util.ArrayList(filtered)
  }

  def diff() = {
    committers = new ArrayList
    changeSets.clear
    entries.clear
    show = false
    println("Artifact 1: => " + artifact1)
    println("Artifact 2: => " + artifact2)

    val a1 = artifact1.trim
    val a2 = artifact2.trim

    val firstArtifactId = a1.substring(0, a1.indexOf("(")).trim
    val secondArtifactId = a2.substring(0, a2.indexOf("(")).trim

    val firstVersion = a1.substring(a1.indexOf("(") + 1, a1.length - 1).trim
    val secondVersion = a2.substring(a2.indexOf("(") + 1, a2.length - 1).trim

    println("Artifact 1: => " + artifact1)
    println("Artifact 2: => " + artifact2)
    println("First aid1: => " + firstArtifactId)
    println("First version: => " + firstVersion)
    println("SEcond aid: => " + secondArtifactId)
    println("SEcond versiont: => " + secondVersion)

    var query = new Query()
    query.addCriteria(Criteria.where("artifactId").is(firstArtifactId).andOperator(Criteria.where("version").is(firstVersion)))
    val first = mongo.find(query, classOf[ArtifactDetail])

    query = new Query()
    query.addCriteria(Criteria.where("artifactId").is(secondArtifactId).andOperator(Criteria.where("version").is(secondVersion)))
    val second = mongo.find(query, classOf[ArtifactDetail])

    if (first.get(0).artifactId.equals(second.get(0).artifactId) == false) {
      RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Different versions of the same artifact can only be compared!"))
    } else {
      val firstDeps = first.get(0).children
      val secondDeps = second.get(0).children

      // modified
      val modifiedDeps = secondDeps.filter(curr => firstDeps.filter(pre => pre.artifactId.equals(curr.artifactId) && !pre.version.equals(curr.version)).size > 0)

      // added.
      val addedDeps = secondDeps.filter(curr => firstDeps.filter(pre => pre.artifactId.equals(curr.artifactId)).size == 0)

      // removed.
      val removedDeps = secondDeps.filter(pre => firstDeps.filter(curr => curr.artifactId.equals(pre.artifactId)).size == 0)
      deltas = new ArrayList
      deltas.addAll(addedDeps.map(ad => "[DEPENDENCY ADDED] " + ad.groupId + " | " + ad.artifactId + " | " + ad.version))
      deltas.addAll(removedDeps.map(ad => "[DEPENDENCY REMOVED] " + ad.groupId + " | " + ad.artifactId + " | " + ad.version))
      deltas.addAll(modifiedDeps.map(ad => "[DEPENDENCY MODIFIED] " + ad.groupId + " | " + ad.artifactId + " From: " + firstDeps.filter(p => p.groupId.equals(ad.groupId) && p.artifactId.equals(ad.artifactId)).get(0).version + " To:  " + ad.version))
      show = true

      // Author deltas.
      var query = new Query()
      query.addCriteria(Criteria.where("artifactId").is(firstArtifactId).andOperator(Criteria.where("version").is(firstVersion)))
      firstScmDetail = mongo.find(query, classOf[ArtifactScmDetail]).get(0)

      query = new Query()
      query.addCriteria(Criteria.where("artifactId").is(firstArtifactId).andOperator(Criteria.where("version").is(secondVersion)))
      secondScmDetail = mongo.find(query, classOf[ArtifactScmDetail]).get(0)

      val firstVersionSequence = firstScmDetail.versionSequence
      val secondVersionSequence = secondScmDetail.versionSequence

      val from = Math.min(firstVersionSequence, secondVersionSequence)
      val to = Math.max(firstVersionSequence, secondVersionSequence)
      println("From: " + from + " To: " + to)

      for (i <- from to to) {
        query = new Query()
        query.addCriteria(Criteria.where("artifactScmDetail.artifactId").is(firstArtifactId).andOperator(Criteria.where("artifactScmDetail.versionSequence").is(i)))
        val _changeSets = mongo.find(query, classOf[ChangeSet])
        val _committers = changeSets.map(_.committer)
        committers.addAll(_committers)
        changeSets.addAll(_changeSets)
        entries.addAll(_changeSets.flatMap(_.entries).map(_.file))
      }
      committers = committers.distinct
      entries = entries.distinct
    }

  }

  def viewFileDiff(fileName: String) = {
    println("Main file: " + fileName)
    def getMinAndMaxRevisionForFile(changeSets: List[ChangeSet] ) = {
      println(changeSets.flatMap(cs => cs.entries).map(_.file).mkString("\n"))
      val relevantChangeSets = changeSets.filter(cs => cs.entries.filter(cse => { println("FIle: " + cse.file); cse.file.equals(fileName) }).isEmpty == false)
      val sorted = relevantChangeSets.sortBy(cs => cs.revision)
      (sorted(1).revision,sorted.last.revision) 
    }

    

    // Get all the changesets between the first and second.
    val query = new Query()
    query.addCriteria(Criteria.where("artifactScmDetail.artifactId").is(firstScmDetail.artifactId).andOperator(Criteria.where("artifactScmDetail.versionSequence").gte(firstScmDetail.versionSequence), Criteria.where("artifactScmDetail.versionSequence").lte(secondScmDetail.versionSequence)))
    
    val allChangeSetsInRange = Config.getMongoTemplate.find(query, classOf[ChangeSet]).toList 
    
    val minAndMax = getMinAndMaxRevisionForFile(allChangeSetsInRange)
    val firstRevision = minAndMax._1
    val secondRevision = minAndMax._2
    
    val fullFilepath = Config.getSvnBaseUrl + "/" + fileName

    println("File: " + fileName)
    println("First revision: " + firstRevision)
    println("Second revision: " + secondRevision)
    println("Full file path: " + fullFilepath)

    val prevAndCurrContents = SvnUtil.getPrevAndCurrRevisionFileContents(fullFilepath, firstRevision, secondRevision)

    val prevContentLines = prevAndCurrContents._1.split("\n").toList
    val currContentLines = prevAndCurrContents._2.split("\n").toList
    val title = fileName + " (Prev revision: " + firstRevision + ", Curr revision: " + secondRevision + " )"
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
    diffSource.currContent = currContent
    diffSource.prevRevision = firstRevision
    diffSource.currRevision = secondRevision
    diffSource.currFile = fileName
    val params = new java.util.HashMap[String, java.util.List[String]]
    RequestContext.getCurrentInstance().openDialog("diffSource", options, new java.util.HashMap);

  }
} 
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
import org.primefaces.model.chart.CartesianChartModel
import java.util.Date
import api.ChangeSet
import org.primefaces.model.chart.ChartSeries
import java.text.SimpleDateFormat

class ChartsController {

  val mongo = Config.getMongoTemplate
  val httpRequest = FacesContext.getCurrentInstance().getExternalContext().getRequest().asInstanceOf[HttpServletRequest]
  val httpResponse = FacesContext.getCurrentInstance().getExternalContext().getResponse().asInstanceOf[HttpServletResponse]

  val dateFormat = new SimpleDateFormat("dd-MMM-yy")
  @BeanProperty
  var topCommitters: CartesianChartModel = _

  @BeanProperty
  var committersBarWidth: Int = _

  @BeanProperty
  var topReleases: CartesianChartModel = _

  @BeanProperty
  var commitTrend: CartesianChartModel = _

  @BeanProperty
  var topReleasesBarWidth: Int = _

  val fifteenDaysInMillis = 16 * 24 * 60 * 60 * 1000

  val fifteenDaysAgo = new Date(System.currentTimeMillis() - fifteenDaysInMillis)
  val query = new Query(Criteria.where("commitDate").gte(fifteenDaysAgo))
  val changesets = mongo.find(query, classOf[ChangeSet])
  val committers = changesets.toList.groupBy(cs => cs.committer)

  topCommitters = new CartesianChartModel

  val series = new ChartSeries
  series.setLabel("Number of committers in last 15 days (based on code checkins)")
  committers.foreach(committerTuple => {
    if (Config.excludeAuthors.contains(committerTuple._1) == false) {
      committersBarWidth = committersBarWidth + 25
      series.set(committerTuple._1, committerTuple._2.size)
    }
  })
  topCommitters.addSeries(series)

  topReleases = new CartesianChartModel
  val artifactScms = changesets.map(cs => cs.artifactScmDetail.artifactId + "|" + cs.artifactScmDetail.version).toList.distinct

  val artifacts = artifactScms.groupBy(token => token.substring(0, token.indexOf("|")))

  val series1 = new ChartSeries
  series1.setLabel("Number of maven releases in last 15 days")
  artifacts.foreach(committerTuple => {
    topReleasesBarWidth = topReleasesBarWidth + 25
    series1.set(committerTuple._1, committerTuple._2.size)
  })
  topReleases.addSeries(series1)

  commitTrend = new CartesianChartModel

  val changeSetsPerDate = changesets.toList.groupBy(cs => dateFormat.format(cs.commitDate))

  val series2 = new ChartSeries
  val keysSorted = changeSetsPerDate.keySet.toList.sortBy(_.toString)
  series2.setLabel("Number code commits in last 15 days")
  keysSorted.foreach(key => {
    println(key + " ==> "+changeSetsPerDate.get(key).size)
    series2.set(key, changeSetsPerDate.get(key).get.size)
  })
  commitTrend.addSeries(series2)
}
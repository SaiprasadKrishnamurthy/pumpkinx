package org.sai.pumpkinx.ui.config

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import com.mongodb.MongoClient
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl

/**
 * Created by Kumar on 30/03/2015.
 */
object Config {

  val mongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")

  //val mongo = new SimpleMongoDbFactory(new MongoClient("10.70.101.135"), "pumpkinx")

  // This is just an aggregator pom and will simply list the children which are actually releasable.
  def getAggregatorArtifactCategory = {
    "project"
  }

  val mongoTemplate = new MongoTemplate(mongo);

  def getMongoTemplate = {
    mongoTemplate
  }

  val authManager = new BasicAuthenticationManager("skrishnamurthy", "002632");
  DAVRepositoryFactory.setup();

  SVNRepositoryFactoryImpl.setup();

  def getSvnAuthManager = authManager

  def getSvnBaseUrl = "http://10.70.101.200:7777/svn/gsl"

  val mapCenterCoordinates = "27.370833,62.334167"

  val locationsMap = Map("Aldershot, UK" -> (51.248366, -0.755751),
    "Hayes Middlesex (London Gate), UK" -> (51.520998, -0.416057),
    "Bangalore, India" -> (12.971599, 77.594563),
    "Muscat, Oman" -> (23.610000, 58.540000),
    "Delhi, India" -> (28.613939, 77.209021),
    "Sydney, Australia" -> (-33.867487, 151.206990),
    "Atlanta, USA" -> (33.748995, -84.387982),
    "Doha, Qatar" -> (25.291610, 51.530437),
    "Letterkenny, Ireland" -> (54.955839, -7.734279),
    "Dubai, UAE" -> (25.204849, 55.270783),
    "Riyadh, Saudi Arabia" -> (24.633333, 46.716667))
  val locations = locationsMap.keys
  
  val excludeAuthors = List("ccbuilder")

  /*  def getEmailSender = system.actorOf(Props[EmailActor], "emailActor")
  system.awaitTermination()*/
}

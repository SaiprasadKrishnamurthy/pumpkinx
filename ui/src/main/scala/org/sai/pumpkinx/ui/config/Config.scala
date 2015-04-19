package org.sai.pumpkinx.ui.config

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import com.mongodb.MongoClient
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl
import akka.actor.ActorSystem
import akka.actor.Props
import email.actors.EmailActor

/**
 * Created by Kumar on 30/03/2015.
 */
object Config {

  val mongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")
  
  val remoteMongo = new SimpleMongoDbFactory(new MongoClient("10.70.101.135"), "pumpkinx")
   
  
  // This is just an aggregator pom and will simply list the children which are actually releasable.
  def getAggregatorArtifactCategory = {
    "project"
  }

  val mongoTemplate = new MongoTemplate(remoteMongo);

  def getMongoTemplate = {
    mongoTemplate
  }

  val authManager = new BasicAuthenticationManager("skrishnamurthy", "002632");
  DAVRepositoryFactory.setup();

  SVNRepositoryFactoryImpl.setup();
  
  def getSvnAuthManager = authManager
  
  def getSvnBaseUrl = "http://10.70.101.200:7777/svn/gsl"
    
  // Start notifications actor system.
  val system = ActorSystem("pumpkinx-ui-akka")
  
/*  def getEmailSender = system.actorOf(Props[EmailActor], "emailActor")
  system.awaitTermination()*/
}

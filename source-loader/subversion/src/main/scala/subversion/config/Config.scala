package subversion.config

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import com.mongodb.MongoClient
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl

/**
 * Created by Sai on 30/03/2015.
 */
object Config {

  val mongo = new SimpleMongoDbFactory(new MongoClient(), "pumpkinx")

  val mongoTemplate = new MongoTemplate(mongo);

  val authManager = new BasicAuthenticationManager("skrishnamurthy", "002632");
  DAVRepositoryFactory.setup();

  SVNRepositoryFactoryImpl.setup();

  def getParallelSourceLoads = 10000

  def getMongoTemplate = mongoTemplate

  def getSvnAuthManager = authManager

  def getRealSvnUrl(url: String) = url.replace("#{gsl.repo}", "http://10.70.101.200:7777/svn/gsl")
}

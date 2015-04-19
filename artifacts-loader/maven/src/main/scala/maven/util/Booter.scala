package maven.util

import maven.config.Config
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.RepositorySystem
import org.springframework.stereotype.Component
import org.eclipse.aether.repository.LocalRepository
import org.springframework.beans.factory.annotation.Autowired
import org.eclipse.aether.repository.RemoteRepository
import java.util.UUID
import java.io.File

object Booter {


  def getRepositorySystemSession() = {
    // This will return a string. 
    val localRepoDirectory = System.getProperty("user.home") + File.separator + "pumpkinrepo1"
    val session = MavenRepositorySystemUtils.newSession()
    val localRepo = new LocalRepository(localRepoDirectory)
    session.setLocalRepositoryManager(getRepositorySystem.newLocalRepositoryManager(session, localRepo))
    session
  }

  def getRepositories() = {
       Config.getMavenRepoUrls.map(url => new RemoteRepository.Builder(UUID.randomUUID().toString(), "default", url).build())
  }

  def getRepositorySystem() = {
    val locator = MavenRepositorySystemUtils.newServiceLocator()
    locator.addService(classOf[RepositoryConnectorFactory], classOf[BasicRepositoryConnectorFactory])
    locator.addService(classOf[TransporterFactory], classOf[FileTransporterFactory])
    locator.addService(classOf[TransporterFactory], classOf[HttpTransporterFactory])
    locator.getService(classOf[RepositorySystem])
  }
}
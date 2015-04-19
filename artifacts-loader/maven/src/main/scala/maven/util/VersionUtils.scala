package maven.util

import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.resolution.VersionRangeRequest
import org.eclipse.aether.{RepositorySystemSession, RepositorySystem}
import scala.collection.JavaConversions._
import maven.config.Config

/**
 * Created by Kumar on 30/03/2015.
 */
object VersionUtils {

  def getAllAvailableVersions(groupId: String, artifactId: String) = {

    val session = Booter.getRepositorySystemSession()

    val artifact = new DefaultArtifact(s"$groupId:$artifactId:[0,)")

    val rangeRequest = new VersionRangeRequest()
    rangeRequest.setArtifact(artifact)
    rangeRequest.setRepositories(Booter.getRepositories.toList)

    val rangeResult = Booter.getRepositorySystem().resolveVersionRange(session, rangeRequest)

    val versions = rangeResult.getVersions()
    versions.filter(_.toString().contains("-SNAPSHOT") == false)
  }
}

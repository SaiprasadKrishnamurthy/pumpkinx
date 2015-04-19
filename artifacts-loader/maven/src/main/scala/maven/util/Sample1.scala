package maven.util

import maven.config.Config

/**
 * Created by Kumar on 30/03/2015.
 */
object Sample1 {

  def main(args: Array[String]) {
    println(VersionUtils.getAllAvailableVersions("aero.sita.gsl.security", "security-presentation-client"))
  } 
}

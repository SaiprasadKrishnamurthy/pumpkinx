package scratchpad

import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.SVNURL
import subversion.config.Config
import scala.collection.JavaConversions._
import org.tmatesoft.svn.core.SVNDirEntry
import java.util.Collection
import java.io.ByteArrayOutputStream
import org.tmatesoft.svn.core.SVNNodeKind
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.SVNLogEntry

object SvnListFile {

  def main(args: Array[String]) {

    def list(repo: SVNRepository, path: String, accumulated: collection.mutable.Buffer[String]): collection.mutable.Buffer[String] = {
      println(path)
      val dirs = repo.getDir(path, -1, null, null.asInstanceOf[Collection[_]])

      dirs.foreach(dir => {
        val entry = dir.asInstanceOf[SVNDirEntry]
        if (entry.getKind() == SVNNodeKind.DIR) {
          list(repo, if (path.equals("")) entry.getName() else path + "/" + entry.getName(), accumulated)
        } else {
          accumulated.add(entry.getURL().toDecodedString())
        }
      })
      accumulated
    }

    def getAllFiles(svnUrl: String) = {
    }

    def getAllRevisions(svnUrl: String) = {
      val fileName = svnUrl.substring(svnUrl.lastIndexOf("/") + 1)
      val svnPath = svnUrl.substring(0, svnUrl.lastIndexOf("/"))
      val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnPath))
      repository.setAuthenticationManager(Config.getSvnAuthManager)
      val logEntries = repository.log(Array(fileName), null, 0, -1, true, true)
      logEntries.map(log => {
        val entry = log.asInstanceOf[SVNLogEntry]
        entry.getRevision()
      }).toList
    }

    def getPrevRevision(svnFileUrl: String, currRevision: Long) = {
      val allRevisions = getAllRevisions(svnFileUrl)
      println(allRevisions)
      val currIndex = allRevisions.indexOf(currRevision)
      if (currIndex > 0) allRevisions(allRevisions.indexOf(currRevision) - 1) else -1
    }
  }
}
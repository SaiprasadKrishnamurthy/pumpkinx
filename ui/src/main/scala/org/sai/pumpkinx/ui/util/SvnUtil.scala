package org.sai.pumpkinx.ui.util

import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.SVNURL
import org.sai.pumpkinx.ui.config._
import scala.collection.JavaConversions._
import org.tmatesoft.svn.core.SVNLogEntry
import java.io.StringWriter
import java.io.ByteArrayOutputStream

object SvnUtil {
  
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
    
    def getPrevAndCurrRevisionFileContents(svnFilePath: String, prevRevision: Long, currRevision: Long) = {
      var out = new ByteArrayOutputStream
      val fileName = svnFilePath.substring(svnFilePath.lastIndexOf("/") + 1)
      val svnPath = svnFilePath.substring(0, svnFilePath.lastIndexOf("/"))
      val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnPath))
      repository.setAuthenticationManager(Config.getSvnAuthManager)
      repository.getFile(fileName, prevRevision, null, out)
      val prevContents = new String(out.toByteArray())
      out = new ByteArrayOutputStream
      repository.getFile(fileName, currRevision, null, out)
      val currContents = new String(out.toByteArray())
      (prevContents, currContents)
    }

}
package org.sai.pumpkinx.ui.util

import scala.collection.JavaConversions._
import difflib.DiffUtils
import scala.io.Source
import scala.collection.mutable.ListBuffer
object DiffGen {

  def generateDiffHtmls(prev: List[String], curr: List[String]) = {
    val res = DiffUtils.diff(prev, curr)

    val colorMapping = Map("INSERT" -> "#bfb", "CHANGE" -> "#fea", "DELETE" -> "#F78181")

    val currChangedLineNos, currDeletedLineNos, currAddedLineNos = new ListBuffer[Int]
    val prevChangedLineNos, prevDeletedLineNos, prevAddedLineNos = new ListBuffer[Int]

    res.getDeltas().foreach(delta => {
      if (delta.getType().toString().equals("CHANGE")) {
        currChangedLineNos += delta.getRevised().getPosition()
        prevChangedLineNos += delta.getOriginal().getPosition()
      }
      if (delta.getType().toString().equals("INSERT")) {
        currAddedLineNos ++= (delta.getRevised().getPosition() until (delta.getRevised().getPosition() + delta.getRevised().getLines().size()))
        prevAddedLineNos += delta.getOriginal().getPosition()
        
        delta.getRevised().getLines().size()
      }
      if (delta.getType().toString().equals("DELETE")) {
        currDeletedLineNos += delta.getRevised().getPosition()
        prevDeletedLineNos ++= (delta.getOriginal().getPosition() until (delta.getOriginal().getPosition() + delta.getOriginal().getLines().size()))
      }
    })

    def getTextColor(index: Int, isCurr: Boolean) = {
      val (changedLineNos, deletedLineNos, addedLineNos) = isCurr match {
        case true => (currChangedLineNos, currDeletedLineNos, currAddedLineNos)
        case _ => (prevChangedLineNos, prevDeletedLineNos, prevAddedLineNos)
      }

      if (changedLineNos.contains(index)) colorMapping.get("CHANGE").get
      else if (deletedLineNos.contains(index) && !isCurr) colorMapping.get("DELETE").get
      else if(addedLineNos.contains(index) && isCurr) colorMapping.get("INSERT").get
      else "#fff"
    }
    
    val prevContentAsHtml = <table border='0'>{ prev.indices.map(index => <tr bgcolor={ getTextColor(index, false) }><td bgcolor="gray">{ index + 1 }</td><td/><td/><td>{ prev.get(index).trim }</td></tr>) }</table>
    val currContentAsHtml = <table border='0'>{ curr.indices.map(index => <tr bgcolor={ getTextColor(index, true) }><td bgcolor="gray">{ index + 1 }</td><td/><td/><td>{ curr.get(index).trim }</td></tr>) }</table>

    (prevContentAsHtml.toString, currContentAsHtml.toString)
  }

}
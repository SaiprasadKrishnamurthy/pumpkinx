package scratchpad

import difflib.DiffUtils
import scala.collection.JavaConversions._
import scala.io.Source

object DiffUtil {

  def main(args: Array[String]): Unit = {
    val res = DiffUtils.diff(Source.fromFile("c:/pumpkinx/data/CreateReleaseController1.scala").getLines.toList, Source.fromFile("c:/pumpkinx/data/CreateReleaseController2.scala").getLines.toList)
    res.getDeltas().foreach(delta => {
      println("Original: "+delta.getOriginal().getPosition()+" : "+delta.getOriginal().getLines().mkString("\n"))
      println("New: "+delta.getRevised().getPosition()+" : "+delta.getRevised().getLines().mkString("\n"))
      println("Type: "+delta.getType())
      
      println(" -------- \n\n")
    })
  }

}
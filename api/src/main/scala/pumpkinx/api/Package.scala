package pumpkinx.api

import scala.beans.BeanProperty

case class Package(@BeanProperty name: String, @BeanProperty location: String, @BeanProperty packageType: String = "rpm") {
  override def toString = {
    name+"\n"
  }
}
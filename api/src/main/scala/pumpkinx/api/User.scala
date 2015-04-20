package pumpkinx.api

import scala.beans.BeanProperty

/**
 * Created by Kumar on 30/03/2015.
 */
case class User(@BeanProperty fullName: String, @BeanProperty userId: String, @BeanProperty password: String, @BeanProperty email: String)


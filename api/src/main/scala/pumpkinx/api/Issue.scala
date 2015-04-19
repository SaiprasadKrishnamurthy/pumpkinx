package pumpkinx.api

import scala.beans.BeanProperty
import java.util.Date

/**
 * Created by Kumar on 30/03/2015.
 */
case class Issue(@BeanProperty issueId: String, @BeanProperty issueUrl: String, @BeanProperty issueTitle: String, @BeanProperty issueDescription: String, @BeanProperty reportedDate: Date, @BeanProperty reporter: String)


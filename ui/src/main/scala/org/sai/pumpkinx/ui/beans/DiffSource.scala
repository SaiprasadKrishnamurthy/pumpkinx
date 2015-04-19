package org.sai.pumpkinx.ui.beans

import scala.beans.BeanProperty

class DiffSource {

  @BeanProperty
  var prevContent: String = _

  @BeanProperty
  var currContent: String = _

  @BeanProperty
  var prevRevision: Long = _

  @BeanProperty
  var currRevision: Long = _

  @BeanProperty
  var currFile: String = _

}
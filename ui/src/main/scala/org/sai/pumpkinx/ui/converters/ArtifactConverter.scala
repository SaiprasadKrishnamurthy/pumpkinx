package org.sai.pumpkinx.ui.converters

import javax.faces.component.UIComponent
import javax.faces.context.FacesContext
import javax.faces.convert.Converter
import org.sai.pumpkinx.ui.config.Config
import pumpkinx.api.ArtifactDetail
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import javax.faces.convert.FacesConverter

class ArtifactConverter extends Converter {

  val mongo = Config.getMongoTemplate

  override def getAsObject(fc: FacesContext, uic: UIComponent, value: String) = {
    println("getAsObject ==> "+value)
    if (value != null && value.trim().length() > 0) {
      val artifactId = value.substring(0, value.indexOf("(")).trim
      val version = value.substring(value.indexOf("(") + 1, value.indexOf(")")).trim
      var query5 = new Query()
      query5.addCriteria(Criteria.where("artifactId").is(artifactId).andOperator(Criteria.where("version").is(version)))
      val _artifact = mongo.find(query5, classOf[ArtifactDetail])
      if (_artifact.isEmpty() == false) _artifact.get(0)
    }
    null
  }

  override def getAsString(fc: FacesContext, uic: UIComponent, obj: Object) = {
    if (obj != null) {
      val arti = obj.asInstanceOf[ArtifactDetail]
      arti.artifactId + " (" + arti.version + ") "
    }
    null
  }
}
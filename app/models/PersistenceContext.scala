package models

import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.storage.StorageFactory
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage

object AppContext extends ActivateContext{
  
  val factory = play.api.Play.current.configuration.getString("activate.storage.factory").
		  			getOrElse("")
  val driver = play.api.Play.current.configuration.getString("activate.storage.jdbcDriver").
  					getOrElse("")
  val user = play.api.Play.current.configuration.getString("activate.storage.user").
  					getOrElse("")
  val password = play.api.Play.current.configuration.getString("activate.storage.password").
  					getOrElse("")
  val url = play.api.Play.current.configuration.getString("activate.storage.url").
  					getOrElse("")
  val dialect = play.api.Play.current.configuration.getString("activate.storage.dialect").
  					getOrElse("")

  System.getProperties.put("activate.storage.default.factory", factory)
  System.getProperties.put("activate.storage.default.jdbcDriver", driver)
  System.getProperties.put("activate.storage.default.user", user)
  System.getProperties.put("activate.storage.default.password", password)
  System.getProperties.put("activate.storage.default.url", url)
  System.getProperties.put("activate.storage.default.dialect", dialect)
  
  val storage = StorageFactory.fromSystemProperties("default")      
}
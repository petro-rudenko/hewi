import play.api._
import play.api.mvc._
import play.filters.gzip.GzipFilter
import com.typesafe.config.ConfigFactory

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {
	//val registeredApps = ConfigFactory.load("apps.conf")
}

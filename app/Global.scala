import play.api._
import play.api.mvc._
import play.filters.gzip.GzipFilter
import com.typesafe.config.ConfigFactory
import play.filters.csrf.CSRFFilter

object Global extends WithFilters(new GzipFilter(), CSRFFilter()) with GlobalSettings {
	//val registeredApps = ConfigFactory.load("apps.conf")
}

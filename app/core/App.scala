package core

import auth.AuthConfigImpl
import jp.t2v.lab.play2.auth.AuthElement
import jp.t2v.lab.play2.stackc._
import models.AppContext._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.templates.Html
import scala.concurrent.Future
import views._



trait App extends StackableController {
  self: Controller with AuthElement with AuthConfigImpl =>
    
    type Template = String => Html => Html
    
  /*val name: String
  
  implicit def appName[A](implicit request: Request[A]) : String = name
  implicit def submenu[A](implicit request: Request[A]) : List[MenuItem]  
  */
  implicit def user[A](implicit request: RequestWithAttributes[A])  = transactional {loggedIn(request)}

  case object TemplateKey extends RequestAttributeKey[Template]
    
  abstract override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[SimpleResult]): Future[SimpleResult] = {
    transactional{
    	val template: Template = html.main.apply(loggedIn(req))
    	super.proceed(req.set(TemplateKey, template))(f)
    }
  }
    
  implicit def template[A](implicit req: RequestWithAttributes[A]): Template = req.get(TemplateKey).get
}

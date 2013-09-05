package applications

import jp.t2v.lab.play2.stackc.StackableController
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc._
import auth.AuthConfigImpl



case class MenuItem(url: String, name: String){  
  def isActive = false
}

trait App extends StackableController with AuthElement with AuthConfigImpl{
  self: Controller with AuthElement =>
  /*val name: String
  
  implicit def appName[A](implicit request: Request[A]) : String = name
  implicit def submenu[A](implicit request: Request[A]) : List[MenuItem]  
  */
}
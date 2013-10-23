package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.Future
import views._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Execution.Implicits._
import auth.{AuthProvider, AuthConfigImpl}
import models.User
import jp.t2v.lab.play2.auth.{LoginLogout, OptionalAuthElement}
import models.AuthType._


object Application extends Controller with OptionalAuthElement with LoginLogout with AuthConfigImpl{
  
  val loginForm = Form {
    mapping(
        "username" -> nonEmptyText, 
        "password" -> nonEmptyText
        )(AuthProvider.getProvider.authenticate)(_.map(u => (u.username, u.password.get))).
    		verifying("Invalid username or password", result => result.isDefined)
  }
 
  
  def login = AsyncStack { implicit request => 
    val maybeUser: Option[User] = loggedIn
    maybeUser match {
      case Some(user) => gotoLoginSucceeded(maybeUser.get.id)
      case None => Future.successful(Ok(html.login(loginForm)))
    }    
  }  
  
  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def authenticate = Action.async {
    implicit request => loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.login(formWithErrors))),
        user         => gotoLoginSucceeded(user.get.id)
    )
  }
  
}

package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import jp.t2v.lab.play2.auth.AuthElement
import core.App
import auth._
import scala.concurrent.Future
import net.fwbrasil.activate.play.EntityForm
import views._
import models.{User, UserStatusEncoder}
import models.AppContext._

object UserAdmin extends Controller with App with AuthElement with AuthConfigImpl {

  val encoder = new UserStatusEncoder()

  val userForm = EntityForm[User](
    _.username -> nonEmptyText,
    _.status -> mapping("status" -> boolean)(b => (if(b) SuperUser else NormalUser))(u => u match {case SuperUser => Some(true); case _ => None}),
    _.password -> tuple(
      "main" -> nonEmptyText,
      "confitm" -> nonEmptyText
    ).verifying(
      "Passwords don't match", passwords => passwords._1 == passwords._2
    ),
    _.email -> optional(email),
    _.fullName -> optional(text)
  )

  def index (page: Int, orderBy: Int, filter: String) = AsyncStack(AuthorityKey -> SuperUser){
    implicit request => asyncTransactionalChain {
      implicit ctx =>
      User.list(page, orderBy, filter = ("*" + filter + "*")).map(
        page => Ok(views.html.useradmin.index(page, orderBy, filter))
      )
    }
  }

  //Json Handler
  def list (page: Int, orderBy: Int, filter: String)= AsyncStack(AuthorityKey -> SuperUser){
    implicit request => asyncTransactionalChain {
      implicit ctx =>
      User.listJson(page, orderBy, filter = ("*" + filter + "*")).map(
        users => Ok(users)
      )
    }
  }

  def addUser = AsyncStack(AuthorityKey -> SuperUser){
    implicit request =>
    Future.successful(Ok(views.html.useradmin.create(userForm)))
  }

  /**
    * Handles addUser form submition
    * */
  def save = AsyncStack(AuthorityKey -> SuperUser){
    implicit request => Future.successful(Ok("TODO"))
  }


  def editUser(id: String) = AsyncStack(AuthorityKey -> SuperUser){
    implicit request => Future.successful(Ok("TODO"))
  }

  /**
    * Handles editUser form submition
    * @param User Id to update
    * */
  def update(id: String) = AsyncStack(AuthorityKey -> SuperUser){
    implicit request => Future.successful(Ok("TODO"))
  }

}

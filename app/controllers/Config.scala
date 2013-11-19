package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthElement
import core.App
import auth._
import views._

object Config extends Controller with App with AuthElement with AuthConfigImpl {

  def index = StackAction(AuthorityKey -> SuperUser){
    implicit request => Ok(html.config("Config"))
  }

}

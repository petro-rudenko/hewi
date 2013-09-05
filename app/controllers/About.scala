package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthElement
import auth.{AuthConfigImpl, NormalUser}
import views._


object About extends Controller with AuthElement with AuthConfigImpl{
	
  /*def index = StackAction(AuthorityKey -> NormalUser){     
    implicit request => Ok(html.about())
  }*/
  
  def index = Action{implicit request => Ok(html.about())}

  
}

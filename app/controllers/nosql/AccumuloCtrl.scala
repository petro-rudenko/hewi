package controllers.nosql

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthElement
import core.App
import auth._
import views._

object AccumuloCtrl extends Controller with App with AuthElement with AuthConfigImpl { 

  def index = StackAction(AuthorityKey -> NormalUser){     
    implicit request => Ok(views.html.nosql.accumulo.index())
  }

  
}

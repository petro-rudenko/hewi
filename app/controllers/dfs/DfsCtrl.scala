package controllers.dfs

import api.DfsApi
import api.DfsImplicits._
import auth._
import core.App
import java.io._
import jp.t2v.lab.play2.auth.AuthElement
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._
import org.apache.hadoop.fs.Path
//import play.api.libs.MimeType
import play.api.mvc._
import views._


object DfsCtrl extends Controller with App with AuthElement with AuthConfigImpl {

  def index = StackAction(AuthorityKey -> NormalUser){
    implicit request => Ok(views.html.dfs.index())
  }

  def getHomeDirectory = StackAction(AuthorityKey -> NormalUser){
    implicit request => Ok(Json.toJson(Map("homedir" -> DfsApi(loggedIn.username).getHomeDir())))
  }

  def listdir(path: String) = StackAction(AuthorityKey -> NormalUser){
    implicit request => Ok(DfsApi(loggedIn.username).listdir(path).toJson)
  }

  def download(path: String) = StackAction(AuthorityKey -> NormalUser){
    implicit request =>
    val dfs = DfsApi(loggedIn.username)
    var length: Long = 0
    try {
      val status = dfs.getFileStatus(path)
      length = status.getLen()
    }
    catch{
      case e: FileNotFoundException => NotFound(e.getMessage)
    }
    val data = dfs.open(path).asInstanceOf[java.io.InputStream]
    val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(data)
    SimpleResult(
      header = ResponseHeader(200, Map(CONTENT_LENGTH -> length.toString)),
      body = dataContent
    )
  }

}

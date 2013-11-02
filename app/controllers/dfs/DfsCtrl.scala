package controllers.dfs

import api.DfsApi
import api.DfsImplicits._
import auth._
import core.App
import java.io._
import jp.t2v.lab.play2.auth.{AsyncAuth, AuthElement}
import jp.t2v.lab.play2.stackc._
import org.apache.hadoop.fs.Path
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Done, Iteratee}
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import views._


trait UploadHandler extends Controller with AsyncAuth with AuthConfigImpl {
  import scala.concurrent.ExecutionContext.Implicits.global

  def hdfsUploader(path: String)(implicit context: ExecutionContext) = BodyParser {
    request => Iteratee.flatten(authorized(NormalUser)(request, context).map {
      case Right(user)  => parse.multipartFormData(DfsApi(user.username).hdfsUploadHandler(path))(request)
      case Left(result) => Done[Array[Byte], Either[SimpleResult, (Path, User)]](Left(result))
    })
  }


}


object DfsCtrl extends Controller with App with AuthElement with AsyncAuth  with AuthConfigImpl with UploadHandler{
  
  def index = StackAction(AuthorityKey -> NormalUser) {
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


  def upload(path: String) = authorizedAction(hdfsUploader(path), NormalUser){
    user => implicit request => Ok("Uploaded")
  }

}

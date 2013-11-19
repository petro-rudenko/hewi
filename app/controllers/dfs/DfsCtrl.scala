package controllers.dfs

import api.DfsApi
import api.DfsImplicits._
import auth._
import core.App
import java.io._
import java.util.zip._
import jp.t2v.lab.play2.auth.{AsyncAuth, AuthElement}
import jp.t2v.lab.play2.stackc._
import org.apache.hadoop.fs.Path
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Done, Iteratee}
import play.api.libs.json._
import play.api.mvc._
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration._
import views._
import play.api.libs.functional.syntax._


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

  def index = AsyncStack(AuthorityKey -> NormalUser) {
    implicit request => 
    Future.successful(Redirect(routes.DfsCtrl.browse(DfsApi(loggedIn.username).homeDir())))
  }

  def browse(path: String) = AsyncStack(AuthorityKey -> NormalUser) {
    implicit request => Future.successful(Ok(views.html.dfs.index()))
  }

  def listdir(path: String) = AsyncStack(AuthorityKey -> NormalUser){
    implicit request => Future.successful(
      try{
        Ok(DfsApi(loggedIn.username).listdir(path).toJson)
      }
      catch {
        case ex: FileNotFoundException => NotFound(ex.getMessage)
      }
    )
  }


  /** 
    * Concats files and stream back them as a single file.
    *  E.g. File1 = "File 1 content", File2 = "File2 content"
    *  concat(File1::File2) = "File1 content\nFile2 content"
    */ 
  def concat(files: String) = AsyncStack(AuthorityKey -> NormalUser){
    implicit request =>
    val paths = files split "::"
    if (paths.length < 2){
      Future.successful(BadRequest("Concat accepts 2 or more files"))
    }
    else {
      val dfs = DfsApi(loggedIn.username)
      val enumerators = paths map (p => Enumerator.fromStream(dfs.open(p).asInstanceOf[java.io.InputStream])) 
      val enumerator = enumerators.foldLeft(Enumerator.empty[Array[Byte]]){(enum, p) =>
        enum >>> p
      }
      Future.successful(
        Ok.chunked(enumerator >>> Enumerator.eof).withHeaders(
        "Content-Disposition"->"attachment; filename=concat.txt"
      ))
    }
  }

  /** 
    * If requested single file - it would download only this file,
    * otherwise it would pack and stream all requested files/directories in gzip format.
    */ 
  def download(files: String) = AsyncStack(AuthorityKey -> NormalUser){
    implicit request =>
    val dfs = DfsApi(loggedIn.username)
    val paths = files split "::"
    if (paths.length == 1) {
      val p = paths.head
      try {
        val status = dfs.fileStatus(p)
        if (status.isFile) downloadFile(p, dfs, status.getLen())
        else downloadGzip(Seq(p), dfs)
      }
      catch{
        case e: FileNotFoundException => Future.successful(NotFound(e.getMessage))
      }
    }
    else downloadGzip(paths, dfs)
  }

  def downloadFile(path: String, dfs: DfsApi, length: Long) = {
    val data = dfs.open(path).asInstanceOf[java.io.InputStream]
    val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(data)
    Future.successful(
      SimpleResult(
        header = ResponseHeader(200, Map(CONTENT_LENGTH -> length.toString)),
        body = dataContent
      )
    )
  }

  def downloadGzip(paths: Seq[String], dfs: DfsApi) = {
    @tailrec
    def writeGZipRecursively(pathAcc: List[Path], zip: ZipOutputStream): Unit = {
      
      def addDir(p: String) = {
        zip.putNextEntry(new ZipEntry(p.tail + "/"))
        zip.closeEntry();
      }

      def addFile(p: Path) = {
        val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(dfs.open(p))
        val it = Iteratee.fold[Array[Byte],Unit](zip.putNextEntry(new ZipEntry(p.toString.tail))) {(_, bytes) =>
          zip.write(bytes)
        }
        Await.result(dataContent.run(it) map (_ => zip.closeEntry()), 1000.seconds)
      }

      if (pathAcc.isEmpty) return
      val currentPath = Path.getPathWithoutSchemeAndAuthority(pathAcc.head)
      val status = try{
        dfs.fileStatus(currentPath)
      } catch{//If error occurred - it would continue to stream gzip with file marked as error.
        case ex : Throwable => {
          zip.putNextEntry(new ZipEntry(currentPath.toString.tail + "_ERROR"))
          zip.write(ex.getMessage.toCharArray.map(_.toByte));
          zip.closeEntry();
          return;  
        }
      }
      if (status.isFile) {
        addFile(currentPath)
        writeGZipRecursively(pathAcc.tail, zip)
      }
      else {
        addDir(currentPath.toString)
        val content = dfs.listdir(currentPath)
        content filter (_.isFile) foreach (c => addFile(Path.getPathWithoutSchemeAndAuthority(c.getPath)))
        writeGZipRecursively(pathAcc.tail ::: (content filter (_.isDirectory) map (_.getPath)).toList, zip)
      }

    }

    val enumerator = Enumerator.outputStream { os =>
      val zip = new ZipOutputStream(os);
      paths foreach {p => writeGZipRecursively(List(p), zip) }
      zip.close
    }
    Future.successful(
      Ok.chunked(enumerator >>> Enumerator.eof).withHeaders(
        "Content-Type"->"application/zip",
        "Content-Disposition"->"attachment; filename=hdfs.zip"
      )
    )
  }

  def upload(path: String) = authorizedAction(hdfsUploader(path), NormalUser){
    user => implicit request => Ok("Uploaded")
  }

}

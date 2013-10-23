package api

import DfsImplicits._
import java.net.URI
import java.security.PrivilegedExceptionAction
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.apache.hadoop.security.UserGroupInformation
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._

object DfsImplicits{
  import scala.language.implicitConversions
  implicit def String2Path(path: String): Path = new Path(path)
  implicit def String2ArrayBytes (st: String): Array[Byte] = st.getBytes

  implicit class FileStatusJson(fs: FileStatus) {
    def toJson = JsObject(Seq(
      "path" -> JsString(fs.getPath.toString),
      "isDirectory" -> JsBoolean(fs.isDirectory),
      "modification_time" -> JsNumber(fs.getModificationTime),
      "access_time" -> JsNumber(fs.getAccessTime),
      "owner" -> JsString(fs.getOwner),
      "group" -> JsString(fs.getGroup),
      "permission" -> JsString(fs.getPermission.toString),
      "isSymlink" -> JsBoolean(fs.isSymlink),
      "replication" -> JsNumber(fs.getReplication),
      "length" -> JsNumber(fs.getLen())
    ))
  }

  implicit class FSStatusJson(fs: FsStatus) {
    def toJson = JsObject(Seq(
      "capacity" -> JsNumber(fs.getCapacity()),
      "remaining" -> JsNumber(fs.getRemaining()),
      "used" -> JsNumber(fs.getUsed())
    ))
  }
x
  implicit class FsArrayJson(files: Array[FileStatus]){
    def toJson = JsArray(files map(_.toJson))
  }


}



class DfsApi (val defaultFS: String, val username: String){

	val conf = new Configuration()
	val dfs = FileSystem.get(URI.create(defaultFS), conf)
	val ugi: UserGroupInformation  = UserGroupInformation.createProxyUser(username, UserGroupInformation.getLoginUser());


	def listdir(path: Path): Array[FileStatus] = {
	  var result: Array[FileStatus] = Array()
	  ugi.doAs(new PrivilegedExceptionAction[Unit]() {
	    def run = {result = dfs.listStatus(path)}
	  })
	  result
	}

        /**
          *
          * @return "/user/ + username" directory if it exists otherwice "/"
          */
        def getHomeDir(user: String = username): String = {
          if (dfs.isDirectory("/user/" + user)) "/user/" + user else "/"
        }

        def getFileStatus(path: Path): FileStatus = {
          var result = new FileStatus
          ugi.doAs(new PrivilegedExceptionAction[Unit]() {
	    def run = {result = dfs.getFileStatus(path)}
	  })
          result
        }

 
        def open(path: Path) = {
          var result: Option[FSDataInputStream] = None
          ugi.doAs(new PrivilegedExceptionAction[Unit]() {
	    def run = {result = Some(dfs.open(path))}
	  })
          result.get
        }

        def create(path: Path, overwrite: Boolean = false) = {
          var result: Option[FSDataOutputStream] = None
          ugi.doAs(new PrivilegedExceptionAction[Unit]() {
	    def run = {
              result = Some(dfs.create(path, overwrite))
            }
          })
          result.get
        }

        def write(path: Path, content: Enumerator[Array[Byte]], offset: Int = 0) = {
          ugi.doAs(new PrivilegedExceptionAction[Unit]() {
	    def run = {
              val outputStream = dfs.create(path, false)
              val writeIteratee = Iteratee.fold[Array[Byte],Int](offset) { (length, bytes) =>
                outputStream.write(bytes, length, bytes.length)
                length + bytes.length
              }
              val result = content.run(writeIteratee)
              result.onSuccess {
                case x =>
                  play.api.Logger.debug(s"Wrote $x bytes to $path")
                  outputStream.close()
              }
            }
	  })
        }


        def delete(path: Path, recursive: Boolean = false) = {
          ugi.doAs(new PrivilegedExceptionAction[Unit]() {
            def run = {
              dfs.delete(path, recursive)
            }
          })
        }

}


object DfsApi {

  def apply(username: String) = {
    new DfsApi("webhdfs://127.0.0.1:50070", username)
  }

}

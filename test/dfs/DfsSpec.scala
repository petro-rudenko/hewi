import api.DfsApi
import api.DfsImplicits._
import auth._
import controllers.dfs.DfsCtrl
import java.io.{FileNotFoundException, IOException}
import models.AppContext._
import models.User
import org.specs2.mutable._
import play.api.libs.iteratee.Enumerator
import play.api.test.Helpers._
import play.api.test.{WithApplication, FakeRequest}
import jp.t2v.lab.play2.auth.test.Helpers._

class DfsSpec extends Specification {

  "Distributed file system" should {
    val dfs = new DfsApi("file://tmp", "test")
    lazy val user = AuthProvider.getProvider.authenticate("test", "1111").get

    object config extends AuthConfigImpl

    "List directories" in {
      dfs.listdir("/tmp") must not be empty
      dfs.listdir("/defwefwe") must throwA[FileNotFoundException]
    }

    "Create and read files on dfs" in {
      try { dfs.delete("/tmp/test") }
      dfs.write("/tmp/test", Enumerator("Hello world"))
      val data = dfs.open("/tmp/test").asInstanceOf[java.io.InputStream]
      val result = scala.io.Source.fromInputStream(data).getLines().mkString
      result must equalTo("Hello world")
      dfs.write("/tmp/test", Enumerator("Hello world")) must throwA[IOException](message="File already exists: /tmp/test")
    }

    "Upload files streamingly to hdfs" in new WithApplication{
      //val request = FakeRequest().withLoggedIn(config)(user.id)
      todo
    }

    "Download files from HDFS" in new WithApplication{
       try {transactional(readWrite){ all[User].foreach(_.delete)}} // https://groups.google.com/forum/#!searchin/activate-persistence/activatetest/activate-persistence/I0sHxv4WatI/l1mw2bAJDdcJ
      val request = FakeRequest().withLoggedIn(config)(user.id)
      try { dfs.delete("/tmp/test.txt") }
      dfs.write("/tmp/test.txt", Enumerator("Hello world"))
      val result = DfsCtrl.download("/tmp/test.txt")(request)
    }

    "Stream hdfs folders in gzip" in new WithApplication{
      todo
    }

    "Stream file concatenation" in new WithApplication{
      todo
    }

  }
  
}


import api.DfsApi
import api.DfsImplicits._
import java.io.{FileNotFoundException, IOException}
import org.specs2.mutable._
import play.api.libs.iteratee.Enumerator
import play.api.test.Helpers._

class DfsSpec extends Specification {
  
  "Distributed file system" should {
    val dfs = new DfsApi("file://tmp", "test")

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

  }
  
}

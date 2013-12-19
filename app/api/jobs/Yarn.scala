package api.jobs
import org.apache.hadoop.mapreduce.v2.hs.JobHistory
import org.apache.hadoop.conf.Configuration

object Yarn {
  val conf = new Configuration()
  conf.addResource("mapred-site.xml")
  conf.addResource("yarn-site.xml")
  val hs = new JobHistory()
  hs.init(conf)

}

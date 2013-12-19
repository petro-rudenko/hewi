package api.scripts

import org.apache.hadoop.conf.Configuration
import org.apache.pig.{ PigServer, ExecType }

object Pig {
  val conf = new Configuration()
  val server = new PigServer(ExecType.MAPREDUCE, conf)

}

package api.nosql
import org.apache.accumulo.core.client.Instance
import org.apache.accumulo.core.client.ZooKeeperInstance
import org.apache.accumulo.core.client.Connector

class Accumulo(instanceName: String, zookeepers: String, username: String, password: String) {
  val inst = new ZooKeeperInstance(instanceName, zookeepers)
  val conn = inst.getZooKeepers()

}

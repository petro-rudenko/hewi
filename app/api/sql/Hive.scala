package api.sql

import java.sql.{ Connection, DriverManager, ResultSet }

class HiveApi(username: String, connectionStr: String) {
  val driverName = "org.apache.hive.jdbc.HiveDriver"
  Class.forName(driverName);
  def con: Connection = DriverManager.getConnection(connectionStr, username, "");

}

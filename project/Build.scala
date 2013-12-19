import sbt._
import Keys._
import play.Project._
import de.johoop.jacoco4sbt._
import JacocoPlugin._

object ApplicationBuild extends Build {

  val appName         = "BigDataUI"
  val appVersion      = "1.0-SNAPSHOT"
    
  val webjars = Seq(
    "org.webjars" % "webjars-play_2.10" % "2.2.0",
    "org.webjars" % "font-awesome" % "4.0.3",
    "org.webjars" % "bootswatch" % "3.0.0",
    "org.webjars" % "bootstrap-glyphicons" % "bdd2cbfba0",
    "org.webjars" % "jquery-file-upload" % "8.4.2",
    "org.webjars" % "knockout" % "3.0.0"
  )

  val HADOOP_VERSION = "2.2.0"
  val apache = Seq(
    "org.apache.hadoop" % "hadoop-hdfs" % HADOOP_VERSION,
    "org.apache.hadoop" % "hadoop-common" % HADOOP_VERSION,
    "org.apache.hadoop" % "hadoop-mapreduce" % HADOOP_VERSION,
    "org.apache.hadoop" % "hadoop-mapreduce-client-hs" % HADOOP_VERSION,
    "org.apache.hadoop" % "hadoop-client" % HADOOP_VERSION,
    "org.apache.pig" % "pig" % "0.12.0" classifier "h2",
    "dk.brics.automaton" % "automaton" % "1.11-8", //https://issues.apache.org/jira/browse/PIG-3315
    "org.apache.hive" % "hive-jdbc" % "0.12.0",
    "org.apache.hbase" % "hbase-common" % "0.96.0-hadoop2",
    "org.apache.hbase" % "hbase-client" % "0.96.0-hadoop2",
    "org.apache.accumulo" % "accumulo" % "1.5.0"
  )

  val appDependencies = Seq(
    jdbc, filters, cache,
    "net.fwbrasil" %% "activate-core" % "1.5-SNAPSHOT",
    "net.fwbrasil" %% "activate-play" % "1.4.1" exclude("org.scala-stm", "scala-stm_2.10.0"),
    "net.fwbrasil" %% "activate-jdbc" % "1.5-SNAPSHOT",
    "mysql" % "mysql-connector-java" % "5.1.27",
    "com.unboundid" % "unboundid-ldapsdk" % "2.3.5",
    "jp.t2v" %% "play2-auth"      % "0.11.0",
    "jp.t2v" %% "play2-auth-test" % "0.11.0" % "test",
    "org.mindrot" % "jbcrypt" % "0.3m"
  ) ++ webjars ++ apache

  
  val main = play.Project(appName, appVersion, appDependencies).settings(
    javaOptions in Test += "-Dconfig.file=conf/test.conf",
    scalaVersion := "2.10.4-SNAPSHOT", //http://stackoverflow.com/questions/19256417/using-unboundid-ldap-in-scala-strange-compile-error
    resolvers ++= Seq(
      "flavio" at "http://fwbrasil.net/maven",
      "clojars" at "http://clojars.org/repo/",
      "clojure-releases" at "http://build.clojure.org/releases",
      "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      "Nexus Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Nexus release" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )).settings(org.scalastyle.sbt.ScalastylePlugin.Settings: _*).
    settings(jacoco.settings: _*).
    settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}

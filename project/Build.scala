import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "BigDataUI"
  val appVersion      = "1.0-SNAPSHOT"
  val appsPath 		  = "applications"
    
  val webjars = Seq(
	 "org.webjars" %% "webjars-play" % "2.1.0-3",
	 "org.webjars" % "font-awesome" % "3.2.1",
	 "org.webjars" % "html5shiv" % "3.6.2",
	 "org.webjars" % "bootstrap" % "3.0.0",
	 "org.webjars" % "bootstrap-glyphicons" % "bdd2cbfba0",
	 "org.webjars" % "angularjs" % "1.1.5-1"	
  )

  val appDependencies = Seq(
      jdbc,
      "org.apache.hadoop" % "hadoop-hdfs" % "2.0.5-alpha",
      //"com.typesafe.play" %% "play-slick" % "0.4.0",
        "net.fwbrasil" %% "activate-core" % "1.3",
  		"net.fwbrasil" %% "activate-play" % "1.3",
  		//"net.fwbrasil" %% "activate-slick" % "1.3",
  		//"mysql" % "mysql-connector-java" % "5.1.26"
  		"net.fwbrasil" %% "activate-jdbc" % "1.3",
      "jp.t2v" %% "play2.auth"      % "0.10.1",
      "jp.t2v" %% "play2.auth.test" % "0.10.1" % "test",
      "org.mindrot" % "jbcrypt" % "0.3m"
  ) ++ webjars

  val dfs = play.Project(appName + "-dfs", appVersion, appDependencies, path = file(appsPath + "/dfs"))
  val rms = play.Project(appName + "-rm", appVersion, appDependencies, path = file(appsPath + "/rms"))
  val dbs = play.Project(appName + "-db", appVersion, appDependencies, path = file(appsPath + "/dbs"))
  val graphs = play.Project(appName + "-graph", appVersion, appDependencies, path = file(appsPath + "/graphs"))
  val scripts = play.Project(appName + "-scripts", appVersion, appDependencies, path = file(appsPath + "/scripting"))
  val streams = play.Project(appName + "-streams", appVersion, appDependencies, path = file(appsPath + "/streams"))
  val sql = play.Project(appName + "-sql", appVersion, appDependencies, path = file(appsPath + "/sql"))
  val workflows = play.Project(appName + "-workflows", appVersion, appDependencies, path = file(appsPath + "/workflows"))
  
  
  val main = play.Project(appName, appVersion, appDependencies).settings(
      javaOptions in Test += "-Dconfig.file=conf/test.conf"     
    ).dependsOn(dfs, rms, dbs, graphs, scripts, streams, sql, workflows)
    .aggregate(dfs,rms, dbs,graphs, scripts, streams, sql, workflows)

}

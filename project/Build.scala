import sbt._
import Keys._
import play.Project._


object ApplicationBuild extends Build {

  val appName         = "BigDataUI"
  val appVersion      = "1.0-SNAPSHOT"
  val appsPath 		  = "applications"
    
  val webjars = Seq(
	 "org.webjars" % "webjars-play_2.10" % "2.2.0",
         "org.webjars" % "requirejs" % "2.1.8",
	 "org.webjars" % "font-awesome" % "3.2.1",
	 "org.webjars" % "html5shiv" % "3.6.2",
	 "org.webjars" % "bootstrap" % "3.0.0",
	 "org.webjars" % "jquery" % "2.0.3",
	 "org.webjars" % "bootstrap-glyphicons" % "bdd2cbfba0",
	 "org.webjars" % "angularjs" % "1.0.8"	
  )

  val appDependencies = Seq(
      jdbc, filters, cache,
      "org.apache.hadoop" % "hadoop-hdfs" % "2.2.0",
      "org.apache.hadoop" % "hadoop-common" % "2.2.0",
      "org.apache.pig" % "pig" % "0.11.1",
      "org.apache.accumulo" % "accumulo" % "1.5.0",
      //"com.typesafe.play" %% "play-slick" % "0.4.0",
      "net.fwbrasil" %% "activate-core" % "1.5-SNAPSHOT",
      "net.fwbrasil" %% "activate-play" % "1.4.1" exclude("org.scala-stm", "scala-stm_2.10.0"),
      "net.fwbrasil" %% "activate-jdbc" % "1.5-SNAPSHOT",
  		//"net.fwbrasil" %% "activate-slick" % "1.3",
  		//"mysql" % "mysql-connector-java" % "5.1.26"  		
      "jp.t2v" %% "play2-auth"      % "0.11.0-SNAPSHOT",
      "jp.t2v" %% "play2-auth-test" % "0.11.0-SNAPSHOT" % "test",
      "org.mindrot" % "jbcrypt" % "0.3m"
  ) ++ webjars

  
  val main = play.Project(appName, appVersion, appDependencies).settings(
      javaOptions in Test += "-Dconfig.file=conf/test.conf",
      resolvers ++= Seq(
        "flavio" at "http://fwbrasil.net/maven",
        "clojars" at "http://clojars.org/repo/",
        "clojure-releases" at "http://build.clojure.org/releases",
        "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
        "Nexus Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots",
        "Nexus release" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
      ),
    initialCommands := """ // make app resources accessible
                     |Thread.currentThread.setContextClassLoader(getClass.getClassLoader)
                     |""".stripMargin
  )

}

// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "clojars" at "http://clojars.org/repo/"

resolvers += "clojure-releases" at "http://build.clojure.org/releases"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val clojureScriptLibs = Seq()

libraryDependencies ++= clojureScriptLibs

// Use the Play sbt plugin for Play projects //
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.1")

addSbtPlugin("io.github.petro-rudenko" % "play-clojurescript" % "0.0.1")

//https://github.com/jrudolph/sbt-dependency-graph -- For generating dependency graphs
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

//Code style and Unit test coverage tools.
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.2")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.1")


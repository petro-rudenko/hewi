// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "clojars" at "http://clojars.org/repo/"

resolvers += "clojure-releases" at "http://build.clojure.org/releases"

libraryDependencies ++= Seq(
  "jayq" % "jayq" % "2.4.0"
  // "org.clojure" % "clojure" % "1.5.1",
  // "org.clojure" % "clojurescript" % "0.0-1934"
)

// Use the Play sbt plugin for Play projects //
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")
//addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.1.3")

addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.+")

addSbtPlugin("io.github.petro-rudenko" % "play-clojurescript" % "0.0.1")
//addSbtPlugin("de.johoop" % "play-clojurescript" % "1.0.0")

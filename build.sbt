name := "bitcoin-akka-node"

organization := "oohish.com"

version := "1.0.1-SNAPSHOT"

scalaVersion := "2.11.2"

scalacOptions += "-feature"

scalacOptions += "-deprecation"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "yzernik repo" at "http://dl.bintray.com/yzernik/maven/"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"
 
libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "com.oohish" %% "bitcoin-scodec" % "0.1.6",
    "org.typelevel" %% "scodec-stream" % "0.1.0",
    "com.github.krasserm" %% "streamz-akka-stream" % "0.1",
    "com.typesafe.akka" %% "akka-stream-experimental" % "0.6",
    "joda-time" % "joda-time" % "2.4",
    "org.joda" % "joda-convert" % "1.4"
)
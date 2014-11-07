import scoverage.ScoverageSbtPlugin.instrumentSettings
import org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

name := "bitcoin-akka-node"

organization := "oohish.com"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.2"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalacOptions += "-feature"

scalacOptions += "-deprecation"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "yzernik repo" at "http://dl.bintray.com/yzernik/maven/",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"
)
 
libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "com.oohish" %% "bitcoin-scodec" % "0.2.3",
    "org.typelevel" %% "scodec-stream" % "0.1.0",
    "joda-time" % "joda-time" % "2.4",
    "org.joda" % "joda-convert" % "1.4",
    "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
    "com.github.scopt" %% "scopt" % "3.2.0",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test",
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)

instrumentSettings

coverallsSettings

seq(bintrayPublishSettings:_*)
name := "reactive-bitcoin-node"

organization := "io.github.yzernik"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.6"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalacOptions += "-feature"

scalacOptions += "-deprecation"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "yzernik repo" at "http://dl.bintray.com/yzernik/maven/"
)
 
libraryDependencies ++= {
	val akkaV = "2.3.7"
	val sumacV = "0.3.0"
	val btcioV = "0.1.3"
	Seq(
    	"com.typesafe.akka" %% "akka-actor" % akkaV,
    	"com.quantifind" %% "sumac" % sumacV,
    	"io.github.yzernik" %% "btc-io" % btcioV,
    	"com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    	"org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
	)
}


scoverage.ScoverageSbtPlugin.instrumentSettings

org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

seq(bintrayPublishSettings:_*)

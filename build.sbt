name := "bitcoin-akka-node"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.4"

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
	val bitcoinScodecV = "0.2.3"
	Seq(
    	"com.typesafe.akka" %% "akka-actor" % akkaV,
    	"com.quantifind" %% "sumac" % sumacV,
    	"com.oohish" %% "bitcoin-scodec" % bitcoinScodecV,
    	"com.github.yzernik" %% "btc-io" % "0.1.0",
    	"joda-time" % "joda-time" % "2.4",
    	"org.joda" % "joda-convert" % "1.4",
    	"com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    	"org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
	)
}


scoverage.ScoverageSbtPlugin.instrumentSettings

org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

seq(bintrayPublishSettings:_*)
name := "reactive-bitcoin-node"

organization := "io.github.yzernik"

version := "0.1.0"

scalaVersion := "2.11.6"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "yzernik repo" at "http://dl.bintray.com/yzernik/maven/"
)
 
libraryDependencies ++= {
  val akkaV = "2.3.11"
  Seq(
    "com.typesafe.akka" %% "akka-actor"     % akkaV,
    "com.quantifind"    %% "sumac"          % "0.3.0",
    "com.typesafe.play" %% "play-json"      % "2.4.2",
    "io.github.yzernik" %% "btc-io"         % "0.1.6",
    "com.typesafe.akka" %% "akka-testkit"   % akkaV    % "test",
    "org.scalatest"      % "scalatest_2.11" % "2.2.4"  % "test"
  )
}


scoverage.ScoverageSbtPlugin.instrumentSettings

org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

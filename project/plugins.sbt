resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.11")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "0.99.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.2")

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")
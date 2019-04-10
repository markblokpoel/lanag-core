val username = "markblokpoel"
val repo = "com.markblokpoel.lanag-core"

lazy val commonSettings = Seq(
  name := repo,
  scalaVersion := "2.12.8",
  organization := s"com.markblokpoel",
  description := "This is an implementation of the core API for the Lanag agent-based simulation framework.",
  crossScalaVersions := Seq("2.12.8"),
  crossVersion := CrossVersion.binary,
  libraryDependencies += Dependencies.scalatest,
  libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "2.4.1" % Provided,
    "org.apache.spark" %% "spark-sql" % "2.4.1" % Provided,
    "com.typesafe" % "config" % "1.3.3"
  ),
  // Compile options
  updateImpactOpenBrowser := false,
  compile in Compile := (compile in Compile).dependsOn(formatAll).value,
  mainClass in assembly := Some("com.markblokpoel.lanag.com.markblokpoel.lanag.coreg.util.DefaultMain"),
  test in Test := (test in Test).dependsOn(checkFormat).value,
  formatAll := {
    (scalafmt in Compile).value
    (scalafmt in Test).value
    (scalafmtSbt in Compile).value
  },
  checkFormat := {
    (scalafmtCheck in Compile).value
    (scalafmtCheck in Test).value
    (scalafmtSbtCheck in Compile).value
  }
)

lazy val root = (project in file("."))
  .settings(name := s"$repo-root")
  .settings(commonSettings: _*)
  .settings(docSettings: _*)
  .settings(skip in publish := true)
  .settings(releaseSettings: _*)
  .enablePlugins(ScalaUnidocPlugin)
  .enablePlugins(GhpagesPlugin)

/*
 Scaladoc settings
 Note: To compile diagrams, Graphviz must be installed in /usr/local/bin
 */
lazy val docSettings = Seq(
  autoAPIMappings := true,
  siteSourceDirectory := baseDirectory.value / "site",
  scalacOptions in(Compile, doc) ++= Seq(
    "-groups",
    "-diagrams",
    "-implicits",
    "-doc-root-content", baseDirectory.value + "/overview.txt",
    "-doc-title", "Language Agents",
    "-diagrams-dot-path", "/usr/local/bin/dot"),
  siteSubdirName in ScalaUnidoc := "latest/api",
  addMappingsToSiteDir(mappings in(ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc),
  git.remoteRepo := s"git@github.com:$username/$repo.git",
  envVars in ghpagesPushSite += ("SBT_GHPAGES_COMMIT_MESSAGE" -> s"Publishing Scaladoc [CI SKIP]")
)


// Enforce source formatting before submit
lazy val formatAll = taskKey[Unit]("Format all the source code which includes src, test, and build files")
lazy val checkFormat = taskKey[Unit]("Check all the source code which includes src, test, and build files")


// Maven / Scaladex release settings
import ReleaseTransformations._

lazy val releaseSettings = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    //runClean,
    //runTest,
    setReleaseVersion,
    //commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion,
    //commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    //pushChanges
  ),
  // Github publish settings
  homepage := Some(url(s"https://github.com/$username/$repo")),
  licenses += "MIT" -> url(s"https://github.com/$username/$repo/blob/master/LICENSE"),
  scmInfo := Some(ScmInfo(url(s"https://github.com/$username/$repo"), s"git@github.com:$username/$repo.git")),
  apiURL := Some(url(s"https://$username.github.io/$repo/latest/api/")),
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  developers := List(
    Developer(
      id = username,
      name = "Mark Blokpoel",
      email = "mark.blokpoel@gmail.com",
      url = new URL(s"http://github.com/$username")
    )
  ),
  useGpg := true,
  usePgpKeyHex("2774FB8FDE41F70F72F7FA1BB1B7EBBD73248A55"),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
  sonatypeProfileName := "markblokpoel",
//  credentials ++= (for {
//    username <- sys.env.get("SONATYPE_USERNAME")
//    password <- sys.env.get("SONATYPE_PASSWORD")
//  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,
  // Following 2 lines need to get around https://github.com/sbt/sbt/issues/4275
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
)

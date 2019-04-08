name := "Lanag2"

version := "0.2"

scalaVersion := "2.12.8"

/*
 Library dependencies
 */
libraryDependencies += "org.apache.spark" %% "spark-lanag.core" % "2.4.1" % Provided

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.1" % Provided

libraryDependencies += "com.typesafe" % "config" % "1.3.3"

/*
 Scaladoc settings
 Note: To compile diagrams, Graphviz must be installed in /usr/local/bin
 */

scalacOptions in (Compile, doc) ++= Seq("-groups", "-diagrams", "-implicits",
  "-doc-root-content", baseDirectory.value+ "/overview.txt", "-doc-title", "Language Agents",
  "-diagrams-dot-path", "/usr/local/bin/dot")

autoAPIMappings := true

/*
  Assembly settings
 */
mainClass in assembly := Some("lanag.lanag.coreg.util.DefaultMain")
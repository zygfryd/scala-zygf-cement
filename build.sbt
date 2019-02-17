name := "cement"

version := "0.1"

lazy val scala212 = "2.12.8"
lazy val scala213 = "2.13.0-M5"
lazy val supportedScalaVersions = List(scala212, scala213)

scalaVersion := scala212
crossScalaVersions := supportedScalaVersions

libraryDependencies ++= Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value,
                            "org.scalatest" %% "scalatest" % "3.0.6-SNAP6" % "test")

scalacOptions ++= Seq("-sourcepath", (baseDirectory in ThisBuild).value.getAbsolutePath,
                      "-deprecation",
                      "-feature",
                      "-unchecked",
                      "-language:experimental.macros",
                      "-Xfatal-warnings",
                      "-Xlint:inaccessible",
                      "-Xlint:infer-any",
                      "-Xlint:missing-interpolator",
                      "-Xlint:option-implicit",
                      "-Xlint:poly-implicit-overload",
                      "-Xlint:type-parameter-shadow")

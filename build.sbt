name := "cement"

version := "0.1"

organization := "net.zygfryd"
organizationName := "zygfryd's projects"
organizationHomepage := Some(url("https://zygfryd.net/"))

description := "A factory macro for anonymous final static lazy variables, for Scala."

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/zygfryd/scala-zygf-cement"))

scmInfo := Some(ScmInfo(url("https://github.com/zygfryd/scala-zygf-cement"),
                        "scm:git@github.com:zygfryd/scala-zygf-cement.git"))

lazy val scala212 = "2.12.8"
lazy val scala213 = "2.13.0-M5"
lazy val supportedScalaVersions = List(scala212, scala213)

scalaVersion := scala212
crossScalaVersions := supportedScalaVersions

libraryDependencies ++= Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value,
                            "org.scalatest" %% "scalatest" % "3.0.6-SNAP6" % "test",
                            "org.apache.logging.log4j" % "log4j-api" % "2.11.2" % "test",
                            "com.lihaoyi" %% "sourcecode" % "0.1.4" % "test")

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

publishTo := sonatypePublishTo.value

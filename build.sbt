name := "cement"

version := "0.2.2"

organization := "net.zygfryd"
organizationName := "zygfryd's projects"
organizationHomepage := Some(url("https://zygfryd.net/"))

description := "A factory macro for anonymous final static lazy variables, for Scala."

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/zygfryd/scala-zygf-cement"))

scmInfo := Some(ScmInfo(url("https://github.com/zygfryd/scala-zygf-cement"),
                        "scm:git@github.com:zygfryd/scala-zygf-cement.git"))

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.11"
lazy val scala213 = "2.13.1"
lazy val supportedScalaVersions = List(scala212, scala213, scala211)

scalaVersion := scala212
crossScalaVersions := supportedScalaVersions

libraryDependencies ++= Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value,
                            "org.scalatest" %% "scalatest" % "3.0.8" % "test",
                            "org.apache.logging.log4j" % "log4j-api" % "2.11.2" % "test",
                            "com.lihaoyi" %% "sourcecode" % "0.2.1" % "test")

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

name := "cement"

version := "0.3.0"

organization := "net.zygfryd"
organizationName := "zygfryd's projects"
organizationHomepage := Some(url("https://zygfryd.net/"))

description := "A factory macro for anonymous final static lazy variables, for Scala."

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/zygfryd/scala-zygf-cement"))

scmInfo := Some(ScmInfo(url("https://github.com/zygfryd/scala-zygf-cement"),
                        "scm:git@github.com:zygfryd/scala-zygf-cement.git"))

val scala211 = "2.11.12"
val scala212 = "2.12.14"
val scala213 = "2.13.6"
val scala30x = "3.0.0"
val supportedScalaVersions = List(scala212, scala213, scala211, scala30x)

scalaVersion := scala30x
crossScalaVersions := supportedScalaVersions

libraryDependencies ++= {
  if (scalaVersion.value.startsWith("2"))
    Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
  else
    Nil
}

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.2.9" % "test",
                            "org.apache.logging.log4j" % "log4j-api" % "2.11.2" % "test",
                            "com.lihaoyi" %% "sourcecode" % "0.2.7" % "test")

scalacOptions ++= Seq("-deprecation",
                      "-feature",
                      "-unchecked",
                      "-language:experimental.macros",
                      "-Xfatal-warnings")

scalacOptions ++= {
  if (scalaVersion.value.startsWith("2"))
    Seq("-Xlint:inaccessible",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:option-implicit",
        "-Xlint:poly-implicit-overload",
        "-Xlint:type-parameter-shadow")
  else
    Seq("-Xcheck-macros",
        "-old-syntax",
        "-no-indent")
}


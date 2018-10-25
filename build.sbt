
// https://github.com/pbassiner/sbt-multi-project-example/blob/master/build.sbt

name := "dsl-for-starting-in-dependency-order"
organization in ThisBuild := "hu.tigra"
scalaVersion in ThisBuild := "2.12.6"

// PROJECTS

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(
    core_dsl,
    open_shift_starter
  )

lazy val core_dsl = project
  .settings(
    name := "core_dsl",
    settings,
    libraryDependencies ++= core_dsl_dependencies
  )

lazy val open_shift_starter = project
  .settings(
    name := "open_shift_starter",
    settings,
    libraryDependencies ++= core_dsl_dependencies ++ Seq(
    )
  )
  .dependsOn(
    core_dsl
  )

// DEPENDENCIES

lazy val dependencies =
  new {
    val combinatorParserV = "1.0.4"

    val logbackV = "1.2.3"
    val logstashV = "4.11"
    val scalaLoggingV = "3.7.2"
    val slf4jV = "1.7.25"
    val typesafeConfigV = "1.3.1"
    val pureconfigV = "0.8.0"
    val monocleV = "1.4.0"
    val akkaV = "2.5.6"
    val scalatestV = "3.0.4"
    val scalacheckV = "1.13.5"


    val combinatorParser = "org.scala-lang.modules" %% "scala-parser-combinators" % combinatorParserV

    val logback = "ch.qos.logback" % "logback-classic" % logbackV
    val logstash = "net.logstash.logback" % "logstash-logback-encoder" % logstashV
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV
    val slf4j = "org.slf4j" % "jcl-over-slf4j" % slf4jV
    val typesafeConfig = "com.typesafe" % "config" % typesafeConfigV
    val akka = "com.typesafe.akka" %% "akka-stream" % akkaV
    val monocleCore = "com.github.julien-truffaut" %% "monocle-core" % monocleV
    val monocleMacro = "com.github.julien-truffaut" %% "monocle-macro" % monocleV
    val pureconfig = "com.github.pureconfig" %% "pureconfig" % pureconfigV
    val scalatest = "org.scalatest" %% "scalatest" % scalatestV
    val scalacheck = "org.scalacheck" %% "scalacheck" % scalacheckV
  }

lazy val core_dsl_dependencies = Seq(
  dependencies.combinatorParser,
  dependencies.scalatest % "test",
  dependencies.scalacheck % "test"
)


// SETTINGS

lazy val settings = commonSettings

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

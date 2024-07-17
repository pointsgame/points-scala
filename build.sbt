ThisBuild / scalaVersion := "3.4.2"
ThisBuild / scalacOptions ++= Seq("-language:strictEquality", "-Yexplicit-nulls", "-Ysafe-init")
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val cats = "org.typelevel" %% "cats-core" % "2.12.0"
val munit = "org.scalameta" %% "munit" % "1.0.0" % Test
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.18.0" % Test
val munitScalacheck = "org.scalameta" %% "munit-scalacheck" % "1.0.0" % Test

val field = project
  .settings(
    libraryDependencies ++= Seq(cats, munit, scalacheck, munitScalacheck),
  )

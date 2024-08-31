ThisBuild / scalaVersion := "3.5.0"
ThisBuild / scalacOptions ++= Seq("-language:strictEquality", "-Yexplicit-nulls", "-Wsafe-init")
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val cats = "org.typelevel" %% "cats-core" % "2.12.0"
val catsEffect = "org.typelevel" %% "cats-effect" % "3.5.4"
val fs2 = "co.fs2" %% "fs2-core" % "3.11.0"
val munit = "org.scalameta" %% "munit" % "1.0.1" % Test
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.18.0" % Test
val munitScalacheck = "org.scalameta" %% "munit-scalacheck" % "1.0.0" % Test
val kittens = "org.typelevel" %% "kittens" % "3.4.0"
val decline = "com.monovore" %% "decline" % "2.4.1"
val declineEffect = "com.monovore" %% "decline-effect" % "2.4.1"

val field = project
  .settings(
    libraryDependencies ++= Seq(cats, munit, scalacheck, munitScalacheck),
  )

val bench = project
  .dependsOn(field)
  .settings(
    Compile / mainClass := Some("points.bench.Bench"),
    Compile / run / fork := true,
    libraryDependencies ++= Seq(catsEffect, fs2, kittens, decline, declineEffect),
  )

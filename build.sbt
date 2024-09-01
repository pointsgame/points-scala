import scala.scalanative.build.*

ThisBuild / scalaVersion := "3.5.0"
ThisBuild / scalacOptions ++= Seq("-language:strictEquality", "-Yexplicit-nulls", "-Wsafe-init")
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val field = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.12.0",
      "org.scalameta" %%% "munit" % "1.0.1" % Test,
      "org.scalacheck" %%% "scalacheck" % "1.18.0" % Test,
      "org.scalameta" %%% "munit-scalacheck" % "1.0.0" % Test,
    ),
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.12.0",
      "org.scalameta" %%% "munit" % "1.0.1" % Test,
      "org.scalacheck" %%% "scalacheck" % "1.18.0" % Test,
      "org.scalameta" %%% "munit-scalacheck" % "1.0.0" % Test,
    ),
  )
  .nativeSettings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.11.0",
    ),
  )

val bench = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .dependsOn(field)
  .settings(
    Compile / mainClass := Some("points.bench.Bench"),
    Compile / run / fork := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "3.5.4",
      "co.fs2" %%% "fs2-core" % "3.11.0",
      "com.monovore" %%% "decline" % "2.4.1",
      "com.monovore" %%% "decline-effect" % "2.4.1",
    ),
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "kittens" % "3.4.0",
    )
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "kittens" % "3.4.0",
    )
  )
  .nativeSettings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "kittens" % "3.3.0",
    ),
    nativeConfig ~= {
      _.withLTO(LTO.thin)
        .withMode(Mode.releaseFull)
    },
  )

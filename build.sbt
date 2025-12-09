import scala.scalanative.build.*

ThisBuild / scalaVersion := "3.7.4"
ThisBuild / scalacOptions ++= Seq("-language:strictEquality", "-Yexplicit-nulls", "-Wsafe-init")
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val field = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.13.0",
      "org.scalameta" %%% "munit" % "1.2.1" % Test,
      "org.scalacheck" %%% "scalacheck" % "1.19.0" % Test,
      "org.scalameta" %%% "munit-scalacheck" % "1.2.0" % Test,
    ),
  )

val bench = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .dependsOn(field)
  .settings(
    Compile / mainClass := Some("points.bench.Bench"),
    Compile / run / fork := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "3.6.3",
      "co.fs2" %%% "fs2-core" % "3.12.2",
      "org.typelevel" %%% "kittens" % "3.5.0",
      "com.monovore" %%% "decline" % "2.5.0",
      "com.monovore" %%% "decline-effect" % "2.5.0",
    ),
  )
  .nativeSettings(
    nativeConfig ~= {
      _.withLTO(LTO.thin)
        .withMode(Mode.releaseFull)
    },
  )

val verify = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .dependsOn(field)
  .settings(
    Compile / mainClass := Some("points.verify.Verify"),
    Compile / run / fork := true,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "3.6.3",
      "co.fs2" %%% "fs2-core" % "3.12.2",
      "co.fs2" %%% "fs2-io" % "3.12.2",
    ),
  )
  .nativeSettings(
    nativeConfig ~= {
      _.withLTO(LTO.thin)
        .withMode(Mode.releaseFull)
    },
  )

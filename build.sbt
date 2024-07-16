ThisBuild / scalaVersion := "3.4.2"
ThisBuild / scalacOptions ++= Seq("-language:strictEquality")
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val cats = "org.typelevel" %% "cats-core" % "2.12.0"

val field = project
  .settings(
    libraryDependencies ++= Seq(cats),
  )

import Dependencies._

ThisBuild / scalaVersion     := "2.12.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "dev.rubensanchez"

lazy val root = (project in file("."))
  .settings(
    name := "booksoup",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += jsoup,
    libraryDependencies += csv,
  )

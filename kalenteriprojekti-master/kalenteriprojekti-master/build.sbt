ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "Kalenteriprojekti"
  )

libraryDependencies ++= Seq(
  "org.scalafx" % "scalafx_3" % "22.0.0-R33",
  "org.mnode.ical4j" % "ical4j" % "4.0.0"
)

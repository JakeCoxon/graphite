organization  := "com.jakemadethis"

version       := "1.1"

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= 
  Seq(
    "commons-collections" % "commons-collections" % "3.2.1",
    "org.scala-lang" % "scala-swing" % "2.10.4",
    "net.sf.jung" % "jung-api" % "2.0.1",
    "net.sf.jung" % "jung-algorithms" % "2.0.1",
    "net.sf.jung" % "jung-io" % "2.0.1",
    "net.sf.jung" % "jung-visualization" % "2.0.1",
    "org.abego.treelayout" % "org.abego.treelayout.core" % "1.0.1",
    "org.scalatest" % "scalatest_2.10" % "2.2.4"
  )

name := "edx-programming-reactive-systems"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.21"
val akkaHttpVersion = "10.1.7"
val jsoupVersion = "1.11.3"
val specs2Version = "4.4.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "org.jsoup" % "jsoup" % jsoupVersion,

  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.specs2" %% "specs2-core" % specs2Version % Test
)

scalacOptions in Test ++= Seq("-Yrangepos")
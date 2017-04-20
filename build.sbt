import android.Keys._
import sbt.Keys._

lazy val common = (project in file("common"))
  .settings(commonSettings)
  .settings(
    name := "Akka VoIP common library",
    exportJars := true
  )

lazy val client = (project in file("client")).enablePlugins(AndroidApp)
  .settings(commonSettings)
  .settings(
    name := "Akka VoIP client",
    versionName := Some("0.1-SNAPSHOT"),
    versionCode := Some(1),
    platformTarget := "android-25",
    minSdkVersion := "18",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions += "-target:jvm-1.7"
  ).aggregate(common).dependsOn(common)

lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    name := "Akka VoIP server"
    /*
     Override Akka version by newer one if you want.
     Please watch out if `common` codes are kept source compatible among Akka different versions.
     */
    //,libraryDependencies ++= Seq(
    //   "com.typesafe.akka" %% "akka-actor" % "2.5.0",
    //   "com.typesafe.akka" %% "akka-testkit" % "2.3.16" % Test
    // )
  ).aggregate(common).dependsOn(common)

lazy val commonSettings = Seq(
  scalaVersion := "2.11.11",
  version := "0.1-SNAPSHOT",
  scalacOptions ++= Seq("-unchecked", "-feature", "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.16",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.16" % Test,
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )
)

lazy val akkaJar = (project in file("akka-jar")).enablePlugins(AndroidApp)
  .settings(commonSettings)
  .settings(
    name := "Akka Jar Test",
    versionName := Some("0.1-SNAPSHOT"),
    versionCode := Some(1),
    platformTarget := "android-25",
    minSdkVersion := "18",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions += "-target:jvm-1.7"
  )

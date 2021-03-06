name := "beyond"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "ch.qos.logback.contrib" % "logback-mongodb-classic" % "0.1.2",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT",
  "org.reactivemongo" %% "reactivemongo" % "0.10.5.akka23-SNAPSHOT"
)

lazy val root = project.in(file("."))
  .aggregate(beyondCore, beyondUser, beyondAdmin, rhinoScalaBinding)
  .dependsOn(beyondCore, beyondUser, beyondAdmin)
  .enablePlugins(PlayScala)

lazy val beyondCore = project.in(file("core"))
  .dependsOn(rhinoScalaBinding)
  .enablePlugins(PlayScala)

lazy val beyondAdmin: Project = project.in(file("modules/admin"))
  .enablePlugins(PlayScala)

lazy val beyondUser: Project = project.in(file("modules/user"))
  .dependsOn(beyondCore)
  .enablePlugins(PlayScala)

lazy val rhinoScalaBinding = project

org.scalastyle.sbt.ScalastylePlugin.Settings

lazy val pluginTest = TaskKey[Unit]("plugin-test", "Plugin JavaScript API Test")

fullRunTask(pluginTest, Compile, "beyond.plugin.test.TestRunner")

lazy val jsConsole = TaskKey[Unit]("js-console", "JavaScript shell console in Beyond")

fullRunTask(jsConsole, Compile, "beyond.tool.JavaScriptShellConsole")

scalariformSettings

Common.settings

scalacOptions ++= Seq(
  "-feature"
)

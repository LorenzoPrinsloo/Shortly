enablePlugins(JavaAppPackaging)
enablePlugins(AshScriptPlugin)

dockerBaseImage := "openjdk:jre-alpine"

val Http4sVersion = "0.20.23"
val CirceVersion = "0.11.2"
val ScalaTestVersion = "3.2.2"
val MockitoVersion = "1.16.2"
val DoobieVersion = "0.9.0"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    organization := "xite",
    name := "shortly",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.11",
    libraryDependencies ++= Seq(
      "org.tpolecat"    %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"    %% "doobie-postgres"     % DoobieVersion,
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "org.scalatest"   %% "scalatest"           % ScalaTestVersion % "test",
      "org.mockito"     %% "mockito-scala"       % MockitoVersion % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "com.typesafe"    % "config"               % "1.4.0"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification"
)

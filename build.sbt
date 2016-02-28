import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

PB.protobufSettings
PB.pythonExecutable in PB.protobufConfig := "C:\\Python27\\Python.exe"

name          := """dis13-rest"""
organization  := "cz.codingmonkeys"
version       := "1.0.0"
scalaVersion  := "2.11.7"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaStreamV      = "2.0.3"
  val scalaTestV       = "3.0.0-M1"
  val scalaMockV       = "3.2.2"
  val scalazScalaTestV = "0.2.3"
  val slickVersion     = "3.1.0"
  val akkaVersion = "2.4.1"
  Seq(
    "com.typesafe.akka"  %% "akka-stream-experimental"             % akkaStreamV,
    "com.typesafe.akka"  %% "akka-http-core-experimental"          % akkaStreamV,
    "com.typesafe.akka"  %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.slick" %% "slick"                                % slickVersion,
    "com.typesafe.akka"  %% "akka-cluster"                         % akkaVersion,
    "com.typesafe.akka"  %% "akka-cluster-tools"                   % akkaVersion,
    "com.typesafe.slick" %% "slick-hikaricp"                       % "3.1.0",
    "com.zaxxer"         %  "HikariCP"                             % "2.4.3",
    "org.slf4j"          %  "slf4j-nop"                            % "1.6.4",
    "org.postgresql"     %  "postgresql"                           % "9.4-1201-jdbc41",
    "org.mindrot"        %  "jbcrypt"                              % "0.3m",
    "org.flywaydb"       %  "flyway-core"                          % "3.2.1",
    "com.google.protobuf" % "protobuf-java" % "2.6.1",
    "org.scalatest"      %% "scalatest"                            % scalaTestV       % "it,test",
    "org.scalamock"      %% "scalamock-scalatest-support"          % scalaMockV       % "it,test",
    "com.typesafe.akka"  %% "akka-http-testkit-experimental"       % akkaStreamV      % "it,test"
  )
}

lazy val root = project.in(file(".")).configs(IntegrationTest)
Defaults.itSettings

Revolver.settings
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerExposedPorts := Seq(9000)

dockerEntrypoint := Seq("bin/%s" format executableScriptName.value, "-Dconfig.resource=docker.conf")

parallelExecution in Test := false
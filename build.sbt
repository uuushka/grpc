name := "otus-scala-grpc"

version := "0.1"

scalaVersion := "2.13.4"

val deps = Seq(
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)

lazy val akkaGrpc = (project in file("akkaGrpc"))
  .settings(
    scalaVersion := "2.13.4",
    libraryDependencies ++= deps)
  .enablePlugins(AkkaGrpcPlugin)


lazy val protoGrpc = (project in file("protoGrpc"))
  .settings(libraryDependencies ++= deps ++
    Seq( "com.typesafe.akka" %% "akka-stream" % "2.5.31"),
    scalaVersion := "2.13.4",
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
    ))

lazy val root = (project in file("."))
  .aggregate(akkaGrpc, protoGrpc)


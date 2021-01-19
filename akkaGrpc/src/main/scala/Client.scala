import MainServer.config
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.{Sink, Source}
import otus.scala.grpc.{HelloRequest, HelloWorldClient}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Client extends App {

  implicit val system: ActorSystem = ActorSystem("client", config)

  val client: HelloWorldClient = HelloWorldClient(
    GrpcClientSettings
      .connectToServiceAt("localhost", 8080)
      .withTls(false)
  )

  val s = client.helloStream(Source.repeat(HelloRequest("Hello")))
    .map(resp => println(s"server response: $resp"))
    .runWith(Sink.ignore)

  Await.result(s, Duration.Inf)

}

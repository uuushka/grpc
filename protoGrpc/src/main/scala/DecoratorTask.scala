import akka.Done
import akka.actor.ActorSystem
import otus.scala.grpc.helloworld3.{HelloRequest, HelloResponse}
import otus.scala.grpc.helloworld3.HelloWorldGrpc.{HelloWorld, HelloWorldStub}
import akka.stream.scaladsl.{Sink, Source}
import io.grpc.{ManagedChannel, ManagedChannelBuilder}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/***
   *   Реализуйте методы HelloWorldStubDecorator
   *   черз методы HelloWorldStub
   *   Проверить свою реализаю можно запустив Server
   * */
object DecoratorTask extends App {

  val channel: ManagedChannel = ManagedChannelBuilder
    .forAddress("localhost", 8080)
    .usePlaintext()
    .build()

  val stub: HelloWorldStub = new HelloWorldStub(channel)

  class HelloWorldStubDecorator(underlining: HelloWorld) {
    def hello(request: HelloRequest): scala.concurrent.Future[HelloResponse] = ???
    def helloStream(stream: Source[HelloRequest, akka.NotUsed]): Source[HelloResponse, akka.NotUsed] = ???
  }

  val helloWorldClient = new HelloWorldStubDecorator(stub)

  implicit val system: ActorSystem = ActorSystem("client")

  val s: Future[Done] = helloWorldClient.helloStream(Source.repeat(HelloRequest("Hello")))
    .map(resp => println(s"server response: $resp"))
    .runWith(Sink.ignore)

  Await.result(s, Duration.Inf)

}

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import io.grpc.stub.StreamObserver
import io.grpc.{CallOptions, ManagedChannel, ManagedChannelBuilder}
import otus.scala.grpc.helloworld3.HelloWorldGrpc.{HelloWorld, HelloWorldStub, METHOD_HELLO_STREAM}
import otus.scala.grpc.helloworld3.{HelloRequest, HelloResponse}
import scalapb.grpc.ClientCalls

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
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

    def hello(request: HelloRequest): scala.concurrent.Future[HelloResponse] = Future { HelloResponse(s"Call method hello with $request") }

    def helloStream(stream: Source[HelloRequest, akka.NotUsed]): Source[HelloResponse, akka.NotUsed] = {

      /**
       * Формируем обсёрвер, который будет получает запрос и слушает ответы сервера
       */
      def getObserver: StreamObserver[HelloResponse] => StreamObserver[HelloRequest] =
        (outputObserver: StreamObserver[HelloResponse]) => ClientCalls.asyncBidiStreamingCall(
          channel,
          METHOD_HELLO_STREAM,
          CallOptions.DEFAULT,
          outputObserver
        )

      stream
        .throttle(1, 2.second) // чтобы проще смотреть консоль
        .via(Flow.fromGraph(new CustomGraphObserver[HelloRequest, HelloResponse](getObserver)))
    }
  }


  val helloWorldClient = new HelloWorldStubDecorator(stub)

  implicit val system: ActorSystem = ActorSystem("client")

  val s: Future[Done] = helloWorldClient.helloStream(Source.repeat(HelloRequest("Hello")))
    .map(resp => println(s"server response: $resp"))
    .runWith(Sink.ignore)

  Await.result(s, Duration.Inf)

}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import otus.scala.grpc.{HelloRequest, HelloResponse, HelloWorld, HelloWorldHandler}

import scala.concurrent.Future

object MainServer extends App {

  val config = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())

  implicit val system: ActorSystem = ActorSystem("main", config)

  val handler: HttpRequest => Future[HttpResponse] = HelloWorldHandler(new HelloWorld {
    override def hello(in: HelloRequest): Future[HelloResponse] =
      Future.successful(HelloResponse("Server is up"))

    override def helloStream(in: Source[HelloRequest, NotUsed]): Source[HelloResponse, NotUsed] = {
      Source.unfold(0 -> 1) {
        case (a, _) if a > 10000000 => None
        case (a, b) => Some((b -> (a + b)) -> a)
      }.map(i => HelloResponse(i.toString))
    }
  })

  Http().bindAndHandleAsync(handler, interface = "127.0.0.1", port = 8080)

}

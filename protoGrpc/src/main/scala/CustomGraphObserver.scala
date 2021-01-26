import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.grpc.stub.StreamObserver

/**
 * Кастомный граф, который принимает значение из потока и посредством обсёрвера getObserver
 * передает дальше в пайплайн стрима результат вычисления от сервера
 */
class CustomGraphObserver[I, O](val getObserver: StreamObserver[O] => StreamObserver[I]) extends GraphStage[FlowShape[I, O]] {
  val in: Inlet[I] = Inlet("request.in")
  val out: Outlet[O] = Outlet("response.out")
  override val shape: FlowShape[I, O] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      // обсёрвер над сервером
      val outputObserver = new StreamObserver[O] {
        override def onError(t: Throwable): Unit = println(s"Error [${t.getMessage}]")
        override def onCompleted(): Unit = ()
        override def onNext(value: O): Unit = emit(out, value)
      }

      val inputObserver = getObserver(outputObserver)

      override def preStart(): Unit = pull(in)

      setHandlers(in, out, new InHandler with OutHandler {
        override def onPush(): Unit = {
          val input: I = grab(in)
          if (isAvailable(out)) inputObserver.onNext(input)
          if (!hasBeenPulled(in)) pull(in)
        }

        override def onPull(): Unit = ()
      })
    }
}


package cz.codingmonkeys.dis13

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cz.codingmonkeys.dis13.http.HttpService
import cz.codingmonkeys.dis13.utils.{Config, Migration}

import scala.concurrent.ExecutionContext

object Main extends App with Config with HttpService with Migration {
  protected implicit val system = ActorSystem()

  override protected implicit val executor: ExecutionContext = system.dispatcher
  override protected val log: LoggingAdapter = Logging(system, getClass)
  override protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  migrate()


  Http().bindAndHandle(routes, httpInterface, httpPort)
}

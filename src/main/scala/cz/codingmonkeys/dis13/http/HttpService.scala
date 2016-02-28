package cz.codingmonkeys.dis13.http


import akka.http.scaladsl.server.Directives._
import cz.codingmonkeys.dis13.http.routes.HlaseniServiceRoute
import cz.codingmonkeys.dis13.utils.CorsSupport

/**
  * @author Richard Stefanca
  */
trait HttpService extends HlaseniServiceRoute with CorsSupport {

  val routes = pathPrefix("v5") {
    corsHandler {
      pathPrefix("api") {
        hlaseniRoute
      }
    }
  }

}

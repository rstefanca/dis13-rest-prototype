package cz.codingmonkeys.dis13.http.routes

import akka.event.Logging
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.MalformedRequestContentRejection
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry}
import akka.stream.io.Framing
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import cz.codingmonkeys.dis13.models.{DistribuceRegEntity, HlaseniEntity, LpSummary}
import cz.codingmonkeys.dis13.services.HlaseniService
import cz.codingmonkeys.dis13.utils.AktualniObdobi

import scala.util.Failure

/**
  * @author Richard Stefanca
  */

object HlaseniServiceRoute {
  def requestMethodAsInfo(req: HttpRequest): LogEntry = LogEntry(s"${req.method.toString} ${req.uri.path} ${req.headers}", Logging.InfoLevel)

  val logAsInfo = DebuggingDirectives.logRequest(requestMethodAsInfo _)
  val splitLines = Framing.delimiter(ByteString("\n"), 1024, allowTruncation = true)

  case class DistribuceReg(typPohybu: String, typOdberatele: String, kodSUKL: String, mnozstvi: Int, sarze: String)

  case class SaveStatus(status: String, idPodani: String, verze: Int, invalidSuklCodes: Seq[String])

  private def toHlaseniEntity(dr: DistribuceReg, verze: Int)(implicit distributor: String): HlaseniEntity = {
    val mnozstvi = getMnozstvi(dr.typPohybu, dr.mnozstvi)
    val obdobi = AktualniObdobi()
    DistribuceRegEntity(distributor, obdobi, dr.typOdberatele, dr.kodSUKL, mnozstvi, dr.sarze, verze)
  }

  private def vectorToHlaseniEntity(fields: Vector[String], verze: Int)(implicit distributor: String): HlaseniEntity = {
    val obdobi = AktualniObdobi()
    DistribuceRegEntity(distributor, obdobi, fields(1), fields(2), getMnozstvi(fields(0), fields(3).toInt), fields(4), verze)
  }

  //VRATKA (V) neguje mnozstvi
  private def getMnozstvi(typPohybu: String, puvodniMnozstvi: Int) = typPohybu match {
    case "D" => puvodniMnozstvi
    case "V" => -puvodniMnozstvi
  }

  private def toDistribuceReg(entity: HlaseniEntity) = {
    DistribuceReg(entity.typPohybu, entity.typOdberatele, entity.kodSUKL.getOrElse(""), entity.mnozstviAbs, entity.sarze)
  }
}

trait HlaseniServiceRoute extends HlaseniService with BaseServiceRoute {

  import HlaseniServiceRoute._
  import StatusCodes._

  implicit val distribuceRegFormat = jsonFormat5(DistribuceReg)
  implicit val summaryFormat = jsonFormat2(LpSummary)
  implicit val saveStatusFormat = jsonFormat4(SaveStatus)

  private implicit val distributor = "0000000075"

  val hlaseniRoute = pathPrefix("hvlp") {
    logAsInfo {
      pathPrefix("distribuce") {
        pathPrefix("reg") {
          get {
            complete(getHlaseni(distributor, AktualniObdobi()).map(rows => rows.map(toDistribuceReg)))
          } ~
            post {
              entity(as[List[DistribuceReg]]) { distReg =>
                val source = Source(distReg)
                doSave(source, toHlaseniEntity)
              } ~ fileUpload("file") {
                case (_, byteSource) =>
                  val source = byteSource
                    .via(splitLines)
                  doSave(source, (line: ByteString, verze) => vectorToHlaseniEntity(line.utf8String.trim.replace("\"", "").split(",").toVector, verze))
              }
            }
        }
      } ~ pathPrefix("summary") {
        get {
          complete(LpSummary(Array("rok", "mesic", "kod", "bilance"), Array(
            Array("2016", "01", "1234567", "20"),
            Array("2016", "02", "1234567", "20")
          )))
        }
      }
    }
  }

  private def doSave[A](src: Source[A, Any], mapper: (A, Int) => HlaseniEntity) = {
    val futureSave = getMaxVerze(distributor, AktualniObdobi()).flatMap {
      verze =>
        log.info(s"Aktualni verze hlaseni: $verze")
        val aktualniVerze = verze.fold(0)(_ + 1)
        src
          .map(mapper(_, aktualniVerze))
          .via(createHlaseni(200))
          //.runWith(Sink.fold[(Int, Set[String]), String]((0, Set[String]())) { (set, str) => (aktualniVerze, set._2 + str) })
          .runWith(Sink.fold[Set[String], String](Set[String]()) { (set, str) => set + str }).map((aktualniVerze, _))
    }

    onComplete(futureSave) {
      case scala.util.Success(result) => complete(Created -> SaveStatus("OK", java.util.UUID.randomUUID().toString, result._1, result._2.toList.sorted))
      case Failure(x) => reject(MalformedRequestContentRejection(x.getMessage, Option(x.getCause)))
    }
  }
}

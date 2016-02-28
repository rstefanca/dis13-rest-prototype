package cz.codingmonkeys.dis13.services

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.pattern.CircuitBreaker
import akka.stream.FlowShape
import akka.stream.scaladsl._
import cz.codingmonkeys.dis13.models.db.{HlaseniTable, NeplatneKodyTable}
import cz.codingmonkeys.dis13.models.{HlaseniEntity, NeplatnyKodEntity}
import cz.codingmonkeys.dis13.utils.Obdobi

import scala.concurrent.Future


/**
  * @author Richard Stefanca
  */


trait HlaseniService extends HlaseniTable with NeplatneKodyTable with KodSUKLValidationService {

  protected def system: ActorSystem

  protected def log: LoggingAdapter

  import dbConfig.driver.api._

  import scala.concurrent.duration._

  lazy val breaker =
    new CircuitBreaker(system.scheduler, maxFailures = 3, callTimeout = 10.seconds, resetTimeout = 1.minute)(system.dispatcher)

  implicit private lazy val dbDispatcher = system.dispatchers.lookup("db-dispatcher")

  import slick.dbio.DBIOAction


  private def actionWithBreaker[R](dbAction: DBIOAction[R, NoStream, Nothing]): Future[R] = {
    breaker.withCircuitBreaker(db.run(dbAction))
  }

  def createHlaseni(hlaseni: HlaseniEntity): Future[HlaseniEntity] = breaker.withCircuitBreaker(db.run(hlasenis returning hlasenis += hlaseni))

  def createHlaseni(hlaseni: Seq[HlaseniEntity]): Future[List[HlaseniEntity]] = actionWithBreaker {
    hlasenis returning hlasenis ++= hlaseni
  }.map(_.toList)

  /**
    * Vraci Flow pro ukladani hlaseni. [[HlaseniEntity]] ~> [neplatny kod sukl jako String]
    *
    * @param groupSize velikost batche
    * @return Flow
    */
  def createHlaseni(groupSize: Int = 250): Flow[HlaseniEntity, String, Any] = {
    Flow[HlaseniEntity].grouped(groupSize)
      .mapAsync(8)(createHlaseni)
      .mapConcat(identity)
      .via(kodSUKLValidation)
      .via(saveInvalidKodSUKLs)
  }

  def getHlaseni(distributor: String, obdobi: Obdobi = Obdobi()): Future[Seq[HlaseniEntity]] = {
    breaker.withCircuitBreaker(db.run(hlaseniByDistributorAndObdobiCompiled(distributor, obdobi.rok, obdobi.mesic).result))
  }

  def getMaxVerze(distributor: String, obdobi: Obdobi = Obdobi()): Future[Option[Int]] = actionWithBreaker {
    log.info(s"getMaxVerze($distributor, $obdobi)")
    maxVerzeCompiled(distributor, obdobi.rok, obdobi.mesic).result
  }

  /**
    * Flow filtruje instance [[HlaseniEntity]] s nevalidnim kodem SUKL
    */
  private val kodSUKLValidation = Flow[HlaseniEntity].grouped(100).mapAsyncUnordered(4)(entities => {
    def getInvalidEntities(code: String) = entities.filter(_.kodSUKL.contains(code))
    validateKodSUKLs(entities.flatMap(_.kodSUKL)).map(_.flatMap(getInvalidEntities)).map(_.toList)
  }).mapConcat(identity)

  /**
    * Sink pro instance [[NeplatnyKodEntity]]. Instance jsou po davkach ukladany do db
    */
  private val invalidKodSUKLSink = Flow[NeplatnyKodEntity].grouped(100).to(Sink.foreach { invalidEntries =>
    log.info(s"Ukladam neplatne kody $invalidEntries")
    breaker.withCircuitBreaker(db.run(neplatneKody ++= invalidEntries))
  })

  /**
    * Flow s broadcastem, ktery predava neplatne kody na vystup pro dalsi zpracovani a do invalidKodSUKLSink predava entity [[NeplatnyKodEntity]]
    */
  private val saveInvalidKodSUKLs: Flow[HlaseniEntity, String, Any] = Flow.fromGraph(GraphDSL.create() { implicit builder =>

    import GraphDSL.Implicits._

    val bcast = builder.add(Broadcast[HlaseniEntity](2))

    val invalidKodSUKL = Flow[HlaseniEntity].map(e => NeplatnyKodEntity(None, e.id.get))
    val toKodSUKL = builder.add(Flow[HlaseniEntity].map(_.kodSUKL.get))
    bcast ~> invalidKodSUKL ~> invalidKodSUKLSink
    bcast ~> toKodSUKL

    FlowShape(bcast.in, toKodSUKL.out)
  })
}

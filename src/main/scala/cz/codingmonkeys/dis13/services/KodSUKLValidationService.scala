package cz.codingmonkeys.dis13.services

import akka.actor.{ActorPath, ActorSystem}
import akka.cluster.client.ClusterClient.Send
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern._
import akka.util.Timeout
import cz.tronevia.api.cis.protocol.CisProtocol.{InvalidCodesResponse, ReturnInvalidSuklCodes}

import scala.concurrent.Future

/**
  * Uses [[ClusterClient]] to talk with [[CisActor]] on cis akka cluster
  *
  * @author Richard Stefanca
  */
trait KodSUKLValidationService {

  protected def system: ActorSystem

  //TODO konfigurace
  val initialContacts = Set(
    ActorPath.fromString("akka.tcp://cis@10.152.10.50:2551/system/receptionist"),
    ActorPath.fromString("akka.tcp://cis@10.152.10.50:2552/system/receptionist")
  )

  private lazy val validator = system.actorOf(ClusterClient.props(ClusterClientSettings(system).withInitialContacts(initialContacts)), "cis-client")

  import scala.concurrent.duration._

  implicit val to = Timeout(5.seconds)

  private def send(query: AnyRef): Send = {
    //TODO konfigurace
    ClusterClient.Send("/user/cisActor", query, localAffinity = false)
  }

  def validateKodSUKLs(suklCodes: Seq[String]): Future[Seq[String]] = {
    val start = System.currentTimeMillis()
    (validator ? send(ReturnInvalidSuklCodes(suklCodes))).mapTo[InvalidCodesResponse].map { r =>
      system.log.info(s"Validation time: {}", System.currentTimeMillis() - start)
      r.invalidCodes
    }(system.dispatcher)
  }
}

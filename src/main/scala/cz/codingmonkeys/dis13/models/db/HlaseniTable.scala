package cz.codingmonkeys.dis13.models.db

import cz.codingmonkeys.dis13.models.HlaseniEntity
import cz.codingmonkeys.dis13.utils.DbConfig


/**
  * @author Richard Stefanca
  */
trait HlaseniTable extends DbConfig {

  //import driver.api._
  import dbConfig.driver.api._


  class Hlaseni(tag: Tag) extends Table[HlaseniEntity](tag, "hlaseni13") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def verze = column[Int]("verze")

    def typ = column[String]("typ")

    def distributor = column[String]("distributor")

    def rok = column[Int]("rok")

    def mesic = column[Int]("mesic")

    def typOdberatele = column[String]("typ_odberatele")

    def kodSukl = column[Option[String]]("kod_sukl")

    def mnozstvi = column[Int]("mnozstvi")

    def sarze = column[String]("sarze")

    def * = (id, typ, distributor, rok, mesic, typOdberatele, kodSukl, mnozstvi, sarze, verze) <>((HlaseniEntity.apply _).tupled, HlaseniEntity.unapply)
  }

  protected val hlasenis = TableQuery[Hlaseni]

  private def hlaseniByDistributorAndObdobi(distributor: Rep[String], rok: Rep[Int], mesic: Rep[Int]) =
    for {
      hlaseni <- hlasenis if hlaseni.distributor === distributor && hlaseni.rok === rok && hlaseni.mesic === mesic
    } yield hlaseni

  val hlaseniByDistributorAndObdobiCompiled = Compiled(hlaseniByDistributorAndObdobi _)

  private def getMaxVerze(distributor: Rep[String], rok: Rep[Int], mesic: Rep[Int]) = {
    val q = for {
      hlaseni <- hlasenis if hlaseni.distributor === distributor && hlaseni.rok === rok && hlaseni.mesic === mesic
    } yield hlaseni.verze

    q.max
  }

  val maxVerzeCompiled = Compiled(getMaxVerze _)


}

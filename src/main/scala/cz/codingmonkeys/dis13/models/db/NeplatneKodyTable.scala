package cz.codingmonkeys.dis13.models.db

import cz.codingmonkeys.dis13.models.NeplatnyKodEntity
import cz.codingmonkeys.dis13.utils.DbConfig

/**
  * @author Richard Stefanca
  */
trait NeplatneKodyTable extends DbConfig {
  import dbConfig.driver.api._

  class NeplatneKody(tag: Tag) extends Table[NeplatnyKodEntity](tag, "neplatne_kody") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def idHlaseni = column[Long]("id_hlaseni")

    def * = (id, idHlaseni) <> ((NeplatnyKodEntity.apply _).tupled, NeplatnyKodEntity.unapply)


  }

  protected val neplatneKody = TableQuery[NeplatneKody]
}

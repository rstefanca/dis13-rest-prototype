package cz.codingmonkeys.dis13.models

import cz.codingmonkeys.dis13.utils.Obdobi

/**
  * @author Richard Stefanca
  */
case class HlaseniEntity(id: Option[Long] = None, typ: String, distributor: String, rok: Int, mesic: Int, typOdberatele: String, kodSUKL: Option[String], mnozstvi: Int, sarze: String, verze: Int = 1) {

  def typPohybu = if (mnozstvi > 0) "D" else "V"

  def mnozstviAbs = Math.abs(mnozstvi)

  def isReg = kodSUKL.isDefined

}

object HlaseniEntity {
  val DistribuceReg = "DIST_REG"
}


object DistribuceRegEntity {

  import HlaseniEntity._

  def apply(distributor: String, obdobi: Obdobi, typOdberatele: String, kodSUKL: String, mnozstvi: Int, sarze: String, verze: Int = 1): HlaseniEntity = {
    HlaseniEntity(None, DistribuceReg, distributor, obdobi.rok, obdobi.mesic, typOdberatele, Some(kodSUKL), mnozstvi, sarze, verze)
  }
}



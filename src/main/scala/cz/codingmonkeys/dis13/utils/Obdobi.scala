package cz.codingmonkeys.dis13.utils

import java.util.Calendar


/**
 * @author Richard Stefanca
 */
case class Obdobi(rok: Int, mesic: Int) {

}

object Obdobi {
  def apply(): Obdobi = {
    val cal = Calendar.getInstance
    Obdobi(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
  }
}

object AktualniObdobi {

  def apply(): Obdobi = {
    val obdobi = Obdobi()
    val cal = Calendar.getInstance
    val den = cal.get(Calendar.DAY_OF_MONTH)
    if (den > 20) obdobi else predchozi(obdobi)
  }

  private def predchozi(obdobi: Obdobi): Obdobi = {
    val mesic = obdobi.mesic - 1
    val rok = if (mesic == 0) obdobi.rok - 1 else obdobi.rok
    Obdobi(rok = rok, mesic = mesic)
  }

}

package org.triplepoint.ScalaCrazyflie

/**
 * This class represents a CrazyRadio, and the CRTP protocol
 * it uses to communicate.
 */

object CrazyRadio {
  def apply() = new CrazyRadio(CrazyRadioUsb())
}

class CrazyRadio(val transportHardware: CrazyRadioUsb)
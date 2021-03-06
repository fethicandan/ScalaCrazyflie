package org.triplepoint.ScalaCrazyflie

object Main extends App {

  // ## Testing!
  // Eventually, we want to not talk to the radio directly and instead
  //  talk to a CrazyFlie object that owns a radio object.
  val radio = CrazyRadio()

  println("Radio Firmware Version", radio.firmwareVersion)

  radio.scanChannels()
}

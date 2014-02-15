package org.triplepoint.ScalaCrazyflie

import de.ailis.usb4java.utils._

object Main extends App {

  // ## Testing!
  // Eventually, we want to not talk to the radio directly and instead
  //  talk to a CrazyFlie object that owns a radio object.  But here we are for
  //  testing.
  val radio = CrazyRadio()
  val usbDevice = radio.usbDevice
  val desc = usbDevice.getUsbDeviceDescriptor

  // Print out the device description
  println(DescriptorUtils.dump(desc))

}

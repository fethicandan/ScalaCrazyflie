package org.triplepoint.ScalaCrazyflie

import javax.usb._

object Main extends App {

  // ## Testing!
  val rootHub = UsbHostManager.getUsbServices.getRootUsbHub
  println(rootHub.isRootUsbHub)

  // Eventually, we want to not talk to the radio directly and instead
  //  talk to a CrazyFlie object that owns a radio object.  But here we are for
  //  testing.
  val radio = CrazyRadio()

  val javaUsbDevice = radio.transportHardware.usbDevice
  val desc = javaUsbDevice.getUsbDeviceDescriptor

  println(f"${desc.idVendor() & 0xffff}%04x:${desc.idProduct() & 0xffff}%04x")

}

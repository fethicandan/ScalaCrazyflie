package org.triplepoint.ScalaCrazyflie

object Main extends App {
  val radio = CrazyRadio()

  // Testing
  val desc = radio.usbDevice.javaUsbDevice.getUsbDeviceDescriptor
  println(f"${desc.idVendor() & 0xffff}%04x:${desc.idProduct() & 0xffff}%04x")
}




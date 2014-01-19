package org.triplepoint.ScalaCrazyflie

import javax.usb._
import scala.collection.JavaConversions._

object Main extends App {
  println("test")

  val services = UsbHostManager.getUsbServices
  val rootHub = services.getRootUsbHub

  dump(rootHub)

  def dump(device: UsbDevice): Unit = {
    val desc = device.getUsbDeviceDescriptor

    // Print out the vendor and product IDs as hex values
    println(f"${desc.idVendor() & 0xffff}%04x:${desc.idProduct() & 0xffff}%04x")

    if (device.isUsbHub) {
      val hub = device.asInstanceOf[UsbHub]
      for (child <- hub.getAttachedUsbDevices) {
        dump(child.asInstanceOf[UsbDevice])
      }
    }
  }
}




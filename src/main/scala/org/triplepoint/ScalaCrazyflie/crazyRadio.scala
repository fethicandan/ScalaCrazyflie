package org.triplepoint.ScalaCrazyflie

import javax.usb._
import scala.collection.JavaConverters._

object CrazyRadio {
  val CrazyRadioVendorId = 0x1915
  val CrazyRadioProductId = 0x7777

  /**
   * traverse the tree of USB devices and pull out the one that matches.
   */
  def apply() = {
    val rootHub = UsbHostManager.getUsbServices.getRootUsbHub

    val foundDevice = findDevice(rootHub, CrazyRadioVendorId, CrazyRadioProductId) match {
      case Some(x) => x
      case None    => throw new RuntimeException("Couldn't find the device.")
    }

    new CrazyRadio(foundDevice)
  }

  /**
   * Given a USB hub device, try to find an object in the hierarchy that matches
   * the given vendor and product IDs.
   */
  protected def findDevice(device: UsbDevice, vendorId: Int, productId: Int): Option[UsbDevice] = device match {
    case x: UsbDevice
      if x.getUsbDeviceDescriptor.idVendor  == vendorId &&
         x.getUsbDeviceDescriptor.idProduct == productId
      => Some(x)

    case x: UsbDevice if x.isUsbHub =>
      val listAttachedDevices = x.asInstanceOf[UsbHub].getAttachedUsbDevices.asScala.toList.map(_.asInstanceOf[UsbDevice])
      listAttachedDevices.map(x => findDevice(x, vendorId, productId)).flatten.headOption

    case _ => None
  }
}

class CrazyRadio(val usbDevice: UsbDevice)
package org.triplepoint.ScalaCrazyflie

import javax.usb._
import scala.collection.JavaConverters._

object CrazyRadioUsb {
  val vendorId = 0x1915
  val productId = 0x7777

  /**
   * traverse the tree of USB devices and pull out the one that matches.
   */
  def apply() = {
    val rootHub = UsbHostManager.getUsbServices.getRootUsbHub

    val foundDevice = findDevice(rootHub, vendorId, productId) match {
      case Some(x) => x
      case None    => throw new RuntimeException("Couldn't find the device.")
    }

    new CrazyRadioUsb(foundDevice)
  }

  /**
   * Given a USB hub device, try to find an object in the hierarchy that matches
   * the given vendor and product IDs.
   */
  protected def findDevice(hub: UsbHub, vendorId: Int, productId: Int): Option[UsbDevice] = {

    def flattenListOfUsbDevices(devices: List[UsbDevice]): List[UsbDevice] = devices flatMap {
      case x: UsbDevice if x.isUsbHub =>
          val listSubDevices = x.asInstanceOf[UsbHub].getAttachedUsbDevices.asScala.toList.map(_.asInstanceOf[UsbDevice])
          x :: flattenListOfUsbDevices(listSubDevices)
      case x: UsbDevice =>
        List(x)
    }

    flattenListOfUsbDevices(List(hub))
      .filter(_.getUsbDeviceDescriptor.idVendor == vendorId)
      .filter(_.getUsbDeviceDescriptor.idProduct == productId)
      .headOption
  }
}

class CrazyRadioUsb(val usbDevice: UsbDevice)
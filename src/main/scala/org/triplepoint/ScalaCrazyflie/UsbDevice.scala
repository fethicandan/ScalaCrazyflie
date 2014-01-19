package org.triplepoint.ScalaCrazyflie

import javax.usb.{UsbDevice => JavaxUsbDevice}
import javax.usb._
import scala.collection.JavaConverters._

object UsbDevice {
  /**
   * Given a vendor and product ID, traverse the tree
   * of USB devices and pull out the one that matches.
   */
  def apply(vendorId: Int, productId: Int) = {
    def flattenListOfUsbDevices(device: JavaxUsbDevice): List[JavaxUsbDevice] = {
      recursiveFlattenListOfUsbDevices(List(device))

      // This helps us deal with the List that UsbHub.getAttachedUsbDevices() returns.
      implicit def javaListToUsbDeviceList(x: java.util.List[_]): List[JavaxUsbDevice] =
        x.asScala.toList.map(_.asInstanceOf[JavaxUsbDevice])

      def recursiveFlattenListOfUsbDevices(devices: List[JavaxUsbDevice]): List[JavaxUsbDevice] =
        devices flatMap {
          case x: JavaxUsbDevice if x.isUsbHub => x :: recursiveFlattenListOfUsbDevices(x.asInstanceOf[UsbHub].getAttachedUsbDevices)
          case x: JavaxUsbDevice               => List(x)
        }
    }

    val rootHub = UsbHostManager.getUsbServices.getRootUsbHub
    val foundDevice :: restOfList = for (
      device <- flattenListOfUsbDevices(rootHub)
      if device.getUsbDeviceDescriptor.idVendor == vendorId
      if device.getUsbDeviceDescriptor.idProduct == productId
    ) yield device

    // TODO - what happens here if nothing is found?  Option?

    new UsbDevice(foundDevice)
  }
}

class UsbDevice(val javaUsbDevice: JavaxUsbDevice) {

}

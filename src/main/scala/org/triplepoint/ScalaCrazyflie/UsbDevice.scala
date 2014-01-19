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

      // This helps us deal with the List that UsbHub.getAttachedUsbDevices() returns.
      implicit def javaListToUsbDeviceList(x: java.util.List[_]): List[JavaxUsbDevice] =
        x.asScala.toList.map(_.asInstanceOf[JavaxUsbDevice])

      def recursiveFlattenListOfUsbDevices(devices: List[JavaxUsbDevice]): List[JavaxUsbDevice] =
        devices flatMap {
          case x: JavaxUsbDevice if x.isUsbHub => x :: recursiveFlattenListOfUsbDevices(x.asInstanceOf[UsbHub].getAttachedUsbDevices)
          case x: JavaxUsbDevice               => List(x)
        }

      recursiveFlattenListOfUsbDevices(List(device))
    }

    val rootHub = UsbHostManager.getUsbServices.getRootUsbHub
    val foundDevice = flattenListOfUsbDevices(rootHub).filter(x => {
        val desc = x.getUsbDeviceDescriptor
        (desc.idVendor == vendorId) && (desc.idProduct == productId)
      }
    ).head

    // TODO - what happens here if nothing is found?  Option?

    new UsbDevice(foundDevice)
  }
}

class UsbDevice(val javaUsbDevice: JavaxUsbDevice) {

}

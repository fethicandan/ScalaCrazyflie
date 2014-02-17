package org.triplepoint.ScalaCrazyflie

import javax.usb._
import de.ailis.usb4java.utils.DescriptorUtils
import scala.collection.JavaConverters._

object CrazyRadio {
  // USB Device parameters
  val CrazyRadioVendorId = 0x1915
  val CrazyRadioProductId = 0x7777

  // Radio Configuration Message Types
  val SetRadioChannel = 0x01
  val SetRadioAddress = 0x02
  val SetDataRate = 0x03
  val SetRadioPower = 0x04
  val SetRadioARD = 0x05 // (Automatic Retransmit Delay)
  val SetRadioARC = 0x06 // (Ack Retry Count)
  val AckEnable = 0x10
  val SetContinuousCarrier = 0x20
  val StartScanChannels = 0x21
  val GetScanChannels = 0x21
  val LaunchBootLoader = 0xFF

  // Enumerate the possible values for the radio data rate setting.
//  sealed class DataRate extends Enumeration {
//    type DataRate = Value
//    val 250Kbps, 1Mbps, 2Mbps = Value
//  }
//
//  // Enumerate the possible values for the radio power setting.
//  sealed class Power extends Enumeration {
//    type Power = Value
//    val Minus18dBm, Minus12dBm, Minus6dBm, ZerodBm = Value
//  }

  /**
   * traverse the tree of USB devices and pull out the first one that matches the CrazyRadio vendor and product IDs.
   */
  def apply() = {
    val rootHub = UsbHostManager.getUsbServices.getRootUsbHub

    val foundUsbDevice = findUsbDevice(rootHub, CrazyRadioVendorId, CrazyRadioProductId) match {
      case Some(x) => x
      case None    => throw new RuntimeException("Couldn't find the requested device.")
    }

    new CrazyRadio(foundUsbDevice)
  }

  /**
   * Given a USB hub device, try to find an object in the hierarchy that matches
   * the given vendor and product IDs.
   */
  protected def findUsbDevice(device: UsbDevice, vendorId: Int, productId: Int): Option[UsbDevice] = device match {
    case x: UsbDevice
      if x.getUsbDeviceDescriptor.idVendor  == vendorId &&
         x.getUsbDeviceDescriptor.idProduct == productId
      => Some(x)

    case x: UsbDevice if x.isUsbHub =>
      val listAttachedDevices = x.asInstanceOf[UsbHub].getAttachedUsbDevices.asScala.toList.map(_.asInstanceOf[UsbDevice])
      listAttachedDevices
        .map(x => findUsbDevice(x, vendorId, productId))
        .flatten
        .headOption

    case _ => None
  }
}

/**
 * For further reference, see http://wiki.bitcraze.se/projects:crazyradio:protocol
 *
 * Also, take a look at the reference implementation in Python, from BitCraze
 * https://github.com/bitcraze/crazyflie-clients-python/blob/master/lib/cflib/drivers/crazyradio.py
 *
 * For details on how the low-level USB elements function, see the JSR-80 specification at:
 * http://javax-usb.sourceforge.net/jsr80.pdf
 */
class CrazyRadio(val usbDevice: UsbDevice) {

  val firmwareVersion = DescriptorUtils.decodeBCD(usbDevice.getUsbDeviceDescriptor.bcdDevice).toFloat

  assert(firmwareVersion >= 0.4, "Firmware version must be 0.4 or greater.")

  initializeUsbDevice()
  initializeRadio()

  def initializeUsbDevice() = {
    // @TODO Set the active configuration to "1" (I think this is already the case)
    // @TODO claim the "0" interface
  }

  def initializeRadio() = {
    setDataRate(2) // @TODO This should be an enum
    setChannel(2)

    // firmware v0.4+ elements
    setContinuousCarrierMode(false)
    //setAddress(0xE7E7E7E7E7)  // @Todo this is a 64-bit unsigned value.  Not sure how to represent this.
    setPower(3)  // @TODO this should be part of an enum
//    setAutomaticRetryDelay() // @TODO This isn't nailed down yet
//    setAutomaticRetryCount() // @TODO This isn't nailed down yet
//    configureAutomaticAck() // @TODO This isn't nailed down yet
  }

  def setChannel(channel: Int) = {
    require(channel >= 0, "The radio channel must be greater than or equal to 0.")
    require(channel <= 125, "The radio channel must be less than or equal to 125.")
    println(channel)
  }

  // @TODO this takes a 64-bit unsigned value, which the JVM cannot represent. Figure this out.
  def setAddress(address: Int) = {
    println(address)
  }

  def setDataRate(dataRate: Int) = {
    // @todo should be an enum
    println(dataRate.id)
  }

  def setPower(power: Int) = {
    // @todo should be an enum
    println(power.id)
  }

  // @ TODO look at how these are configured
  def setAutomaticRetryDelay(microseconds: Int) = {
    // @TODO Convert the microseconds to a hexvalue
    // Also, note that this can be configured in two different ways, by wait time or by payload length.  Figure that out.
  }

  def setAutomaticRetryCount(count: Int) = {
    require(count >= 0, "The automatic retry count must be greater than or equal to 0.")
    require(count <= 15, "The automatic retry count must be less than or equal to 15.")
  }

  def configureAutomaticAck(enabled: Boolean) = {
  }

  def setContinuousCarrierMode(active: Boolean) = {
  }

  def scanChannels() = {
    throw new RuntimeException("This feature is not yet implemented.")
  }

  def launchBootLoader() = {
    throw new RuntimeException("This feature is not yet implemented.")
  }
}
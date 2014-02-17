package org.triplepoint.ScalaCrazyflie

import javax.usb._
import de.ailis.usb4java.utils.DescriptorUtils
import scala.collection.JavaConverters._
import org.triplepoint.ScalaCrazyflie.CrazyRadio.DataRate
import org.triplepoint.ScalaCrazyflie.CrazyRadio.DataRate.DataRate
import org.triplepoint.ScalaCrazyflie.CrazyRadio.Power
import org.triplepoint.ScalaCrazyflie.CrazyRadio.Power.Power

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
  object DataRate extends Enumeration {
    type DataRate = Value
    val `250Kbps` = Value(0)
    val `1Mbps`   = Value(1)
    val `2Mbps`   = Value(2)
  }

  // Enumerate the possible values for the radio power setting.
  object Power extends Enumeration {
    type Power = Value
    val Minus18dBm = Value(0)
    val Minus12dBm = Value(1)
    val Minus6dBm  = Value(2)
    val `0dBm`     = Value(3)
  }

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
    setChannel(2)
    setAddress(BigInt("E7E7E7E7E7", 16))
    setDataRate(DataRate.`2Mbps`)
    setPower(Power.`0dBm`)
//    setAutomaticRetryDelay() // @TODO This isn't nailed down yet
//    setAutomaticRetryCount() // @TODO This isn't nailed down yet
    //    setAutomaticAck() // @TODO This isn't nailed down yet
    setContinuousCarrierMode(false)
  }

  def setChannel(channel: Int) = {
    require(channel >= 0, "The radio channel must be greater than or equal to 0.")
    require(channel <= 125, "The radio channel must be less than or equal to 125.")
    println(channel)
  }

  def setAddress(address: BigInt) = {
    require(address >= 0, "The radio address must be greater than or equal to 0F.")
    require(address <= BigInt("FFFFFFFFFF", 16), "The radio address must be less than or equal to 0xFFFFFFFFFF.")
    println(address)
  }

  def setDataRate(dataRate: DataRate) = {
    println(dataRate.id)
  }

  def setPower(power: Power) = {
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

  def setAutomaticAck(enabled: Boolean) = {
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
package org.triplepoint.ScalaCrazyflie

import scala.collection.JavaConverters._
import javax.usb._
import org.usb4java.DescriptorUtils
import org.triplepoint.ScalaCrazyflie.CrazyRadio.DataRate
import org.triplepoint.ScalaCrazyflie.CrazyRadio.DataRate.DataRate
import org.triplepoint.ScalaCrazyflie.CrazyRadio.Power
import org.triplepoint.ScalaCrazyflie.CrazyRadio.Power.Power

object CrazyRadio {

  /**
   * USB Device parameters
   */
  val CrazyRadioVendorId: Short = 0x10c4//0x1915
  val CrazyRadioProductId: Short = 0x84c4

  /**
   * Radio Configuration Messages (request type, request)
   */
  val SetRadioChannel: (Byte, Byte) = (0x40, 0x01)
  val SetRadioAddress: (Byte, Byte) = (0x40, 0x02)
  val SetDataRate: (Byte, Byte) = (0x40, 0x03)
  val SetRadioPower: (Byte, Byte) = (0x40, 0x04)
  val SetRadioARD: (Byte, Byte) = (0x40, 0x05) // (Automatic Retransmit Delay)
  val SetRadioARC: (Byte, Byte) = (0x40, 0x06) // (Ack Retry Count)
  val AckEnable: (Byte, Byte) = (0x40, 0x10)
  val SetContinuousCarrier: (Byte, Byte) = (0x40, 0x20)
  val StartScanChannels: (Byte, Byte) = (0x40, 0x21)
  val GetScanChannels: (Byte, Byte) = (0xC0.toByte, 0x21)
  val LaunchBootLoader: (Byte, Byte) = (0x40, 0xFF.toByte)

  /**
   * Enumerate the possible values for the radio data rate setting.
   */
  object DataRate extends Enumeration {
    type DataRate = Value
    val `250Kbps` = Value(0)
    val `1Mbps`   = Value(1)
    val `2Mbps`   = Value(2)
  }

  /**
   * Enumerate the possible values for the radio power setting.
   */
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
    val foundUsbDevice = findUsbDevice(rootHub, CrazyRadioVendorId, CrazyRadioProductId)
      .getOrElse {
        throw new RuntimeException("Couldn't find the requested device.")
      }

    new CrazyRadio(foundUsbDevice)
  }

  /**
   * Given a USB hub device, try to find the first object in the hierarchy that matches
   * the given vendor and product IDs.
   */
  protected def findUsbDevice(device: UsbDevice, vendorId: Int, productId: Int): Option[UsbDevice] = device match {
    case x: UsbDevice if (x.getUsbDeviceDescriptor.idVendor, x.getUsbDeviceDescriptor.idProduct) == (vendorId, productId)
      => Some(x)

    case x: UsbDevice if x.isUsbHub
      => x.asInstanceOf[UsbHub].getAttachedUsbDevices.asScala
        .flatMap(y => findUsbDevice(y.asInstanceOf[UsbDevice], vendorId, productId))
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
  require(firmwareVersion >= 0.4, "Firmware version must be 0.4 or greater.")

  //var endpointIn: Option[UsbEndpoint] = getEndpointIn
  //var endpointOut: Option[UsbEndpoint] = getEndpointOut

//  initDevice()

  val irp = usbDevice.createUsbControlIrp(
    (UsbConst.REQUESTTYPE_DIRECTION_IN
      | UsbConst.REQUESTTYPE_TYPE_STANDARD
      | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE).toByte,
    UsbConst.REQUEST_GET_CONFIGURATION,
    0.toShort,
    0.toShort
  )

  irp.setData(List(1.toByte).toArray)
  usbDevice.syncSubmit(irp)
  println(irp.getData().head)




  //setChannel(2)

//  protected def getEndpointIn = {
//    val deviceConfiguration = usbDevice.getActiveUsbConfiguration
//    val deviceConfigurationDescriptor = deviceConfiguration.getUsbConfigurationDescriptor
//
//    require(deviceConfigurationDescriptor.bNumInterfaces == 1, "Could not find device interface")
//
//    val deviceInterface = deviceConfiguration.getUsbInterface(0.toByte)
//    val deviceInterfaceDescription = deviceInterface.getUsbInterfaceDescriptor
//
//    require(deviceInterfaceDescription.bNumEndpoints == 2, "Could not find device interface endpoints")
//
//    deviceInterface.getUsbEndpoints.asScala
//      .map(_.asInstanceOf[UsbEndpoint])
//      .find(_.getDirection == UsbConst.ENDPOINT_DIRECTION_IN)
//  }
//
//  protected def getEndpointOut = {
//    val deviceConfiguration = usbDevice.getActiveUsbConfiguration
//    val deviceConfigurationDescriptor = deviceConfiguration.getUsbConfigurationDescriptor
//
//    require(deviceConfigurationDescriptor.bNumInterfaces == 1, "Could not find device interface")
//
//    val deviceInterface = deviceConfiguration.getUsbInterface(0.toByte)
//    val deviceInterfaceDescription = deviceInterface.getUsbInterfaceDescriptor
//
//    require(deviceInterfaceDescription.bNumEndpoints == 2, "Could not find device interface endpoints")
//
//    deviceInterface.getUsbEndpoints.asScala
//      .map(_.asInstanceOf[UsbEndpoint])
//      .find(_.getDirection == UsbConst.ENDPOINT_DIRECTION_OUT)
//  }

//  def initializeRadio() = {
//    //setChannel(2)
//    //setAddress(BigInt("E7E7E7E7E7", 16)) // @TODO I think the usb library is expecting an array of bytes, not a bigint
//    //setDataRate(DataRate.`2Mbps`)
//    //setPower(Power.`0dBm`)
//    //setAutomaticRetryDelay() // @TODO This isn't nailed down yet
//    //setAutomaticRetryCount() // @TODO This isn't nailed down yet
//    //setAutomaticAck()        // @TODO This isn't nailed down yet
//    //setContinuousCarrierMode(false)
//  }
//
  def setChannel(channel: Short) = {
    require(channel >= 0, "The radio channel must be greater than or equal to 0.")
    require(channel <= 125, "The radio channel must be less than or equal to 125.")
    sendControlRequest(CrazyRadio.SetRadioChannel, channel, 0)
  }
//
//  def setAddress(address: BigInt) = {
//    require(address >= 0, "The radio address must be greater than or equal to 0F.")
//    require(address <= BigInt("FFFFFFFFFF", 16), "The radio address must be less than or equal to 0xFFFFFFFFFF.")
//    //sendControlRequest(CrazyRadio.SetRadioAddress, 0, 0, address) // @TODO address appears to be an array of bytes?
//  }
//
//  def setDataRate(dataRate: DataRate) = {
//    sendControlRequest(CrazyRadio.SetDataRate, dataRate.id.toShort, 0)
//  }
//
//  def setPower(power: Power) = {
//    sendControlRequest(CrazyRadio.SetRadioPower, power.id.toShort, 0)
//  }
//
//  // @ TODO look at how these are configured
//  def setAutomaticRetryDelay(microseconds: Int) = {
//    // @TODO Convert the microseconds to a hexvalue
//    // Also, note that this can be configured in two different ways, by wait time or by payload length.  Figure that out.
//    // sendControlRequest(CrazyRadio.SetRadioARD, microseconds, 0)
//  }
//
//  def setAutomaticRetryCount(count: Short) = {
//    require(count >= 0, "The automatic retry count must be greater than or equal to 0.")
//    require(count <= 15, "The automatic retry count must be less than or equal to 15.")
//    sendControlRequest(CrazyRadio.SetRadioARC, count, 0)
//  }
//
//  def setAutomaticAck(enabled: Boolean) = {
//    val value = (if (enabled) 1 else 0).toShort
//    sendControlRequest(CrazyRadio.AckEnable, value, 0)
//  }
//
//  def setContinuousCarrierMode(active: Boolean) = {
//    val value = (if (active) 1 else 0).toShort
//    sendControlRequest(CrazyRadio.SetContinuousCarrier, value, 0)
//  }
//
//  def scanChannels() = {
//    throw new RuntimeException("This feature is not yet implemented.")
//  }
//
//  def launchBootLoader() = {
//    throw new RuntimeException("This feature is not yet implemented.")
//    //sendControlRequest(CrazyRadio.LaunchBootLoader, 0, 0)
//  }

  protected def sendControlRequest(message: (Byte, Byte), value: Short, index: Short, data: Seq[Byte] = Nil) = {
    val controlIoRequestPacket = usbDevice.createUsbControlIrp(message._1, message._2, value, index)

    if (! data.isEmpty) {
      controlIoRequestPacket.setData(data.toArray)
    }

    usbDevice.syncSubmit(controlIoRequestPacket)
  }
}

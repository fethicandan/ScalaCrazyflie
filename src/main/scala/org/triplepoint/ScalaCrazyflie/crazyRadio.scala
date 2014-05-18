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
   * USB Device identification parameters
   * see: http://wiki.bitcraze.se/projects:crazyradio:protocol#usb_protocol
   */
  val CrazyRadioVendorId  = 0x1915
  val CrazyRadioProductId = 0x7777

  /**
   * Radio Configuration Message codes
   * see: http://wiki.bitcraze.se/projects:crazyradio:protocol#dongle_configuration_and_functions_summary
   */
  val SetRadioChannel: Byte      = 0x01
  val SetRadioAddress: Byte      = 0x02
  val SetDataRate: Byte          = 0x03
  val SetRadioPower: Byte        = 0x04
  val SetRadioARD: Byte          = 0x05 // (Automatic Retransmit Delay)
  val SetRadioARC: Byte          = 0x06 // (Ack Retry Count)
  val AckEnable: Byte            = 0x10
  val SetContinuousCarrier: Byte = 0x20
  val StartScanChannels: Byte    = 0x21
  val GetScanChannels: Byte      = 0x21  // This one is used with UsbConst.REQUESTTYPE_DIRECTION_OUT
  val LaunchBootLoader: Byte     = 0xFF.toByte

  /**
   * Enumerate the possible values for the radio data rate setting.
   * see: http://wiki.bitcraze.se/projects:crazyradio:protocol#set_data_rate
   */
  object DataRate extends Enumeration {
    type DataRate = Value
    val `250Kbps` = Value(0)
    val `1Mbps`   = Value(1)
    val `2Mbps`   = Value(2)
  }

  /**
   * Enumerate the possible values for the radio power setting.
   * see: http://wiki.bitcraze.se/projects:crazyradio:protocol#set_radio_power
   */
  object Power extends Enumeration {
    type Power = Value
    val `-18dBm` = Value(0)
    val `-12dBm` = Value(1)
    val `-6dBm`  = Value(2)
    val `0dBm`   = Value(3)
  }

  /**
   * Traverse the tree of USB devices and pull out the first one that matches the CrazyRadio vendor and product IDs.
   */
  def apply() = {
    val rootHub = UsbHostManager.getUsbServices.getRootUsbHub
    val foundUsbDevice = findUsbDevice(rootHub, CrazyRadioVendorId, CrazyRadioProductId)
      .getOrElse {
        throw new RuntimeException(f"Couldn't find the requested device with Vendor ID 0x$CrazyRadioVendorId%x and Product ID 0x$CrazyRadioProductId%x.")
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
 * Objects of this class represent a Crazy Radio USB dongle.
 *
 * For further reference, see http://wiki.bitcraze.se/projects:crazyradio:protocol
 *
 * Also, take a look at the reference implementation in Python, from BitCraze
 * https://github.com/bitcraze/crazyflie-clients-python/blob/master/lib/cflib/drivers/crazyradio.py
 *
 * For details on how the low-level USB elements function, see the JSR-80 specification at:
 * http://javax-usb.sourceforge.net/jsr80.pdf
 */
class CrazyRadio(protected val usbDevice: UsbDevice) {

  // These are radio properties that we'll mirror here, since we can't fetch them from the radio directly.
  // The values assigned here are the default values used to initialize the device.
  var channel: Short             = 2
  var address                    = Seq(0xE7.toByte, 0xE7.toByte, 0xE7.toByte, 0xE7.toByte, 0xE7.toByte)
  var dataRate                   = DataRate.`2Mbps`
  var power                      = Power.`0dBm`
  var automaticRetryDelay        = 250
  var automaticRetryCount: Short = 3
  var automaticAck               = true
  var continuousCarrierMode      = false

  // The firmware has to be relatively new for this radio code to work.
  val firmwareVersion = DescriptorUtils.decodeBCD(usbDevice.getUsbDeviceDescriptor.bcdDevice).toFloat
  require(firmwareVersion >= 0.5, "Firmware version must be 0.5 or greater.")

  initializeRadio()

  def initializeRadio() = {
    setChannel(channel)
    setAddress(address)
    setDataRate(dataRate)
    setPower(power)
    //setAutomaticRetryDelay(automaticRetryDelay) // @TODO This isn't nailed down yet
    setAutomaticRetryCount(automaticRetryCount)
    setAutomaticAck(automaticAck)
    setContinuousCarrierMode(continuousCarrierMode)
  }

  def setChannel(channel: Short) = {
    require(channel >= 0, "The radio channel must be greater than or equal to 0.")
    require(channel <= 125, "The radio channel must be less than or equal to 125.")
    sendControlRequest(CrazyRadio.SetRadioChannel, channel, 0)
    this.channel = channel
  }

  def setAddress(address: Seq[Byte]) = {
    require(address.length == 5, "The address must be a 5 byte sequence.")
    sendControlRequest(CrazyRadio.SetRadioAddress, 0, 0, address)
    this.address = address
  }

  def setDataRate(dataRate: DataRate) = {
    sendControlRequest(CrazyRadio.SetDataRate, dataRate.id.toShort, 0)
    this.dataRate = dataRate
  }

  def setPower(power: Power) = {
    sendControlRequest(CrazyRadio.SetRadioPower, power.id.toShort, 0)
    this.power = power
  }

  // @ TODO look at how these are configured
  def setAutomaticRetryDelay(microseconds: Int) = {
    // @TODO Convert the microseconds to a hexvalue
    throw new RuntimeException("This feature is not yet implemented.")
    // Also, note that this can be configured in two different ways, by wait time or by payload length.  Figure that out.
    // sendControlRequest(CrazyRadio.SetRadioARD, microseconds, 0)
    //this.automaticRetryDelay = microseconds
  }

  def setAutomaticRetryCount(count: Short) = {
    require(count >= 0, "The automatic retry count must be greater than or equal to 0.")
    require(count <= 15, "The automatic retry count must be less than or equal to 15.")
    sendControlRequest(CrazyRadio.SetRadioARC, count, 0)
    this.automaticRetryCount = count
  }

  def setAutomaticAck(enabled: Boolean) = {
    val value = (if (enabled) 1 else 0).toShort
    sendControlRequest(CrazyRadio.AckEnable, value, 0)
    this.automaticAck = enabled
  }

  def setContinuousCarrierMode(active: Boolean) = {
    val value = (if (active) 1 else 0).toShort
    sendControlRequest(CrazyRadio.SetContinuousCarrier, value, 0)
    this.continuousCarrierMode = active
  }

  def scanChannels(): Unit = scanChannels(0, 125)

  def scanChannels(startChannel: Short, stopChannel: Short): Unit = {
    require(startChannel >= 0, "The scan start radio channel must be greater than or equal to 0.")
    require(startChannel <= 125, "The scan start radio channel must be less than or equal to 125.")
    require(stopChannel >= 0, "The scan stop radio channel must be greater than or equal to 0.")
    require(stopChannel <= 125, "The scan stop radio channel must be less than or equal to 125.")
    require(startChannel < stopChannel, "The scan start radio channel must be less than the scan stop channel.")

//    val startingDataRate = dataRate
//    setDataRate(DataRate.`250Kbps`)

    sendControlRequest(CrazyRadio.StartScanChannels, startChannel, stopChannel, Seq(0xFF.toByte))

    val result = sendControlRequestWithResponse(CrazyRadio.GetScanChannels, 0, 0, Seq.fill[Byte](63)(0))

    for (i <- 0 until result.getActualLength) {
      println("channel", result.getData()(i), "data rate", dataRate)
    }

//    setDataRate(startingDataRate)
  }

  def launchBootLoader() = {
    throw new RuntimeException("This feature is not yet implemented.")
  }

  protected def sendControlRequest(request: Byte, value: Short, index: Short, data: Seq[Byte] = Nil) = {
    val requestType = (UsbConst.REQUESTTYPE_DIRECTION_IN | UsbConst.REQUESTTYPE_TYPE_VENDOR).toByte
    sendVendorControlPacket(requestType, request, value, index, data)
    Unit
  }

  protected def sendControlRequestWithResponse(request: Byte, value: Short, index: Short, data: Seq[Byte] = Nil) = {
    val requestType = (UsbConst.REQUESTTYPE_DIRECTION_OUT | UsbConst.REQUESTTYPE_TYPE_VENDOR).toByte
    sendVendorControlPacket(requestType, request, value, index, data)
  }

  protected def sendVendorControlPacket(requestType: Byte, request: Byte, value: Short, index: Short, data: Seq[Byte] = Nil) = {
    val controlIoRequestPacket = usbDevice.createUsbControlIrp(requestType, request, value, index)

    if (!data.isEmpty) {
      controlIoRequestPacket.setData(data.toArray)
    }

    try {
      usbDevice.syncSubmit(controlIoRequestPacket)
    } catch {
      case e: Exception => println("Failed sending USB Control Packet", e)
    }

    controlIoRequestPacket
  }
}

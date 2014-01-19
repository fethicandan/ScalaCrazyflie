package org.triplepoint.ScalaCrazyflie

object CrazyRadio {
  val vendorId = 0x1915
  val productId = 0x7776

  def apply() = new CrazyRadio(UsbDevice(vendorId, productId))
}

class CrazyRadio(val usbDevice: UsbDevice) {

}

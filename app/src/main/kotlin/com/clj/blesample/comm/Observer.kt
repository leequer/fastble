package com.clj.blesample.comm

import com.clj.fastble.data.BleDevice

interface Observer {
    fun disConnected(bleDevice: BleDevice)
}

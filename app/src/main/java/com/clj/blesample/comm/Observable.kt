package com.clj.blesample.comm

import com.clj.fastble.data.BleDevice

interface Observable {
    fun addObserver(obj: Observer)
    fun deleteObserver(obj: Observer)
    fun notifyObserver(bleDevice: BleDevice)
}

package com.clj.blesample.comm

import com.clj.fastble.data.BleDevice

class ObserverManager private constructor() : Observable {

    companion object {
        val instance: ObserverManager by lazy { ObserverManagerHolder.sObserverManager }
    }

    private object ObserverManagerHolder {
        val sObserverManager = ObserverManager()
    }

    private val observers = mutableListOf<Observer>()

    override fun addObserver(obj: Observer) {
        observers.add(obj)
    }

    override fun deleteObserver(obj: Observer) {
        observers.remove(obj)
    }

    override fun notifyObserver(bleDevice: BleDevice) {
        for (o in observers) {
            o.disConnected(bleDevice)
        }
    }
}

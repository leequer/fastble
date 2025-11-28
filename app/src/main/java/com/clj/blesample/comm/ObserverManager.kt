package com.clj.blesample.comm

import com.clj.fastble.data.BleDevice

class ObserverManager private constructor() : Observable {

    companion object {
        @JvmStatic
        fun getInstance(): ObserverManager {
            return ObserverManagerHolder.sObserverManager
        }

        private object ObserverManagerHolder {
            val sObserverManager = ObserverManager()
        }
    }

    private val observers = ArrayList<Observer>()

    override fun addObserver(obj: Observer) {
        observers.add(obj)
    }

    override fun deleteObserver(obj: Observer) {
        val i = observers.indexOf(obj)
        if (i >= 0) {
            observers.remove(obj)
        }
    }

    override fun notifyObserver(bleDevice: BleDevice) {
        for (observer in observers) {
            observer.disConnected(bleDevice)
        }
    }
}

package com.clj.blesample.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.clj.blesample.R
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice

class DeviceAdapter(private val context: Context) : BaseAdapter() {

    private val bleDeviceList = mutableListOf<BleDevice>()

    fun addDevice(bleDevice: BleDevice) {
        removeDevice(bleDevice)
        bleDeviceList.add(bleDevice)
    }

    fun removeDevice(bleDevice: BleDevice) {
        for (i in bleDeviceList.indices) {
            val device = bleDeviceList[i]
            if (bleDevice.key == device.key) {
                bleDeviceList.removeAt(i)
                break
            }
        }
    }

    fun clearConnectedDevice() {
        val iterator = bleDeviceList.iterator()
        while (iterator.hasNext()) {
            val device = iterator.next()
            if (BleManager.getInstance().isConnected(device)) {
                iterator.remove()
            }
        }
    }

    fun clearScanDevice() {
        val iterator = bleDeviceList.iterator()
        while (iterator.hasNext()) {
            val device = iterator.next()
            if (!BleManager.getInstance().isConnected(device)) {
                iterator.remove()
            }
        }
    }

    fun clear() {
        clearConnectedDevice()
        clearScanDevice()
    }

    override fun getCount(): Int {
        return bleDeviceList.size
    }

    override fun getItem(position: Int): BleDevice? {
        return if (position < bleDeviceList.size) bleDeviceList[position] else null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val view: View

        if (convertView != null) {
            view = convertView
            holder = convertView.tag as ViewHolder
        } else {
            view = View.inflate(context, R.layout.adapter_device, null)
            holder = ViewHolder()
            view.tag = holder
            holder.img_blue = view.findViewById(R.id.img_blue)
            holder.txt_name = view.findViewById(R.id.txt_name)
            holder.txt_mac = view.findViewById(R.id.txt_mac)
            holder.txt_rssi = view.findViewById(R.id.txt_rssi)
            holder.layout_idle = view.findViewById(R.id.layout_idle)
            holder.layout_connected = view.findViewById(R.id.layout_connected)
            holder.btn_disconnect = view.findViewById(R.id.btn_disconnect)
            holder.btn_connect = view.findViewById(R.id.btn_connect)
            holder.btn_detail = view.findViewById(R.id.btn_detail)
        }

        val bleDevice = getItem(position)
        if (bleDevice != null) {
            val isConnected = BleManager.getInstance().isConnected(bleDevice)
            val name = bleDevice.name
            val mac = bleDevice.mac
            val rssi = bleDevice.rssi
            holder.txt_name.text = name
            holder.txt_mac.text = mac
            holder.txt_rssi.text = rssi.toString()
            if (isConnected) {
                holder.img_blue.setImageResource(R.mipmap.ic_blue_connected)
                holder.txt_name.setTextColor(0xFF1DE9B6.toInt())
                holder.txt_mac.setTextColor(0xFF1DE9B6.toInt())
                holder.layout_idle.visibility = View.GONE
                holder.layout_connected.visibility = View.VISIBLE
            } else {
                holder.img_blue.setImageResource(R.mipmap.ic_blue_remote)
                holder.txt_name.setTextColor(0xFF000000.toInt())
                holder.txt_mac.setTextColor(0xFF000000.toInt())
                holder.layout_idle.visibility = View.VISIBLE
                holder.layout_connected.visibility = View.GONE
            }
        }

        holder.btn_connect.setOnClickListener {
            mListener?.onConnect(bleDevice!!)
        }

        holder.btn_disconnect.setOnClickListener {
            mListener?.onDisConnect(bleDevice!!)
        }

        holder.btn_detail.setOnClickListener {
            mListener?.onDetail(bleDevice!!)
        }

        return view
    }

    private class ViewHolder {
        lateinit var img_blue: ImageView
        lateinit var txt_name: TextView
        lateinit var txt_mac: TextView
        lateinit var txt_rssi: TextView
        lateinit var layout_idle: LinearLayout
        lateinit var layout_connected: LinearLayout
        lateinit var btn_disconnect: Button
        lateinit var btn_connect: Button
        lateinit var btn_detail: Button
    }

    interface OnDeviceClickListener {
        fun onConnect(bleDevice: BleDevice)
        fun onDisConnect(bleDevice: BleDevice)
        fun onDetail(bleDevice: BleDevice)
    }

    private var mListener: OnDeviceClickListener? = null

    fun setOnDeviceClickListener(listener: OnDeviceClickListener?) {
        mListener = listener
    }
}

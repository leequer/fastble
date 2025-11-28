package com.clj.blesample.operation

import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.clj.blesample.R

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class ServiceListFragment : Fragment() {

    private lateinit var txt_name: TextView
    private lateinit var txt_mac: TextView
    private lateinit var mResultAdapter: ResultAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_service_list, null)
        initView(v)
        showData()
        return v
    }

    private fun initView(v: View) {
        txt_name = v.findViewById(R.id.txt_name)
        txt_mac = v.findViewById(R.id.txt_mac)

        mResultAdapter = ResultAdapter(requireActivity())
        val listView_device = v.findViewById<ListView>(R.id.list_service)
        listView_device.adapter = mResultAdapter
        listView_device.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->
            val service = mResultAdapter.getItem(position)
            (activity as OperationActivity).setService(service!!)
            (activity as OperationActivity).changePage(2)
        }
    }

    public fun showData() {
        val bleDevice = (activity as OperationActivity).bleDevice
        bleDevice?.let { device ->
            val name = device.name
            val mac = device.mac
            val gatt = BleManager.getInstance().getBluetoothGatt(device)

            txt_name.text = getString(R.string.name) + name
            txt_mac.text = getString(R.string.mac) + mac

            mResultAdapter.clear()
            gatt?.services?.forEach { service ->
                mResultAdapter.addResult(service)
            }
            mResultAdapter.notifyDataSetChanged()
        }
    }

    private inner class ResultAdapter(private val context: Context) : BaseAdapter() {

        private val bluetoothGattServices = mutableListOf<BluetoothGattService>()

        fun addResult(service: BluetoothGattService) {
            bluetoothGattServices.add(service)
        }

        fun clear() {
            bluetoothGattServices.clear()
        }

        override fun getCount(): Int {
            return bluetoothGattServices.size
        }

        override fun getItem(position: Int): BluetoothGattService? {
            return if (position < bluetoothGattServices.size) bluetoothGattServices[position] else null
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
                view = View.inflate(context, R.layout.adapter_service, null)
                holder = ViewHolder()
                view.tag = holder
                holder.txt_title = view.findViewById(R.id.txt_title)
                holder.txt_uuid = view.findViewById(R.id.txt_uuid)
                holder.txt_type = view.findViewById(R.id.txt_type)
            }

            val service = bluetoothGattServices[position]
            val uuid = service.uuid.toString()

            holder.txt_title.text = getString(R.string.service) + "(" + position + ")"
            holder.txt_uuid.text = uuid
            holder.txt_type.text = getString(R.string.type)
            return view
        }

        inner class ViewHolder {
            lateinit var txt_title: TextView
            lateinit var txt_uuid: TextView
            lateinit var txt_type: TextView
        }
    }
}

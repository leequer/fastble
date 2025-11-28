package com.clj.blesample.operation

import android.annotation.TargetApi
import android.bluetooth.BluetoothGattCharacteristic
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
import com.clj.blesample.R

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class CharacteristicListFragment : Fragment() {

    private lateinit var mResultAdapter: ResultAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_characteristic_list, container, false)
        initView(v)
        return v
    }

    private fun initView(v: View) {
        mResultAdapter = ResultAdapter(requireActivity())
        val listView_device = v.findViewById<ListView>(R.id.list_characteristic)
        listView_device.adapter = mResultAdapter
        listView_device.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, position, _ ->
            val characteristic = mResultAdapter.getItem(position)
            characteristic?.let { chara ->
                val charaProp = chara.properties
                (activity as OperationActivity).setCharacteristic(chara)
                (activity as OperationActivity).setCharaProp(charaProp)
                (activity as OperationActivity).changePage(2)
            }
        }
    }

    fun showData() {
        val service = (activity as OperationActivity).getBluetoothGattService()
        mResultAdapter.clear()
        service?.characteristics?.forEach { characteristic ->
            mResultAdapter.addResult(characteristic)
        }
        mResultAdapter.notifyDataSetChanged()
    }

    private inner class ResultAdapter(private val context: Context) : BaseAdapter() {

        private val bluetoothGattCharacteristics = mutableListOf<BluetoothGattCharacteristic>()

        fun addResult(characteristic: BluetoothGattCharacteristic) {
            bluetoothGattCharacteristics.add(characteristic)
        }

        fun clear() {
            bluetoothGattCharacteristics.clear()
        }

        override fun getCount(): Int {
            return bluetoothGattCharacteristics.size
        }

        override fun getItem(position: Int): BluetoothGattCharacteristic? {
            return if (position < bluetoothGattCharacteristics.size) bluetoothGattCharacteristics[position] else null
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
                view = View.inflate(context, R.layout.adapter_characteristic, null)
                holder = ViewHolder()
                view.tag = holder
                holder.txt_title = view.findViewById(R.id.txt_title)
                holder.txt_uuid = view.findViewById(R.id.txt_uuid)
                holder.txt_properties = view.findViewById(R.id.txt_properties)
            }

            val characteristic = bluetoothGattCharacteristics[position]
            val uuid = characteristic.uuid.toString()
            val properties = getProperties(characteristic.properties)

            holder.txt_title.text = getString(R.string.characteristic) + "(" + position + ")"
            holder.txt_uuid.text = uuid
            holder.txt_properties.text = properties
            return view
        }

        private fun getProperties(properties: Int): String {
            val stringBuilder = StringBuilder()
            if (properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                stringBuilder.append(getString(R.string.read))
                stringBuilder.append("\n")
            }
            if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
                stringBuilder.append(getString(R.string.write))
                stringBuilder.append("\n")
            }
            if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
                stringBuilder.append(getString(R.string.write_no_response))
                stringBuilder.append("\n")
            }
            if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                stringBuilder.append(getString(R.string.notify))
                stringBuilder.append("\n")
            }
            if (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
                stringBuilder.append(getString(R.string.indicate))
                stringBuilder.append("\n")
            }
            return stringBuilder.toString()
        }

        private inner class ViewHolder {
            lateinit var txt_title: TextView
            lateinit var txt_uuid: TextView
            lateinit var txt_properties: TextView
        }
    }
}

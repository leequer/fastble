package com.clj.blesample.operation

import android.annotation.TargetApi
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.clj.blesample.R

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class CharacteristicListFragment : Fragment() {

    private lateinit var mResultAdapter: ResultAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_characteric_list, null)
        initView(v)
        return v
    }

    private fun initView(v: View) {
        mResultAdapter = ResultAdapter(requireActivity())
        val listView_device = v.findViewById<ListView>(R.id.list_service)
        listView_device.adapter = mResultAdapter
        listView_device.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->
            val characteristic = mResultAdapter.getItem(position)
            val propList = mutableListOf<Int>()
            val propNameList = mutableListOf<String>()
            val charaProp = characteristic?.properties ?: 0
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_READ)
                propNameList.add("Read")
            }
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_WRITE)
                propNameList.add("Write")
            }
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_WRITE_NO_RESPONSE)
                propNameList.add("Write No Response")
            }
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_NOTIFY)
                propNameList.add("Notify")
            }
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_INDICATE)
                propNameList.add("Indicate")
            }

            if (propList.size > 1) {
                AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.select_operation_type))
                    .setItems(propNameList.toTypedArray()) { dialog, which ->
                        (activity as OperationActivity).setCharacteristic(characteristic!!)
                        (activity as OperationActivity).setCharaProp(propList[which])
                        (activity as OperationActivity).changePage(2)
                    }
                    .show()
            } else if (propList.size > 0) {
                (activity as OperationActivity).setCharacteristic(characteristic!!)
                (activity as OperationActivity).setCharaProp(propList[0])
                (activity as OperationActivity).changePage(2)
            }
        }
    }

    fun showData() {
        val service = (activity as OperationActivity).bluetoothGattService
        mResultAdapter.clear()
        if (service != null) {
            for (characteristic in service.characteristics) {
                mResultAdapter.addResult(characteristic)
            }
        }
        mResultAdapter.notifyDataSetChanged()
    }

    private inner class ResultAdapter(private val context: Context) : BaseAdapter() {

        private val characteristicList = mutableListOf<BluetoothGattCharacteristic>()

        fun addResult(characteristic: BluetoothGattCharacteristic) {
            characteristicList.add(characteristic)
        }

        fun clear() {
            characteristicList.clear()
        }

        override fun getCount(): Int {
            return characteristicList.size
        }

        override fun getItem(position: Int): BluetoothGattCharacteristic? {
            return if (position < characteristicList.size) characteristicList[position] else null
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
                holder.img_next = view.findViewById(R.id.img_next)
            }

            val characteristic = characteristicList[position]
            val uuid = characteristic.uuid.toString()

            holder.txt_title.text = getString(R.string.characteristic) + "（" + position + ")"
            holder.txt_uuid.text = uuid

            val property = StringBuilder()
            val charaProp = characteristic.properties
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                property.append("Read")
                property.append(" , ")
            }
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                property.append("Write")
                property.append(" , ")
            }
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                property.append("Write No Response")
                property.append(" , ")
            }
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                property.append("Notify")
                property.append(" , ")
            }
            if ((charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                property.append("Indicate")
                property.append(" , ")
            }
            if (property.length > 1) {
                property.delete(property.length - 2, property.length - 1)
            }
            if (property.isNotEmpty()) {
                holder.txt_type.text = getString(R.string.characteristic) + "( " + property.toString() + ")"
                holder.img_next.visibility = View.VISIBLE
            } else {
                holder.img_next.visibility = View.INVISIBLE
            }

            return view
        }

        inner class ViewHolder {
            lateinit var txt_title: TextView
            lateinit var txt_uuid: TextView
            lateinit var txt_type: TextView
            lateinit var img_next: ImageView
        }
    }
}

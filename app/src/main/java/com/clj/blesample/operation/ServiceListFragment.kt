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
import com.clj.blesample.R
import com.clj.fastble.BleManager

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class ServiceListFragment : Fragment() {

    private lateinit var txtName: TextView
    private lateinit var txtMac: TextView
    private lateinit var resultAdapter: ResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_service_list, null)
        initView(v)
        showData()
        return v
    }

    private fun initView(v: View) {
        txtName = v.findViewById(R.id.txt_name)
        txtMac = v.findViewById(R.id.txt_mac)

        resultAdapter = ResultAdapter(requireActivity())
        val listViewDevice = v.findViewById<ListView>(R.id.list_service)
        listViewDevice.adapter = resultAdapter
        listViewDevice.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, position, _ ->
            val service = resultAdapter.getItem(position)
            if (service != null) {
                (requireActivity() as OperationActivity).setBluetoothGattService(service)
                (requireActivity() as OperationActivity).changePage(1)
            }
        }
    }

    fun showData() {
        val operationActivity = requireActivity() as OperationActivity
        val bleDevice = operationActivity.getBleDevice()
        val name = bleDevice.name
        val mac = bleDevice.mac
        val gatt = BleManager.getInstance().getBluetoothGatt(bleDevice)

        txtName.text = getString(R.string.name) + name
        txtMac.text = getString(R.string.mac) + mac

        resultAdapter.clear()
        for (service in gatt.services) {
            resultAdapter.addResult(service)
        }
        resultAdapter.notifyDataSetChanged()
    }

    private inner class ResultAdapter(context: Context) : BaseAdapter() {

        private val context: Context
        private val bluetoothGattServices: MutableList<BluetoothGattService>

        init {
            this.context = context
            bluetoothGattServices = ArrayList()
        }

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
            return if (position < bluetoothGattServices.size) {
                bluetoothGattServices[position]
            } else {
                null
            }
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val holder: ViewHolder
            val view: View

            if (convertView != null) {
                view = convertView
                holder = convertView.tag as ViewHolder
            } else {
                view = View.inflate(context, R.layout.adapter_service, null)
                holder = ViewHolder()
                view.tag = holder
                holder.txtTitle = view.findViewById(R.id.txt_title)
                holder.txtUuid = view.findViewById(R.id.txt_uuid)
                holder.txtType = view.findViewById(R.id.txt_type)
            }

            val service = bluetoothGattServices[position]
            val uuid = service.uuid.toString()

            holder.txtTitle.text = getString(R.string.service) + "(" + position + ")"
            holder.txtUuid.text = uuid
            holder.txtType.text = getString(R.string.type)
            return view
        }

        inner class ViewHolder {
            lateinit var txtTitle: TextView
            lateinit var txtUuid: TextView
            lateinit var txtType: TextView
        }
    }
}
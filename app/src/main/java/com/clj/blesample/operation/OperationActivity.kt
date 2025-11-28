package com.clj.blesample.operation

import android.annotation.TargetApi
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.clj.blesample.R
import com.clj.blesample.comm.Observer
import com.clj.blesample.comm.ObserverManager
import com.clj.blesample.utils.ToastUtil

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class OperationActivity : AppCompatActivity(), Observer {

    private lateinit var btn_back: Button
    private lateinit var btn_service: Button
    private lateinit var btn_characteristic: Button
    private lateinit var btn_operation: Button
    private lateinit var txt_title: TextView

    private lateinit var serviceListFragment: ServiceListFragment
    private lateinit var characteristicListFragment: CharacteristicListFragment
    private lateinit var characteristicOperationFragment: CharacteristicOperationFragment

    var bleDevice: BleDevice? = null
        private set
    var bluetoothGattService: BluetoothGattService? = null
        private set
    var characteristic: BluetoothGattCharacteristic? = null
        private set
    var charaProp = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operation)
        initView()
        initData()
        initListener()
        ObserverManager.getInstance().addObserver(this)
    }

    private fun initView() {
        btn_back = findViewById(R.id.btn_back)
        btn_service = findViewById(R.id.btn_service)
        btn_characteristic = findViewById(R.id.btn_characteristic)
        btn_operation = findViewById(R.id.btn_operation)
        txt_title = findViewById(R.id.txt_title)
    }

    private fun initData() {
        val intent = intent
        bleDevice = intent.getParcelableExtra(BleManager.KEY_BLE_DEVICE)
        bleDevice?.let { device ->
            txt_title.text = device.name + "(" + device.mac + ")"
        }

        serviceListFragment = ServiceListFragment()
        characteristicListFragment = CharacteristicListFragment()
        characteristicOperationFragment = CharacteristicOperationFragment()

        changePage(1)
    }

    private fun initListener() {
        btn_back.setOnClickListener { finish() }
        btn_service.setOnClickListener { changePage(1) }
        btn_characteristic.setOnClickListener { changePage(2) }
        btn_operation.setOnClickListener { changePage(3) }
    }

    fun changePage(index: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        when (index) {
            1 -> {
                btn_service.setBackgroundResource(R.drawable.shape_btn_select)
                btn_characteristic.setBackgroundResource(R.drawable.shape_btn_normal)
                btn_operation.setBackgroundResource(R.drawable.shape_btn_normal)
                transaction.replace(R.id.fragment, serviceListFragment)
                serviceListFragment.showData()
            }
            2 -> {
                btn_service.setBackgroundResource(R.drawable.shape_btn_normal)
                btn_characteristic.setBackgroundResource(R.drawable.shape_btn_select)
                btn_operation.setBackgroundResource(R.drawable.shape_btn_normal)
                transaction.replace(R.id.fragment, characteristicListFragment)
                characteristicListFragment.showData()
            }
            3 -> {
                btn_service.setBackgroundResource(R.drawable.shape_btn_normal)
                btn_characteristic.setBackgroundResource(R.drawable.shape_btn_normal)
                btn_operation.setBackgroundResource(R.drawable.shape_btn_select)
                transaction.replace(R.id.fragment, characteristicOperationFragment)
                characteristicOperationFragment.showData()
            }
        }
        transaction.commit()
    }

    fun setService(service: BluetoothGattService) {
        bluetoothGattService = service
    }

    fun setCharacteristic(characteristic: BluetoothGattCharacteristic) {
        this.characteristic = characteristic
    }

    fun setCharaProp(charaProp: Int) {
        this.charaProp = charaProp
    }

    override fun disConnected(bleDevice: BleDevice) {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        ObserverManager.getInstance().deleteObserver(this)
    }
}

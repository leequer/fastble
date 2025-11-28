package com.clj.blesample

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.clj.blesample.adapter.DeviceAdapter
import com.clj.blesample.comm.Observer
import com.clj.blesample.comm.ObserverManager
import com.clj.blesample.operation.OperationActivity
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class MainActivity : AppCompatActivity(), Observer {

    companion object {
        private const val REQUEST_CODE_OPEN_BLUETOOTH = 1
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2
    }

    private lateinit var btn_scan: Button
    private lateinit var txt_state: TextView
    private lateinit var list_device: ListView

    private lateinit var mHandler: Handler
    private lateinit var mDeviceAdapter: DeviceAdapter
    private val mDeviceList = mutableListOf<BleDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mHandler = Handler(Looper.getMainLooper())
        initView()
        initBleManager()
        initObserver()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().cancelScan()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
        ObserverManager.instance.deleteObserver(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_BLUETOOTH && resultCode == Activity.RESULT_CANCELED) {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_LOCATION -> {
                var isGranted = true
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        isGranted = false
                        break
                    }
                }
                if (isGranted) {
                    // 权限已授予
                } else {
                    Toast.makeText(this, R.string.permission_location_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun disConnected(device: BleDevice) {
        runOnUiThread {
            mDeviceAdapter.notifyDataSetChanged()
        }
    }

    private fun initView() {
        btn_scan = findViewById(R.id.btn_scan)
        txt_state = findViewById(R.id.txt_state)
        list_device = findViewById(R.id.list_device)

        btn_scan.setOnClickListener {
            if (BleManager.getInstance().isBlueEnable) {
                btn_scan.text = if (BleManager.getInstance().isScanning) getString(R.string.stop_scan) else getString(R.string.start_scan)
                if (BleManager.getInstance().isScanning) {
                    BleManager.getInstance().cancelScan()
                } else {
                    if (checkPermissions()) {
                        scanDevice()
                    }
                }
            } else {
                Toast.makeText(this, R.string.please_open_bluetooth, Toast.LENGTH_SHORT).show()
            }
        }

        mDeviceAdapter = DeviceAdapter(this, mDeviceList)
        list_device.adapter = mDeviceAdapter
        list_device.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, position, _ ->
            val bleDevice = mDeviceList[position]
            if (BleManager.getInstance().isConnected(bleDevice)) {
                val intent = Intent(this, OperationActivity::class.java)
                intent.putExtra(OperationActivity.KEY_DATA, bleDevice)
                startActivity(intent)
            } else {
                BleManager.getInstance().cancelScan()
                connect(bleDevice)
            }
        }
    }

    private fun initBleManager() {
        BleManager.getInstance().init(application)
        BleManager.getInstance().enableLog(true)
        BleManager.getInstance().setReConnectCount(1, 5000)
        BleManager.getInstance().setConnectOverTime(20000)
        BleManager.getInstance().setOperateTimeout(5000)
    }

    private fun initObserver() {
        ObserverManager.instance.addObserver(this)
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val permissionDeniedList = mutableListOf<String>()
            for (permission in permissions) {
                val result = ActivityCompat.checkSelfPermission(this, permission)
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionDeniedList.add(permission)
                }
            }
            if (permissionDeniedList.isNotEmpty()) {
                val deniedPermissions = permissionDeniedList.toTypedArray()
                ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION)
                return false
            }
        }
        return true
    }

    private fun scanDevice() {
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setServiceUuids(null)
            .setDeviceName(true, null)
            .setDeviceMac(null)
            .setAutoConnect(false)
            .setScanTimeOut(10000)
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)

        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                mDeviceList.clear()
                mDeviceAdapter.notifyDataSetChanged()
                btn_scan.text = getString(R.string.stop_scan)
                txt_state.text = getString(R.string.scanning)
            }

            override fun onLeScan(bleDevice: BleDevice?) {
                // 不需要实现，因为BleScanCallback已经处理了
            }

            override fun onScanning(bleDevice: BleDevice?) {
                runOnUiThread {
                    val index = mDeviceList.indexOfFirst { it.key == bleDevice?.key }
                    if (index != -1) {
                        mDeviceList[index] = bleDevice!!
                        mDeviceAdapter.notifyDataSetChanged()
                    } else {
                        mDeviceList.add(bleDevice!!)
                        mDeviceAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                runOnUiThread {
                    btn_scan.text = getString(R.string.start_scan)
                    txt_state.text = getString(R.string.scan_finish)
                }
            }
        })
    }

    private fun connect(bleDevice: BleDevice) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                runOnUiThread {
                    txt_state.text = getString(R.string.connecting)
                    mDeviceAdapter.notifyDataSetChanged()
                }
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                runOnUiThread {
                    txt_state.text = getString(R.string.connect_failed)
                    mDeviceAdapter.notifyDataSetChanged()
                }
            }

            override fun onConnectSuccess(bleDevice: BleDevice?, gatt: android.bluetooth.BluetoothGatt?, status: Int) {
                runOnUiThread {
                    txt_state.text = getString(R.string.connect_success)
                    mDeviceAdapter.notifyDataSetChanged()
                }
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, bleDevice: BleDevice?, gatt: android.bluetooth.BluetoothGatt?, status: Int) {
                runOnUiThread {
                    txt_state.text = getString(R.string.disconnected)
                    mDeviceAdapter.notifyDataSetChanged()
                }
            }
        })
    }
}

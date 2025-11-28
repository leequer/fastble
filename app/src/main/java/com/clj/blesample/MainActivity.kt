package com.clj.blesample

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clj.blesample.adapter.DeviceAdapter
import com.clj.blesample.comm.ObserverManager
import com.clj.blesample.operation.OperationActivity
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_CODE_OPEN_GPS = 1
    private val REQUEST_CODE_PERMISSION_LOCATION = 2

    private lateinit var layout_setting: LinearLayout
    private lateinit var txt_setting: TextView
    private lateinit var btn_scan: Button
    private lateinit var et_name: EditText
    private lateinit var et_mac: EditText
    private lateinit var et_uuid: EditText
    private lateinit var sw_auto: Switch
    private lateinit var img_loading: ImageView

    private lateinit var operatingAnim: Animation
    private lateinit var mDeviceAdapter: DeviceAdapter
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()

        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setConnectOverTime(20000)
            .setOperateTimeout(5000)
    }

    override fun onResume() {
        super.onResume()
        showConnectedDevice()
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_scan -> {
                if (btn_scan.text == getString(R.string.start_scan)) {
                    checkPermissions()
                } else if (btn_scan.text == getString(R.string.stop_scan)) {
                    BleManager.getInstance().cancelScan()
                }
            }

            R.id.txt_setting -> {
                if (layout_setting.visibility == View.VISIBLE) {
                    layout_setting.visibility = View.GONE
                    txt_setting.setText(getString(R.string.expand_search_settings))
                } else {
                    layout_setting.visibility = View.VISIBLE
                    txt_setting.setText(getString(R.string.retrieve_search_settings))
                }
            }
        }
    }

    private fun initView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        btn_scan = findViewById(R.id.btn_scan)
        btn_scan.setText(getString(R.string.start_scan))
        btn_scan.setOnClickListener(this)

        et_name = findViewById(R.id.et_name)
        et_mac = findViewById(R.id.et_mac)
        et_uuid = findViewById(R.id.et_uuid)
        sw_auto = findViewById(R.id.sw_auto)

        layout_setting = findViewById(R.id.layout_setting)
        txt_setting = findViewById(R.id.txt_setting)
        txt_setting.setOnClickListener(this)
        layout_setting.visibility = View.GONE
        txt_setting.setText(getString(R.string.expand_search_settings))

        img_loading = findViewById(R.id.img_loading)
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)
        operatingAnim.interpolator = LinearInterpolator()
        progressDialog = ProgressDialog(this)

        mDeviceAdapter = DeviceAdapter(this)
        mDeviceAdapter.setOnDeviceClickListener(object : DeviceAdapter.OnDeviceClickListener {
            override fun onConnect(bleDevice: BleDevice?) {
                bleDevice?.let {
                    if (!BleManager.getInstance().isConnected(it)) {
                        BleManager.getInstance().cancelScan()
                        connect(it)
                    }
                }
            }

            override fun onDisConnect(bleDevice: BleDevice?) {
                bleDevice?.let {
                    if (BleManager.getInstance().isConnected(it)) {
                        BleManager.getInstance().disconnect(it)
                    }
                }
            }

            override fun onDetail(bleDevice: BleDevice?) {
                bleDevice?.let {
                    if (BleManager.getInstance().isConnected(it)) {
                        val intent = Intent(this@MainActivity, OperationActivity::class.java)
                        intent.putExtra(OperationActivity.KEY_DATA, it)
                        startActivity(intent)
                    }
                }
            }
        })
        val listView_device = findViewById<ListView>(R.id.list_device)
        listView_device.adapter = mDeviceAdapter
    }

    private fun showConnectedDevice() {
        val deviceList = BleManager.getInstance().allConnectedDevice
        mDeviceAdapter.clearConnectedDevice()
        for (bleDevice in deviceList) {
            mDeviceAdapter.addDevice(bleDevice)
        }
        mDeviceAdapter.notifyDataSetChanged()
    }

    private fun setScanRule() {
        val uuids: Array<String>?
        val str_uuid = et_uuid.text.toString()
        uuids = if (TextUtils.isEmpty(str_uuid)) {
            null
        } else {
            str_uuid.split(",").filter { it.isNotEmpty() }.toTypedArray()
        }
        var serviceUuids: Array<UUID?>? = null
        if (!uuids.isNullOrEmpty()) {
            serviceUuids = Array(uuids.size) { null as UUID? }
            for (i in uuids.indices) {
                val name = uuids[i]
                val components = name.split("-").toTypedArray()
                if (components.size == 5) {
                    serviceUuids[i] = UUID.fromString(uuids[i])
                }
            }
        }

        val names: Array<String>?
        val str_name = et_name.text.toString()
        names = if (TextUtils.isEmpty(str_name)) {
            null
        } else {
            str_name.split(",").filter { it.isNotEmpty() }.toTypedArray()
        }

        val mac = ""
        //val mac = "C4:5B:BE:21:78:0A"

        val isAutoConnect = sw_auto.isChecked

        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
            .setDeviceName(false, *names.orEmpty())   // 只扫描指定广播名的设备，可选
            .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
            .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
            .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
    }

    private fun startScan() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                mDeviceAdapter.clearScanDevice()
                mDeviceAdapter.notifyDataSetChanged()
                img_loading.startAnimation(operatingAnim)
                img_loading.visibility = View.VISIBLE
                btn_scan.setText(getString(R.string.stop_scan))
            }

            override fun onLeScan(bleDevice: BleDevice) {
                super.onLeScan(bleDevice)
            }

            override fun onScanning(bleDevice: BleDevice) {
                mDeviceAdapter.addDevice(bleDevice)
                mDeviceAdapter.notifyDataSetChanged()
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                img_loading.clearAnimation()
                img_loading.visibility = View.INVISIBLE
                btn_scan.setText(getString(R.string.start_scan))
            }
        })
    }

    private fun connect(bleDevice: BleDevice) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                progressDialog.show()
                progressDialog.setCanceledOnTouchOutside(false)
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                img_loading.clearAnimation()
                img_loading.visibility = View.INVISIBLE
                btn_scan.setText(getString(R.string.start_scan))
                progressDialog.dismiss()
                Toast.makeText(this@MainActivity, getString(R.string.connect_fail), Toast.LENGTH_LONG).show()
            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                progressDialog.dismiss()
                mDeviceAdapter.addDevice(bleDevice)
                mDeviceAdapter.notifyDataSetChanged()
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                progressDialog.dismiss()

                mDeviceAdapter.removeDevice(bleDevice)
                mDeviceAdapter.notifyDataSetChanged()

                if (isActiveDisConnected) {
                    Toast.makeText(this@MainActivity, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.disconnected), Toast.LENGTH_LONG).show()
                    ObserverManager.getInstance().notifyObserver(bleDevice)
                }

            }
        })
    }

    private fun readRssi(bleDevice: BleDevice) {
        BleManager.getInstance().readRssi(bleDevice, object : BleRssiCallback() {
            override fun onRssiFailure(exception: BleException) {
                Log.i(TAG, "onRssiFailure$exception")
            }

            override fun onRssiSuccess(rssi: Int) {
                Log.i(TAG, "onRssiSuccess: $rssi")
            }
        })
    }

    private fun setMtu(bleDevice: BleDevice, mtu: Int) {
        BleManager.getInstance().setMtu(bleDevice, mtu, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                Log.i(TAG, "onsetMTUFailure$exception")
            }

            override fun onMtuChanged(mtu: Int) {
                Log.i(TAG, "onMtuChanged: $mtu")
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            @NonNull permissions: Array<String>,
                                            @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_LOCATION -> {
                if (grantResults.isNotEmpty()) {
                    for (i in grantResults.indices) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i])
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show()
            return
        }

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionDeniedList = ArrayList<String>()
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }
        if (permissionDeniedList.isNotEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION)
        }
    }

    private fun onPermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.notifyTitle)
                        .setMessage(R.string.gpsNotifyMsg)
                        .setNegativeButton(R.string.cancel
                        ) { dialog, which -> finish() }
                        .setPositiveButton(R.string.setting
                        ) { dialog, which ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    setScanRule()
                    startScan()
                }
            }
        }
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                setScanRule()
                startScan()
            }
        }
    }
}

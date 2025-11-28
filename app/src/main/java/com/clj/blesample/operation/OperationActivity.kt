package com.clj.blesample.operation

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.clj.blesample.R
import com.clj.blesample.comm.Observer
import com.clj.blesample.comm.ObserverManager
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice

class OperationActivity : AppCompatActivity(), Observer {

    companion object {
        const val KEY_DATA = "key_data"
    }

    private lateinit var bleDevice: BleDevice
    private var bluetoothGattService: BluetoothGattService? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private var charaProp: Int = 0

    private lateinit var toolbar: Toolbar
    private val fragments: MutableList<Fragment> = ArrayList()
    private var currentPage = 0
    private lateinit var titles: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operation)
        initData()
        initView()
        initPage()

        ObserverManager.getInstance().addObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().clearCharacterCallback(bleDevice)
        ObserverManager.getInstance().deleteObserver(this)
    }

    override fun disConnected(device: BleDevice) {
        if (device != null && device.key == bleDevice.key) {
            finish()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentPage != 0) {
                currentPage--
                changePage(currentPage)
                return true
            } else {
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initView() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = titles[0]
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            if (currentPage != 0) {
                currentPage--
                changePage(currentPage)
            } else {
                finish()
            }
        }
    }

    private fun initData() {
        bleDevice = intent.getParcelableExtra(KEY_DATA) ?: run {
            finish()
            return
        }

        titles = arrayOf(
            getString(R.string.service_list),
            getString(R.string.characteristic_list),
            getString(R.string.console)
        )
    }

    private fun initPage() {
        prepareFragment()
        changePage(0)
    }

    fun changePage(page: Int) {
        currentPage = page
        toolbar.title = titles[page]
        updateFragment(page)
        if (currentPage == 1) {
            (fragments[1] as CharacteristicListFragment).showData()
        } else if (currentPage == 2) {
            (fragments[2] as CharacteristicOperationFragment).showData()
        }
    }

    private fun prepareFragment() {
        fragments.add(ServiceListFragment())
        fragments.add(CharacteristicListFragment())
        fragments.add(CharacteristicOperationFragment())
        fragments.forEach { fragment ->
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment, fragment)
                .hide(fragment)
                .commit()
        }
    }

    private fun updateFragment(position: Int) {
        if (position > fragments.size - 1) {
            return
        }
        fragments.forEachIndexed { index, fragment ->
            val transaction = supportFragmentManager.beginTransaction()
            if (index == position) {
                transaction.show(fragment)
            } else {
                transaction.hide(fragment)
            }
            transaction.commit()
        }
    }

    fun getBleDevice(): BleDevice {
        return bleDevice
    }

    fun getBluetoothGattService(): BluetoothGattService? {
        return bluetoothGattService
    }

    fun setBluetoothGattService(service: BluetoothGattService?) {
        bluetoothGattService = service
    }

    fun getCharacteristic(): BluetoothGattCharacteristic? {
        return characteristic
    }

    fun setCharacteristic(char: BluetoothGattCharacteristic?) {
        characteristic = char
    }

    fun getCharaProp(): Int {
        return charaProp
    }

    fun setCharaProp(prop: Int) {
        charaProp = prop
    }
}
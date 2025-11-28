package com.clj.blesample.operation

import android.annotation.TargetApi
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.clj.blelibrary.BleManager
import com.clj.blelibrary.callback.BleMtuChangedCallback
import com.clj.blelibrary.callback.BleNotifyCallback
import com.clj.blelibrary.callback.BleReadCallback
import com.clj.blelibrary.callback.BleWriteCallback
import com.clj.blelibrary.data.BleDevice
import com.clj.blesample.R
import com.clj.blesample.comm.ObserverManager
import com.clj.blesample.utils.ByteUtil
import com.clj.blesample.utils.ToastUtil

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class CharacteristicOperationFragment : Fragment() {

    companion object {
        const val PROPERTY_READ = 0x01
        const val PROPERTY_WRITE = 0x02
        const val PROPERTY_WRITE_NO_RESPONSE = 0x04
        const val PROPERTY_NOTIFY = 0x08
        const val PROPERTY_INDICATE = 0x10
    }

    private var bleDevice: BleDevice? = null
    private var bluetoothGattService: BluetoothGattService? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private var charaProp = 0

    private lateinit var txt_title: TextView
    private lateinit var txt_uuid: TextView
    private lateinit var txt_read_result: TextView
    private lateinit var btn_read: Button
    private lateinit var btn_write: Button
    private lateinit var btn_notify: Button
    private lateinit var btn_indicate: Button
    private lateinit var btn_clear: Button
    private lateinit var edit_write_data: EditText
    private lateinit var txt_mtu: TextView
    private lateinit var btn_set_mtu: Button
    private lateinit var btn_get_mtu: Button

    private var isNotify = false
    private var isIndicate = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_characteric_operation, null)
        initView(v)
        setListener()
        return v
    }

    private fun initView(v: View) {
        txt_title = v.findViewById(R.id.txt_title)
        txt_uuid = v.findViewById(R.id.txt_uuid)
        txt_read_result = v.findViewById(R.id.txt_read_result)
        btn_read = v.findViewById(R.id.btn_read)
        btn_write = v.findViewById(R.id.btn_write)
        btn_notify = v.findViewById(R.id.btn_notify)
        btn_indicate = v.findViewById(R.id.btn_indicate)
        btn_clear = v.findViewById(R.id.btn_clear)
        edit_write_data = v.findViewById(R.id.edit_write_data)
        txt_mtu = v.findViewById(R.id.txt_mtu)
        btn_set_mtu = v.findViewById(R.id.btn_set_mtu)
        btn_get_mtu = v.findViewById(R.id.btn_get_mtu)
    }

    private fun setListener() {
        btn_read.setOnClickListener { readCharacteristic() }
        btn_write.setOnClickListener { writeCharacteristic() }
        btn_notify.setOnClickListener { notifyCharacteristic() }
        btn_indicate.setOnClickListener { indicateCharacteristic() }
        btn_clear.setOnClickListener { clearReadResult() }
        btn_set_mtu.setOnClickListener { setMtu() }
        btn_get_mtu.setOnClickListener { getMtu() }
    }

    private fun readCharacteristic() {
        bleDevice?.let { device ->
            characteristic?.let { chara ->
                BleManager.getInstance().read(device, chara.service.uuid.toString(), chara.uuid.toString(), object : BleReadCallback() {
                    override fun onReadSuccess(data: ByteArray?) {
                        showReadResult(data)
                    }

                    override fun onReadFailure(exception: BleException?) {
                        exception?.let { e ->
                            ToastUtil.show(requireActivity(), "onReadFailure: " + e.description)
                        }
                    }
                })
            }
        }
    }

    private fun writeCharacteristic() {
        val data = edit_write_data.text.toString().trim()
        if (TextUtils.isEmpty(data)) {
            ToastUtil.show(requireActivity(), getString(R.string.please_input_data))
            return
        }

        bleDevice?.let { device ->
            characteristic?.let { chara ->
                BleManager.getInstance().write(device, chara.service.uuid.toString(), chara.uuid.toString(), ByteUtil.hexStringToBytes(data), object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        ToastUtil.show(requireActivity(), "onWriteSuccess: current: " + current + " total: " + total + " justWrite: " + ByteUtil.bytesToHexString(justWrite))
                    }

                    override fun onWriteFailure(exception: BleException?) {
                        exception?.let { e ->
                            ToastUtil.show(requireActivity(), "onWriteFailure: " + e.description)
                        }
                    }
                })
            }
        }
    }

    private fun notifyCharacteristic() {
        bleDevice?.let { device ->
            characteristic?.let { chara ->
                if (!isNotify) {
                    BleManager.getInstance().notify(device, chara.service.uuid.toString(), chara.uuid.toString(), object : BleNotifyCallback() {
                        override fun onNotifySuccess() {
                            isNotify = true
                            btn_notify.text = getString(R.string.stop_notify)
                            ToastUtil.show(requireActivity(), "notify success")
                        }

                        override fun onNotifyFailure(exception: BleException?) {
                            exception?.let { e ->
                                ToastUtil.show(requireActivity(), "notify failure: " + e.description)
                            }
                        }

                        override fun onCharacteristicChanged(data: ByteArray?) {
                            showReadResult(data)
                        }
                    })
                } else {
                    BleManager.getInstance().stopNotify(device, chara.service.uuid.toString(), chara.uuid.toString())
                    isNotify = false
                    btn_notify.text = getString(R.string.notify)
                    ToastUtil.show(requireActivity(), "stop notify success")
                }
            }
        }
    }

    private fun indicateCharacteristic() {
        bleDevice?.let { device ->
            characteristic?.let { chara ->
                if (!isIndicate) {
                    BleManager.getInstance().indicate(device, chara.service.uuid.toString(), chara.uuid.toString(), object : BleNotifyCallback() {
                        override fun onNotifySuccess() {
                            isIndicate = true
                            btn_indicate.text = getString(R.string.stop_indicate)
                            ToastUtil.show(requireActivity(), "indicate success")
                        }

                        override fun onNotifyFailure(exception: BleException?) {
                            exception?.let { e ->
                                ToastUtil.show(requireActivity(), "indicate failure: " + e.description)
                            }
                        }

                        override fun onCharacteristicChanged(data: ByteArray?) {
                            showReadResult(data)
                        }
                    })
                } else {
                    BleManager.getInstance().stopIndicate(device, chara.service.uuid.toString(), chara.uuid.toString())
                    isIndicate = false
                    btn_indicate.text = getString(R.string.indicate)
                    ToastUtil.show(requireActivity(), "stop indicate success")
                }
            }
        }
    }

    private fun clearReadResult() {
        txt_read_result.text = ""
    }

    private fun setMtu() {
        bleDevice?.let { device ->
            BleManager.getInstance().setMtu(device, 200, object : BleMtuChangedCallback() {
                override fun onSetMTUFailure(exception: BleException?) {
                    exception?.let { e ->
                        ToastUtil.show(requireActivity(), "setMtu failure: " + e.description)
                    }
                }

                override fun onMtuChanged(mtu: Int) {
                    ToastUtil.show(requireActivity(), "mtu size: " + mtu)
                }
            })
        }
    }

    private fun getMtu() {
        bleDevice?.let { device ->
            val mtu = BleManager.getInstance().getMtu(device)
            txt_mtu.text = mtu.toString()
        }
    }

    private fun showReadResult(data: ByteArray?) {
        data?.let { bytes ->
            val dataHex = ByteUtil.bytesToHexString(bytes)
            val dataStr = ByteUtil.bytesToString(bytes)
            val result = txt_read_result.text.toString() + dataHex + "\n" + dataStr + "\n"
            txt_read_result.text = result
        }
    }

    fun showData() {
        bleDevice = (activity as OperationActivity).bleDevice
        bluetoothGattService = (activity as OperationActivity).bluetoothGattService
        characteristic = (activity as OperationActivity).characteristic
        charaProp = (activity as OperationActivity).charaProp

        characteristic?.let { chara ->
            val uuid = chara.uuid.toString()
            txt_title.text = getString(R.string.characteristic)
            txt_uuid.text = uuid

            btn_read.visibility = if ((charaProp and PROPERTY_READ) > 0) View.VISIBLE else View.GONE
            btn_write.visibility = if ((charaProp and PROPERTY_WRITE) > 0 || (charaProp and PROPERTY_WRITE_NO_RESPONSE) > 0) View.VISIBLE else View.GONE
            btn_notify.visibility = if ((charaProp and PROPERTY_NOTIFY) > 0) View.VISIBLE else View.GONE
            btn_indicate.visibility = if ((charaProp and PROPERTY_INDICATE) > 0) View.VISIBLE else View.GONE
        }
    }
}

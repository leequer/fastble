package com.clj.blesample.operation

import android.annotation.TargetApi
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.clj.blesample.R
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class CharacteristicOperationFragment : Fragment() {

    companion object {
        const val PROPERTY_READ = 1
        const val PROPERTY_WRITE = 2
        const val PROPERTY_WRITE_NO_RESPONSE = 3
        const val PROPERTY_NOTIFY = 4
        const val PROPERTY_INDICATE = 5
    }

    private lateinit var layout_container: LinearLayout
    private val childList = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_characteric_operation, null)
        initView(v)
        return v
    }

    private fun initView(v: View) {
        layout_container = v.findViewById(R.id.layout_container)
    }

    fun showData() {
        val operationActivity = requireActivity() as OperationActivity
        val bleDevice = operationActivity.getBleDevice()
        val characteristic = operationActivity.getCharacteristic()
        val charaProp = operationActivity.getCharaProp() ?: 0
        val child = (characteristic?.uuid?.toString() ?: "") + charaProp

        for (i in 0 until (layout_container.childCount)) {
            layout_container.getChildAt(i).visibility = View.GONE
        }
        if (childList.contains(child)) {
            layout_container.findViewWithTag<View>(bleDevice.key + (characteristic?.uuid?.toString() ?: "") + charaProp)?.visibility = View.VISIBLE
        } else {
            childList.add(child)

            val view = LayoutInflater.from(activity).inflate(R.layout.layout_characteric_operation, null)
            view.tag = bleDevice.key + (characteristic?.uuid?.toString() ?: "") + charaProp
            val layout_add = view.findViewById<LinearLayout>(R.id.layout_add)
            val txt_title = view.findViewById<TextView>(R.id.txt_title)
            txt_title.text = (characteristic?.uuid?.toString() ?: "") + getString(R.string.data_changed)
            val txt = view.findViewById<TextView>(R.id.txt)
            txt.movementMethod = ScrollingMovementMethod.getInstance()

            when (charaProp) {
                PROPERTY_READ -> {
                    val view_add = LayoutInflater.from(activity).inflate(R.layout.layout_characteric_operation_button, null)
                    val btn = view_add.findViewById<Button>(R.id.btn)
                    btn.text = getString(R.string.read)
                    btn.setOnClickListener {
                        BleManager.getInstance().read(
                            bleDevice,
                            characteristic?.service?.uuid?.toString() ?: "",
                            characteristic?.uuid?.toString() ?: "",
                            object : BleReadCallback() {
                                override fun onReadSuccess(data: ByteArray) {
                                    runOnUiThread { addText(txt, HexUtil.formatHexString(data, true)) }
                                }

                                override fun onReadFailure(exception: BleException) {
                                    runOnUiThread { addText(txt, exception.toString()) }
                                }
                            }
                        )
                    }
                    layout_add.addView(view_add)
                }

                PROPERTY_WRITE -> {
                    val view_add = LayoutInflater.from(activity).inflate(R.layout.layout_characteric_operation_et, null)
                    val et = view_add.findViewById<EditText>(R.id.et)
                    val btn = view_add.findViewById<Button>(R.id.btn)
                    btn.text = getString(R.string.write)
                    btn.setOnClickListener {
                        //val hex = et.text.toString()
                        val hex = "7e00001d4937000006000100181234567890aabb"
                        if (TextUtils.isEmpty(hex)) {
                            return@setOnClickListener
                        }
                        BleManager.getInstance().write(
                            bleDevice,
                            characteristic?.service?.uuid.toString() ?: "",
                            characteristic?.uuid.toString() ?: "",
                            HexUtil.hexStringToBytes(hex),
                            object : BleWriteCallback() {
                                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                                    runOnUiThread {
                                        addText(txt, "write success, current: $current\n" +
                                                " total: $total\n" +
                                                " justWrite: ${HexUtil.formatHexString(justWrite, true)}")
                                    }
                                }

                                override fun onWriteFailure(exception: BleException) {
                                    runOnUiThread { addText(txt, exception.toString()) }
                                }
                            }
                        )
                    }
                    layout_add.addView(view_add)
                }

                PROPERTY_WRITE_NO_RESPONSE -> {
                    val view_add = LayoutInflater.from(activity).inflate(R.layout.layout_characteric_operation_et, null)
                    val et = view_add.findViewById<EditText>(R.id.et)
                    val btn = view_add.findViewById<Button>(R.id.btn)
                    btn.text = getString(R.string.write)
                    btn.setOnClickListener {
                        val hex = et.text.toString()
                        if (TextUtils.isEmpty(hex)) {
                            return@setOnClickListener
                        }
                        BleManager.getInstance().write(
                            bleDevice,
                            characteristic?.service?.uuid.toString() ?: "",
                            characteristic?.uuid.toString() ?: "",
                            HexUtil.hexStringToBytes(hex),
                            object : BleWriteCallback() {
                                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                                    runOnUiThread {
                                        addText(txt, "write success, current: $current\n" +
                                                " total: $total\n" +
                                                " justWrite: ${HexUtil.formatHexString(justWrite, true)}")
                                    }
                                }

                                override fun onWriteFailure(exception: BleException) {
                                    runOnUiThread { addText(txt, exception.toString()) }
                                }
                            }
                        )
                    }
                    layout_add.addView(view_add)
                }

                PROPERTY_NOTIFY -> {
                    val view_add = LayoutInflater.from(activity).inflate(R.layout.layout_characteric_operation_button, null)
                    val btn = view_add.findViewById<Button>(R.id.btn)
                    btn.text = getString(R.string.open_notification)
                    btn.setOnClickListener {
                        if (btn.text.toString() == getString(R.string.open_notification)) {
                            btn.text = getString(R.string.close_notification)
                            BleManager.getInstance().notify(
                                bleDevice,
                                characteristic?.service?.uuid.toString() ?: "",
                                characteristic?.uuid.toString() ?: "",
                                object : BleNotifyCallback() {
                                    override fun onNotifySuccess() {
                                        runOnUiThread { addText(txt, "notify success") }
                                    }

                                    override fun onNotifyFailure(exception: BleException) {
                                        runOnUiThread { addText(txt, exception.toString()) }
                                    }

                                    override fun onCharacteristicChanged(data: ByteArray) {
                                        runOnUiThread { addText(txt, HexUtil.formatHexString(data, true)) }
                                    }
                                }
                            )
                        } else {
                            btn.text = getString(R.string.open_notification)
                            BleManager.getInstance().stopNotify(
                                bleDevice,
                                characteristic?.service?.uuid.toString() ?: "",
                                characteristic?.uuid.toString() ?: ""
                            )
                        }
                    }
                    layout_add.addView(view_add)
                }

                PROPERTY_INDICATE -> {
                    val view_add = LayoutInflater.from(activity).inflate(R.layout.layout_characteric_operation_button, null)
                    val btn = view_add.findViewById<Button>(R.id.btn)
                    btn.text = getString(R.string.open_notification)
                    btn.setOnClickListener {
                        if (btn.text.toString() == getString(R.string.open_notification)) {
                            btn.text = getString(R.string.close_notification)
                            BleManager.getInstance().indicate(
                                bleDevice,
                                characteristic?.service?.uuid.toString() ?: "",
                                characteristic?.uuid.toString() ?: "",
                                object : BleIndicateCallback() {
                                    override fun onIndicateSuccess() {
                                        runOnUiThread { addText(txt, "indicate success") }
                                    }

                                    override fun onIndicateFailure(exception: BleException) {
                                        runOnUiThread { addText(txt, exception.toString()) }
                                    }

                                    override fun onCharacteristicChanged(data: ByteArray) {
                                        runOnUiThread { addText(txt, HexUtil.formatHexString(data, true)) }
                                    }
                                }
                            )
                        } else {
                            btn.text = getString(R.string.open_notification)
                            BleManager.getInstance().stopIndicate(
                                bleDevice,
                                characteristic?.service?.uuid.toString() ?: "",
                                characteristic?.uuid.toString() ?: ""
                            )
                        }
                    }
                    layout_add.addView(view_add)
                }
            }

            layout_container.addView(view)
        }
    }

    private fun runOnUiThread(runnable: () -> Unit) {
        if (isAdded && activity != null) {
            activity?.runOnUiThread(runnable)
        }
    }

    private fun addText(textView: TextView, content: String) {
        textView.append(content)
        textView.append("\n")
        val offset = textView.lineCount * textView.lineHeight
        if (offset > textView.height) {
            textView.scrollTo(0, offset - textView.height)
        }
    }
}

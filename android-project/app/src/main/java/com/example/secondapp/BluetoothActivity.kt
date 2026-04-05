package com.example.secondapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.secondapp.databinding.ActivityBluetoothBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothBinding

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDevicesListView: ListView
    private lateinit var discoveredDevicesListView: ListView
    private lateinit var scanButton: Button
    private lateinit var connectButton: Button
    private lateinit var sendButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var messageInputTextView: TextView
    private lateinit var receivedMessagesTextView: TextView

    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private val discoveredDeviceNames = mutableListOf<String>()
    private var discoveredDevicesAdapter: ArrayAdapter<String>? = null

    private val pairedDevices = mutableListOf<BluetoothDevice>()
    private val pairedDeviceNames = mutableListOf<String>()
    private var pairedDevicesAdapter: ArrayAdapter<String>? = null

    private var selectedDevice: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var connectedThread: ConnectedThread? = null

    private val handler = Handler(Looper.getMainLooper())
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID

    // BroadcastReceiver for discovered devices
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (it.name != null && !discoveredDevices.contains(it)) {
                            discoveredDevices.add(it)
                            discoveredDeviceNames.add("${it.name}\n${it.address}")
                            discoveredDevicesAdapter?.notifyDataSetChanged()
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    scanButton.text = "开始扫描"
                    statusTextView.text = "扫描完成，发现 ${discoveredDevices.size} 个设备"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        pairedDevicesListView = binding.pairedDevicesList
        discoveredDevicesListView = binding.discoveredDevicesList
        scanButton = binding.scanButton
        connectButton = binding.connectButton
        sendButton = binding.sendButton
        statusTextView = binding.statusTextView
        messageInputTextView = binding.messageInput
        receivedMessagesTextView = binding.receivedMessages

        // Setup adapters
        pairedDevicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedDeviceNames)
        pairedDevicesListView.adapter = pairedDevicesAdapter

        discoveredDevicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, discoveredDeviceNames)
        discoveredDevicesListView.adapter = discoveredDevicesAdapter

        // Get Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Register BroadcastReceiver
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(receiver, filter)

        // Setup click listeners
        setupClickListeners()

        // Load paired devices
        loadPairedDevices()
    }

    private fun setupClickListeners() {
        scanButton.setOnClickListener {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
                scanButton.text = "开始扫描"
                statusTextView.text = "扫描已停止"
            } else {
                startScanning()
            }
        }

        pairedDevicesListView.setOnItemClickListener { _, _, position, _ ->
            selectedDevice = pairedDevices[position]
            statusTextView.text = "已选择配对设备: ${selectedDevice?.name ?: selectedDevice?.address}"
        }

        discoveredDevicesListView.setOnItemClickListener { _, _, position, _ ->
            selectedDevice = discoveredDevices[position]
            statusTextView.text = "已选择发现设备: ${selectedDevice?.name ?: selectedDevice?.address}"
        }

        connectButton.setOnClickListener {
            selectedDevice?.let { device ->
                connectToDevice(device)
            } ?: run {
                Toast.makeText(this, "请先选择一个设备", Toast.LENGTH_SHORT).show()
            }
        }

        sendButton.setOnClickListener {
            val message = messageInputTextView.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageInputTextView.text = ""
            } else {
                Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPairedDevices() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermissions()
            return
        }
        val pairedDevicesSet = bluetoothAdapter.bondedDevices
        pairedDevices.clear()
        pairedDeviceNames.clear()
        pairedDevicesSet?.forEach { device ->
            pairedDevices.add(device)
            pairedDeviceNames.add("${device.name}\n${device.address}")
        }
        pairedDevicesAdapter?.notifyDataSetChanged()
    }

    private fun startScanning() {
        if (!checkPermissions()) {
            requestBluetoothPermissions()
            return
        }

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        discoveredDevices.clear()
        discoveredDeviceNames.clear()
        discoveredDevicesAdapter?.notifyDataSetChanged()

        if (bluetoothAdapter.startDiscovery()) {
            scanButton.text = "停止扫描"
            statusTextView.text = "正在扫描..."
        } else {
            Toast.makeText(this, "扫描启动失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (!checkPermissions()) {
            requestBluetoothPermissions()
            return
        }

        // Cancel discovery before connecting
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        // Start connection in background thread
        Thread {
            try {
                // Get BluetoothSocket
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                socket.connect()

                // Connected successfully
                runOnUiThread {
                    statusTextView.text = "已连接到 ${device.name ?: device.address}"
                    connectButton.text = "断开连接"
                    connectButton.setOnClickListener {
                        disconnectDevice()
                    }
                }

                bluetoothSocket = socket
                inputStream = socket.inputStream
                outputStream = socket.outputStream

                // Start listening for incoming messages
                connectedThread = ConnectedThread()
                connectedThread?.start()

            } catch (e: IOException) {
                Log.e(TAG, "连接失败", e)
                runOnUiThread {
                    Toast.makeText(this, "连接失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    statusTextView.text = "连接失败"
                }
            }
        }.start()
    }

    private fun disconnectDevice() {
        connectedThread?.cancel()
        connectedThread = null

        bluetoothSocket?.close()
        bluetoothSocket = null
        inputStream = null
        outputStream = null

        runOnUiThread {
            statusTextView.text = "已断开连接"
            connectButton.text = "连接设备"
            connectButton.setOnClickListener {
                selectedDevice?.let { device ->
                    connectToDevice(device)
                } ?: run {
                    Toast.makeText(this, "请先选择一个设备", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        if (outputStream == null) {
            Toast.makeText(this, "未连接到设备", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                val bytes = message.toByteArray()
                outputStream?.write(bytes)
                outputStream?.flush()

                runOnUiThread {
                    val currentText = receivedMessagesTextView.text.toString()
                    receivedMessagesTextView.text = "发送: $message\n$currentText"
                }
            } catch (e: IOException) {
                Log.e(TAG, "发送失败", e)
                runOnUiThread {
                    Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private inner class ConnectedThread : Thread() {
        private val buffer = ByteArray(1024)
        private var isRunning = true

        override fun run() {
            while (isRunning) {
                try {
                    val bytes = inputStream?.read(buffer) ?: -1
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        handler.post {
                            val currentText = receivedMessagesTextView.text.toString()
                            receivedMessagesTextView.text = "接收: $message\n$currentText"
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "连接中断", e)
                    isRunning = false
                    runOnUiThread {
                        disconnectDevice()
                    }
                }
            }
        }

        fun cancel() {
            isRunning = false
            try {
                bluetoothSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "关闭socket失败", e)
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_BLUETOOTH_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
                    loadPairedDevices()
                } else {
                    Toast.makeText(this, "需要权限才能使用蓝牙功能", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "蓝牙已启用", Toast.LENGTH_SHORT).show()
                    loadPairedDevices()
                } else {
                    Toast.makeText(this, "蓝牙未启用", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel discovery
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        // Unregister receiver
        unregisterReceiver(receiver)
        // Disconnect
        disconnectDevice()
    }

    companion object {
        private const val TAG = "BluetoothActivity"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 2
    }
}
package com.example.spaceinvadercontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import com.example.spaceinvadercontroller.R
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var leftMoveButton: ImageButton
    private lateinit var rightMoveButton: ImageButton
    private lateinit var hitButton: ImageButton

    private lateinit var socket: BluetoothSocket
    private lateinit var outputStream: OutputStream
    private val REQUEST_BLUETOOTH_CONNECT = 1

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        leftMoveButton = findViewById(R.id.LeftMoveButton)
        rightMoveButton = findViewById(R.id.RightMoveButton)
        hitButton = findViewById(R.id.HitButton)

        leftMoveButton.setOnClickListener{
            sendBluetoothData(byteArrayOf(0x04))
        }
        rightMoveButton.setOnClickListener {
            sendBluetoothData(byteArrayOf(0x02))
        }
        hitButton.setOnClickListener{
            sendBluetoothData(byteArrayOf(0x10))
        }

        // Request Bluetooth and Location permissions at runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_BLUETOOTH_CONNECT)
        } else {
            connectToBluetoothDevice()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectToBluetoothDevice()
            } else {
                Log.e("Bluetooth Connection", "Bluetooth permission denied")
            }
        }
    }

    private fun connectToBluetoothDevice() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val deviceAddress = "94:3C:C6:38:8A:BA" // Replace with the correct Bluetooth address for your device
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        // Replace with your ESP32's device name
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.e("Bluetooth Connection", "Bluetooth permission not granted")
            } else {
                val uuids: Array<ParcelUuid>? = device?.uuids
                if ((uuids != null) && uuids.isNotEmpty()) {
                    socket = device.createRfcommSocketToServiceRecord(uuids[0].uuid)
                    socket.connect()
                    outputStream = socket.outputStream
                }

            }
        } catch (e: Exception) {
            Log.e("Bluetooth Connection", e.message ?: "Unknown error")
        }
    }



    private fun sendBluetoothData(data: ByteArray) {
        try {
            outputStream.write(data)
        } catch (e: IOException) {
            Log.e("Bluetooth Connection", e.message ?: "Unknown error")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket.close()
        } catch (e: IOException) {
            Log.e("Bluetooth Connection", e.message ?: "Unknown error")
        }
    }


}

package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var on: Button
    private lateinit var off: Button
    private lateinit var findButton: Button

    private lateinit var listview: ListView


    private lateinit var deviceList: ArrayList<String>
    private lateinit var adapters: ArrayAdapter<String>

    @SuppressLint("NewApi")
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        on = findViewById(R.id.button_turn_on)
        off = findViewById(R.id.button_turn_off)
        findButton = findViewById(R.id.button_find)
        listview = findViewById(R.id.listview)
        deviceList = ArrayList()

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT)
                .show()
        }

        on.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                val bluetoothIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    return@setOnClickListener
                }
                startActivityForResult(bluetoothIntent, 2)
            } else {
                Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show()
            }
        }

        off.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    return@setOnClickListener
                }
                val intent: Intent = Intent("android.bluetooth.adapter.action.REQUEST_DISABLE")
                startActivityForResult(intent, 12)
            }
        }

        findButton.setOnClickListener {
            registerReceiver(
                deviceScanReceiver,
                IntentFilter(BluetoothDevice.ACTION_FOUND)
            )
            adapters = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
            listview.setAdapter(adapters)

            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                return@setOnClickListener
            }
            bluetoothAdapter?.startDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    val deviceScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action.toString()
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    deviceList.add(deviceName.toString())
                    adapters.notifyDataSetChanged()
                }
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return applicationContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(deviceScanReceiver)
    }
}
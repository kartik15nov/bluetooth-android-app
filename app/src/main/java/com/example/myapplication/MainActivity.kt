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
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

internal const val ACTION_PERMISSIONS_GRANTED = "BluetoothPermission.permissions_granted"
internal const val ACTION_PERMISSIONS_DENIED = "BluetoothPermission.permissions_denied"

class MainActivity : AppCompatActivity() {
    private lateinit var on: Button
    private lateinit var off: Button
    private lateinit var find: Button

    private lateinit var listview: ListView


    private lateinit var deviceList: ArrayList<String>
    private lateinit var adapters: ArrayAdapter<String>

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        on = findViewById(R.id.button_turn_on)
        off = findViewById(R.id.button_turn_off)
        find = findViewById(R.id.button_find)
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
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    startActivityForResult(bluetoothIntent, 2)
                }
            } else {
                Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show()
            }
        }

        off.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.disable()
                Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show()
            }
        }

        find.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_DENIED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                        2
                    )
                }
            }
            bluetoothAdapter?.startDiscovery()
        }

        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, intentFilter)
        adapters = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, deviceList)
        listview.setAdapter(adapters)
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Handle the ACTION_FOUND event here
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                } else {
                    device?.name
                    deviceList.add(device?.name.toString())
                    adapters.notifyDataSetChanged()
                }

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendBroadcast(Intent(ACTION_PERMISSIONS_GRANTED))
            finish()
        } else {
            sendBroadcast(Intent(ACTION_PERMISSIONS_DENIED))
            finish()
        }
    }
}
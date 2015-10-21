package com.example.johnwilkes.testapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 0xB;
    private static final long SCAN_PERIOD = 8000; // ms

    private TextView textView;
    private TextView btoothTextView;
    private int numClicks;
    private BluetoothAdapter btAdapter;
    private Handler btScanHandler;
    private BluetoothGatt btoothGatt;

    private ArrayList<String> devices;
    private ArrayAdapter<String> devicesAdapter;

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback()
            {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
                {
                    runOnUiThread(
                            new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (device != null)
                                    {
                                        String name = device.getName();
                                        if (name == null)
                                        {
                                            name = "None";
                                        }
                                        String str = String.format("Name: %s,\nAddress: %s", name, device.getAddress());
                                        Log.d("Bluetooth", str);
                                        if (!devices.contains(str))
                                        {
                                            devices.add(str);
                                            devicesAdapter.notifyDataSetChanged();

                                            BluetoothGattCallback gattCallback = new BluetoothGattCallback()
                                            {
                                            };
                                            BluetoothGatt btoothGatt = device.connectGatt(getApplicationContext(), true, gattCallback);
                                            boolean ok = device.createBond();
                                            Log.d("Bluetooth", String.format("Bonded: %b", ok));

//                                            Log.d("Bluetooth", "Connected devices:");
//                                            for (BluetoothDevice d : btoothGatt.getConnectedDevices())
//                                            {
//                                                String dName = d.getName();
//                                                if (dName == null)
//                                                {
//                                                    Log.d("Bluetooth", "No name");
//                                                }
//                                                else
//                                                {
//                                                    Log.d("Bluetooth", d.getName());
//                                                }
//                                            }
                                        }
                                    }
                                }
                            }
                    );
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView)findViewById(R.id.devicesList);
        devices = new ArrayList<String>();
        devicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);
        listView.setAdapter(devicesAdapter);

        textView = (TextView) findViewById(R.id.textView);
        btoothTextView = (TextView) findViewById(R.id.bluetoothstatus);
        Button scanButton = (Button) findViewById(R.id.btButton);

        btScanHandler = new Handler();

        numClicks = 0;

        // check for Bluetooth support
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
        {
            btoothTextView.setText("Bluetooth is not supported on this device");
            scanButton.setEnabled(false);
        }
        else
        {
            btoothTextView.setText("Bluetooth is supported on this device");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (btAdapter != null)
        {
            if (!btAdapter.isEnabled())
            {
                Intent enableBtoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtoothIntent, REQUEST_ENABLE_BT);
            }

            int numBonded = btAdapter.getBondedDevices().size();
            Log.d("onStart", String.format("Bonded Devices (%d):", numBonded));
            for (BluetoothDevice device : btAdapter.getBondedDevices())
            {
                Log.d("onStart", String.format("Name: %s, Address: %s", device.getName(), device.getAddress()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonClick(View v)
    {
        ++numClicks;
        textView.setText(String.format("The button was pressed %d time%s", numClicks, (numClicks != 1) ? "s" : ""));

//        devices.add(String.format("click %d", numClicks));
//        devicesAdapter.notifyDataSetChanged();
    }

    public void onBluetoothScan(View v)
    {
        Log.d("Bluetooth", "scanning...");

        devices.clear();
        devicesAdapter.notifyDataSetChanged();

        // stop the scan after the specified period
        btScanHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        btAdapter.stopLeScan(leScanCallback);
                    }
                }, SCAN_PERIOD);

        // start the scan
        btAdapter.startLeScan(leScanCallback);
    }
}

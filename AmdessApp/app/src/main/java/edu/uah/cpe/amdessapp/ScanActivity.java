package edu.uah.cpe.amdessapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class ScanActivity extends AppCompatActivity
{
    private class BtScanCallback extends ScanCallback
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            Log.d("onScanResult", "start...");

            BluetoothDevice device = result.getDevice();

            // if we do not already know about this device,
            // add it to the list view
            if (!AmdessDevices.getInstance().containsDevice(device.getAddress()))
            {
                Log.d("onScanResult", "adding device");

                String name = device.getName();
                if (name == null)
                {
                    name = "None";
                }
                String str = String.format("%s (%s)", name, device.getAddress());

                // display the device if we haven't already
                if (!deviceStrings.contains(str))
                {
                    devices.add(device);

                    deviceStrings.add(str);
                    deviceStringsAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public final static String ACTION_ADDED_DEVICE = "edu.uah.cpe.amdessapp.ADDED_DEVICE";

    private final static long SCAN_PERIOD = 8000; // ms

    private BluetoothLeScanner bluetoothLeScanner;
    private ArrayList<String> deviceStrings;
    private ArrayAdapter<String> deviceStringsAdapter;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private Button rescanButton;
    private BtScanCallback btScanCallback = new BtScanCallback();
    private Handler btScanHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        rescanButton = (Button) findViewById(R.id.rescanButton);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // set up list view and adapter
        deviceStrings = new ArrayList<>();
        deviceStringsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceStrings);
        ListView listView = (ListView) findViewById(R.id.scanListView);
        listView.setAdapter(deviceStringsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                onDeviceSelected(position);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        startScan();
    }

    public void onDoneClick(View v)
    {
        finishScanActivity("");
    }

    public void onRescanClick(View v)
    {
        startScan();
    }

    public void onDeviceSelected(int row)
    {
        BluetoothDevice device = devices.get(row);

        AmdessDevices.getInstance().addDevice(device);

        finishScanActivity(device.getAddress());
    }

    private void finishScanActivity(String address)
    {
        Log.d("finishScanActivity", "Got here");

        // stop the Bluetooth scan
        stopScan();

        Intent intent = new Intent();
        intent.putExtra(ACTION_ADDED_DEVICE, address);
        setResult(Activity.RESULT_OK, intent);

        // finish the activity
        finish();
    }

    private void startScan()
    {
        // disable rescan button
        rescanButton.setEnabled(false);

        // clear devices
        devices.clear();
        deviceStrings.clear();
        deviceStringsAdapter.notifyDataSetChanged();

        // stop the scan after the specified period
        btScanHandler.postDelayed(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        stopScan();
                    }
                }, SCAN_PERIOD);

        // start the scan
        bluetoothLeScanner.startScan(btScanCallback);
    }

    private void stopScan()
    {
        bluetoothLeScanner.stopScan(btScanCallback);
        rescanButton.setEnabled(true);
    }
}

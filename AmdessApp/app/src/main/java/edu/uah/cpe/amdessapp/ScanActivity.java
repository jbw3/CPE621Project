package edu.uah.cpe.amdessapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            if (name == null)
            {
                name = "None";
            }
            String str = String.format("%s (%s)", name, device.getAddress());

            // display the device if we haven't already
            if (!deviceStrings.contains(str))
            {
                deviceStrings.add(str);
                deviceStringsAdapter.notifyDataSetChanged();
            }
        }
    }

    private static final long SCAN_PERIOD = 8000; // ms

    private BluetoothLeScanner bluetoothLeScanner;
    private ArrayList<String> deviceStrings;
    private ArrayAdapter<String> deviceStringsAdapter;
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
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        startScan();
    }

    public void onDoneClick(View v)
    {
        // stop the Bluetooth scan
        stopScan();

        // finish the activity
        finish();
    }

    public void onRescanClick(View v)
    {
        startScan();
    }

    private void startScan()
    {
        // disable rescan button
        rescanButton.setEnabled(false);

        // clear list of devices
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

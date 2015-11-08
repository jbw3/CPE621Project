package edu.uah.cpe.amdessapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class SensorListActivity extends AppCompatActivity
{
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int BT_SCAN = 2;
    public final static String DEVICE_ADDRESS_ID = "edu.uah.cpe.amdessapp.DEVICE_ADDRESS_ID";

    private BluetoothAdapter btAdapter = null;
    private ArrayList<String> devices;
    private ArrayAdapter<String> devicesAdapter;
    private boolean loadDevicesOnStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // set up list view and adapter
        devices = new ArrayList<>();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices);
        ListView listView = (ListView) findViewById(R.id.devicesListView);
        listView.setAdapter(devicesAdapter);

        // check for Bluetooth support
        Button scanButton = (Button) findViewById(R.id.scanButton);
        // if Bluetooth is not supported, disable the scan button
        if (btAdapter == null)
        {
            scanButton.setEnabled(false);
        }
        else // enable the scan button
        {
            scanButton.setEnabled(true);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (btAdapter != null)
        {
            // if bluetooth is disabled, ask to enable it
            if (!btAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            if (loadDevicesOnStart)
            {
                // load bonded devices
                loadBondedDevices();
            }
            // load devices the next time
            loadDevicesOnStart = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BT_SCAN)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                String address = data.getStringExtra(ScanActivity.ACTION_ADDED_DEVICE);

                if (!address.isEmpty())
                {
                    // bond with the device
                    BluetoothDevice device = AmdessDevices.getInstance().getDevice(address);
                    if (device != null)
                    {
                        // bond with device
                        device.createBond();

                        // add device to list view
                        addDeviceToList(device);

                        // when we return from the scanning activity, we do not want
                        // to load bonded devices because the device we just bonded
                        // with will not be in the list (not sure why)
                        loadDevicesOnStart = false;
                    }
                }
            }
        }
    }

    public void onScanClick(View v)
    {
        // launch the scan activity
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, BT_SCAN);
    }

    private void addDeviceToList(BluetoothDevice device)
    {
        String name = device.getName();
        if (name == null)
        {
            name = "None";
        }

        String str = String.format("%s (%s)", name, device.getAddress());
        devices.add(str);
        devicesAdapter.notifyDataSetChanged();
    }

    private void loadBondedDevices()
    {
        // clear existing lists of devices
        AmdessDevices.getInstance().clear();
        devices.clear();
        devicesAdapter.notifyDataSetChanged();

        for (BluetoothDevice device : btAdapter.getBondedDevices())
        {
            // add device to master list
            AmdessDevices.getInstance().addDevice(device);

            // add device to list view
            addDeviceToList(device);
        }
    }

    private void selectDevice(String address)
    {
        Intent intent = new Intent(this, DeviceActivity.class);
        intent.putExtra(DEVICE_ADDRESS_ID, address);
        startActivity(intent);
    }
}

package com.example.johnwilkes.testapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 0xB;
    private static final long SCAN_PERIOD = 20000; // ms

    private TextView btoothTextView;
    private Button scanButton;
    private XYPlot plot;
    private BluetoothAdapter btAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    private Handler btScanHandler;
    private BtScanCallback btScanCallback = new BtScanCallback();

    private class BtScanCallback extends ScanCallback
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            Log.d("Bluetooth", "onScanResult");

            BluetoothDevice device = result.getDevice();
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

                boolean ok = device.createBond();
                Log.d("Bluetooth", String.format("Bonded: %b", ok));

                if (bleBinder == null)
                {
                    Log.d("Bluetooth", "bleBinder is null");
                }
                else
                {
                    bleBinder.connect(device);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            Log.d("Bluetooth", "onScanFailed");
        }
    }

    private AdvertiseCallback advertiseCallback =
            new AdvertiseCallback()
            {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect)
                {
                    Log.d("onStartSuccess", "success");
                    super.onStartSuccess(settingsInEffect);
                }

                @Override
                public void onStartFailure(int errorCode)
                {
                    Log.d("onStartFailure", "failure");
                    super.onStartFailure(errorCode);
                }
            };

    private ArrayList<String> devices;
    private ArrayAdapter<String> devicesAdapter;
    private BluetoothLeService.BleBinder bleBinder = null;

    private ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            bleBinder = (BluetoothLeService.BleBinder) service;
            Log.d("onServiceConnected", "bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            bleBinder = null;
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

        btoothTextView = (TextView) findViewById(R.id.bluetoothstatus);
        scanButton = (Button) findViewById(R.id.btButton);
        plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);

        btScanHandler = new Handler();

        // check for Bluetooth support
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
        {
            btoothTextView.setText("Bluetooth is not supported on this device");
            scanButton.setEnabled(false);
            bluetoothLeScanner = null;
        }
        else
        {
            btoothTextView.setText("Bluetooth is supported on this device");
            scanButton.setEnabled(true);
            bluetoothLeScanner = btAdapter.getBluetoothLeScanner();
        }

        plotStuff();
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

            Intent intent = new Intent(this, BluetoothLeService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        Log.d("Bluetooth", "onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        Log.d("Bluetooth", "onDestroy");

        unbindService(connection);
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
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void plotStuff()
    {
        Number[] series1Numbers = {1, 3, 2, 4, 7, 4, 5};

        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "Series 1"
        );

        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
//        series1Format.configure(getApplicationContext(), );
        plot.addSeries(series1, series1Format);
    }

    public void onBluetoothScan(View v)
    {
        Log.d("Bluetooth", "scanning...");

        scanButton.setEnabled(false);

        bluetoothLeScanner = btAdapter.getBluetoothLeScanner();

        devices.clear();
        devicesAdapter.notifyDataSetChanged();

        // stop the scan after the specified period
        btScanHandler.postDelayed(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        bluetoothLeScanner.stopScan(btScanCallback);
                        scanButton.setEnabled(true);
                    }
                }, SCAN_PERIOD);

        // start the scan
        bluetoothLeScanner.startScan(btScanCallback);
    }
}

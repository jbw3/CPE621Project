package edu.uah.cpe.amdessapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class BluetoothLeService extends Service
{
    public class DeviceInfo
    {
        public boolean connected = false;
        public boolean armed = false;
        public boolean alarming = false;
        public LinkedList<UUID> services = new LinkedList<>();
    }

    // ------ GATT Callback ------
    private class BtGattCallback extends BluetoothGattCallback
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                Log.d("onConnectionStateChange", "Connected");

                setConnectionStatus(address, true);

                broadcastUpdate(Constants.ACTION_GATT_CONNECTED, address);

                gatt.discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.d("onConnectionStateChange", "Disconnected");

                armStateChar = null;
                alarmStateChar = null;

                setConnectionStatus(address, false);

                broadcastUpdate(Constants.ACTION_GATT_DISCONNECTED, address);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            Log.d("onServicesDiscovered", "start");

            // debugging
            printServices(gatt);

            LinkedList<UUID> services = new LinkedList<>();
            String address = gatt.getDevice().getAddress();

            // subscribe to AMDeSS notifications
            BluetoothGattService gattService = gatt.getService(Constants.UUID_SERVICE_AMDESS_STATUS);
            if (gattService == null)
            {
                Log.w("onServicesDiscovered", "The device does not support the AMDeSS Status service");
            }
            else
            {
                armStateChar = gattService.getCharacteristic(Constants.UUID_CHARACTERISTIC_AMDESS_ARM_STATE);
                alarmStateChar = gattService.getCharacteristic(Constants.UUID_CHARACTERISTIC_AMDESS_ALARM_STATE);
                if (armStateChar == null || alarmStateChar == null)
                {
                    if (armStateChar == null)
                    {
                        Log.w("onServicesDiscovered", "The device does not support the AMDeSS Arm State characteristic");
                    }
                    if (alarmStateChar == null)
                    {
                        Log.w("onServicesDiscovered", "The device does not support the AMDeSS Alarm State characteristic");
                    }
                }
                else
                {
                    Log.d("onServicesDiscovered", "Subscribing to notifications");

                    gatt.setCharacteristicNotification(armStateChar, true);
                    gatt.setCharacteristicNotification(alarmStateChar, true);

                    // We cannot write both the arm state and alarm state descriptors
                    // at the same time. Thus, write the arm state now and the alarm state
                    // when its write is complete (in onDescriptorWrite)
                    setCccdNotificationsEnabled(gatt, armStateChar, true);
                }
            }

            // get all services the device supports
            for (BluetoothGattService service : gatt.getServices())
            {
                // add service
                UUID id = service.getUuid();
                services.add(id);
            }

            setServices(address, services);

            broadcastUpdate(Constants.ACTION_GATT_SERVICES_DISCOVERED, address);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.d("onCharacteristicChanged", "start...");

            if (characteristic.getUuid().equals(Constants.UUID_CHARACTERISTIC_AMDESS_ARM_STATE))
            {
                onReceiveArmState(gatt, characteristic);
            }
            else if (characteristic.getUuid().equals(Constants.UUID_CHARACTERISTIC_AMDESS_ALARM_STATE))
            {
                onReceiveAlarmState(gatt, characteristic);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            UUID id = descriptor.getUuid();
            String name = Constants.GATT_DESCRIPTOR_NAMES.get(id);
            if (name == null)
            {
                name = id.toString();
            }
            Log.d("onDescriptorWrite", String.format("Descriptor: %s, write %s", name, (status == BluetoothGatt.GATT_SUCCESS ? "succeeded" : "failed")));

            // when the arm state descriptor write has completed, write the alarm state descriptor
            if (descriptor.getCharacteristic().getUuid().equals(Constants.UUID_CHARACTERISTIC_AMDESS_ARM_STATE) &&
                descriptor.getUuid().equals(Constants.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION))
            {
                setCccdNotificationsEnabled(gatt, alarmStateChar, true);
            }
        }

        private void setCccdNotificationsEnabled(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled)
        {
            // get characteristic name
            UUID charId = characteristic.getUuid();
            String charName = Constants.GATT_CHARACTERISTIC_NAMES.get(charId);
            if (charName == null)
            {
                charName = charId.toString();
            }

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(Constants.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION);
            if (descriptor == null)
            {
                Log.w("setCccdNtfictnsEnabled", String.format("Characteristic %s does not have CCCD", characteristic.getUuid().toString()));
                return;
            }

            byte[] value;
            if (enabled)
            {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            }
            else
            {
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            }
            boolean ok = descriptor.setValue(value);
            if (!ok)
            {
                Log.w("setCccdNtfictnsEnabled", String.format("CCCD setValue failed for characteristic %s", charName));
                return;
            }

            ok = gatt.writeDescriptor(descriptor);
            if (!ok)
            {
                Log.w("setCccdNtfictnsEnabled", String.format("CCCD writeDescriptor failed for characteristic %s", charName));
            }
        }

        private void printServices(BluetoothGatt gatt)
        {
            Log.d("printServices", String.format("Found %s services", gatt.getServices().size()));
            for (BluetoothGattService service : gatt.getServices())
            {
                UUID serviceId = service.getUuid();
                String serviceName = Constants.GATT_SERVICE_NAMES.get(serviceId);
                if (serviceName == null)
                {
                    serviceName = "?";
                }
                Log.d("printServices", String.format("%s (%s)",
                        serviceId.toString(),
                        serviceName));

                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics())
                {
                    UUID charId = characteristic.getUuid();
                    String charName = Constants.GATT_CHARACTERISTIC_NAMES.get(charId);
                    if (charName == null)
                    {
                        charName = "?";
                    }
                    Log.d("printServices", String.format("  %s (%s)",
                            charId.toString(),
                            charName));

                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors())
                    {
                        UUID descriptorId = descriptor.getUuid();
                        String descriptorName = Constants.GATT_DESCRIPTOR_NAMES.get(descriptorId);
                        if (descriptorName == null)
                        {
                            descriptorName = "?";
                        }
                        Log.d("printServices", String.format("    %s (%s)",
                                descriptorId.toString(),
                                descriptorName));
                    }
                }
            }
        }
    }

    // ------ Binder ------
    public class BleBinder extends Binder
    {
        public void connect(BluetoothDevice device)
        {
            bluetoothGatt = device.connectGatt(getApplicationContext(), true, gattCallback);
        }
    }

    // ------ Broadcast Receiver ------
    private class CommandReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String address = intent.getStringExtra(Constants.INFO_DEVICE_ADDRESS);
            if (address == null || address.isEmpty())
            {
                Log.w("onReceive", "deviceAddress is null or an empty string");
            }
            else
            {
                toggleArmState(address);
            }
        }
    }

    private static final int NOTIFICATION_ID = 123;
    private static final int ARM_STATE_DISARMED = 0;
    private static final int ARM_STATE_ARMED = 1;
    private static final int ALARM_STATE_NO_ALARM = 0;
    private static final int ALARM_STATE_ALARM = 1;
    private final long[] VIBRATE_PATTERN = {0, 400, 100, 400, 100, 400, 100, 400};
    private final BtGattCallback gattCallback = new BtGattCallback();
    private BleBinder bleBinder = new BleBinder();
    private BluetoothGatt bluetoothGatt = null;
    static private HashMap<String, DeviceInfo> infoMap = new HashMap<>();
    private BluetoothGattCharacteristic armStateChar = null;
    private BluetoothGattCharacteristic alarmStateChar = null;
    private CommandReceiver commandReceiver = new CommandReceiver();
    IntentFilter intentFilter = new IntentFilter();

    static public DeviceInfo getDeviceInfo(String address)
    {
        return infoMap.get(address);
    }

    public BluetoothLeService()
    {
        // add actions to filter
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return bleBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        intentFilter.addAction(Constants.ACTION_TOGGLE_ARM_STATE);
        registerReceiver(commandReceiver, intentFilter);
    }

    @Override
    public void onDestroy()
    {
        if (bluetoothGatt != null)
        {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        unregisterReceiver(commandReceiver);
    }

    private void setConnectionStatus(String address, boolean connected)
    {
        // get the current info for the device
        DeviceInfo info = infoMap.get(address);

        // if the info does not exist, create it
        if (info == null)
        {
            info = new DeviceInfo();
        }

        info.connected = connected;

        // update the info in the map
        infoMap.put(address, info);
    }

    private void setArmStatus(String address, boolean armed)
    {
        // get the current info for the device
        DeviceInfo info = infoMap.get(address);

        // if the info does not exist, create it
        if (info == null)
        {
            info = new DeviceInfo();
        }

        info.armed = armed;

        // update the info in the map
        infoMap.put(address, info);
    }

    private void setAlarmStatus(String address, boolean alarming)
    {
        // get the current info for the device
        DeviceInfo info = infoMap.get(address);

        // if the info does not exist, create it
        if (info == null)
        {
            info = new DeviceInfo();
        }

        info.alarming = alarming;

        // update the info in the map
        infoMap.put(address, info);
    }

    private void setServices(String address, LinkedList<UUID> services)
    {
        // get the current info for the device
        DeviceInfo info = infoMap.get(address);

        // if the info does not exist, create it
        if (info == null)
        {
            info = new DeviceInfo();
        }

        info.services = services;

        // update the info in the map
        infoMap.put(address, info);
    }

    private void onReceiveArmState(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        Log.d("onReceiveArmState", "start...");

        String address = gatt.getDevice().getAddress();

        Log.d("onReceiveArmState", String.format("Length: %s", characteristic.getValue().length));

        // read arm state from characteristic
        int armState = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

        // update arm state
        if (armState != ARM_STATE_DISARMED && armState != ARM_STATE_ARMED)
        {
            Log.w("onReceiveArmState", "Caleb sent an invalid arm state!!!");
        }
        else
        {
            setArmStatus(address, (armState == ARM_STATE_ARMED));

            // send broadcast
            broadcastUpdate(Constants.ACTION_DEVICE_ARM, address);
        }
    }

    private void onReceiveAlarmState(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        Log.d("onReceiveAlarmState", "start...");

        String address = gatt.getDevice().getAddress();

        Log.d("onReceiveAlarmState", String.format("Length: %s", characteristic.getValue().length));

        // read alarm state from characteristic
        int alarmState = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

        // update alarm state
        if (alarmState != ALARM_STATE_NO_ALARM && alarmState != ALARM_STATE_ALARM)
        {
            Log.w("onReceiveAlarmState", "Caleb sent an invalid alarm state!!!");
        }
        else
        {
            boolean alarming = (alarmState == ALARM_STATE_ALARM);
            setAlarmStatus(address, alarming);

            // send broadcast
            broadcastUpdate(Constants.ACTION_DEVICE_ALARM, address);

            // send notification if alarming
            if (alarming)
            {
                sendAlarmNotification(gatt.getDevice());
            }
        }
    }

    private void sendAlarmNotification(BluetoothDevice device)
    {
        String name = device.getName();
        String address = device.getAddress();
        if (name == null)
        {
            name = address;
        }
        String text = String.format("Alarm from %s", name);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder =
                new Notification.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("Alarm!")
                        .setContentText(text)
                        .setVibrate(VIBRATE_PATTERN);
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void toggleArmState(String address)
    {
        if (bluetoothGatt == null)
        {
            Log.w("toggleArmState", "bluetoothGatt is null");
            return;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothDevice device = null;
        for (BluetoothDevice d : bluetoothManager.getConnectedDevices(BluetoothProfile.GATT))
        {
            if (d.getAddress().equals(address))
            {
                device = d;
                break;
            }
        }

        if (device != null)
        {
            Log.d("toggleArmState", "Found connected device");
        }
        else
        {
            Log.d("toggleArmState", "Could not find connected device");
        }
    }

    private void broadcastUpdate(String action, String address)
    {
        Intent intent = new Intent(action);
        intent.putExtra(Constants.INFO_DEVICE_ADDRESS, address);
        sendBroadcast(intent);
    }
}

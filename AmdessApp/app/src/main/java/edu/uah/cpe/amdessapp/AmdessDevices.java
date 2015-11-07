package edu.uah.cpe.amdessapp;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;

public class AmdessDevices
{
    private static AmdessDevices instance = null;
    private HashMap<String, BluetoothDevice> devices;

    private AmdessDevices()
    {
        devices = new HashMap<>();
    }

    public static AmdessDevices getInstance()
    {
        if (instance == null)
        {
            instance = new AmdessDevices();
        }
        return instance;
    }

    public void addDevice(BluetoothDevice device)
    {
        devices.put(device.getAddress(), device);
    }

    public void removeDevice(String address)
    {
        devices.remove(address);
    }

    public BluetoothDevice getDevice(String address)
    {
        return devices.get(address);
    }
}

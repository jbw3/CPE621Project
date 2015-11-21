package edu.uah.cpe.amdessapp;

import android.graphics.Color;

import java.util.HashMap;
import java.util.UUID;

public class Constants
{
    public static final String ACTION_DEVICE_ALARM =
            "edu.uah.cpe.amdessapp.ACTION_DEVICE_ALARM";

    public static final String ACTION_GATT_CONNECTED =
            "edu.uah.cpe.amdessapp.ACTION_GATT_CONNECTED";

    public static final String ACTION_GATT_DISCONNECTED =
            "edu.uah.cpe.amdessapp.ACTION_GATT_DISCONNECTED";

    public static final String ACTION_GATT_SERVICES_DISCOVERED =
            "edu.uah.cpe.amdessapp.ACTION_GATT_SERVICES_DISCOVERED";

    public final static String INFO_DEVICE_ADDRESS =
            "edu.uah.cpe.amdessapp.INFO_DEVICE_ADDRESS";

    // ------ UUIDs ------

    // services
    public static final UUID UUID_GENERIC_ACCESS    = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_GENERIC_ATTRIBUTE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_IMMEDIATE_ALERT   = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");

    // characteristics
    public static final UUID UUID_DEVICE_NAME       = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DEVICE_APPEARANCE = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_CHANGED   = UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb");

    public static final HashMap<UUID, String> GATT_SERVICE_NAMES;
    static
    {
        GATT_SERVICE_NAMES = new HashMap<>();
        GATT_SERVICE_NAMES.put(UUID_GENERIC_ACCESS,    "Generic Access");
        GATT_SERVICE_NAMES.put(UUID_GENERIC_ATTRIBUTE, "Generic Attribute");
        GATT_SERVICE_NAMES.put(UUID_IMMEDIATE_ALERT,   "Immediate Alert");
    }

    public static final HashMap<UUID, String> GATT_CHARACTERISTIC_NAMES;
    static
    {
        GATT_CHARACTERISTIC_NAMES = new HashMap<>();
        GATT_CHARACTERISTIC_NAMES.put(UUID_DEVICE_NAME,       "Device Name");
        GATT_CHARACTERISTIC_NAMES.put(UUID_DEVICE_APPEARANCE, "Device Appearance");
        GATT_CHARACTERISTIC_NAMES.put(UUID_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS, "Peripheral Preferred Connection Parameters");
        GATT_CHARACTERISTIC_NAMES.put(UUID_SERVICE_CHANGED,   "Service Changed");
    }

    // ------ Colors ------

    public final static int CONNECTED_COLOR = Color.rgb(0, 150, 50);
    public final static int DISCONNECTED_COLOR = Color.rgb(200, 0, 0);
}

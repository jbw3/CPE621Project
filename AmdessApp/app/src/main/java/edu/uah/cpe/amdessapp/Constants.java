package edu.uah.cpe.amdessapp;

import android.graphics.Color;

import java.util.UUID;

public class Constants
{
    public static final String ACTION_DEVICE_ALARM =
            "edu.uah.cpe.amdessapp.ACTION_DEVICE_ALARM";

    public static final String ACTION_GATT_CONNECTED =
            "edu.uah.cpe.amdessapp.ACTION_GATT_CONNECTED";

    public static final String ACTION_GATT_DISCONNECTED =
            "edu.uah.cpe.amdessapp.ACTION_GATT_DISCONNECTED";

    public final static String INFO_DEVICE_ADDRESS =
            "edu.uah.cpe.amdessapp.INFO_DEVICE_ADDRESS";

    // ------ UUIDs ------
    public final static UUID UUID_GENERIC_ACCESS = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_GENERIC_ATTRIBUTE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_IMMEDIATE_ALERT = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");

    // ------ Colors ------

    public final static int CONNECTED_COLOR = Color.rgb(0, 150, 50);
    public final static int DISCONNECTED_COLOR = Color.rgb(200, 0, 0);
}

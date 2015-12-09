------------------------------
AMDeSS Android App
------------------------------

Building:
1. Open the AmdessApp.iml file with Android Studio
2. Build the app using the menu option Build -> Make Project

Using the app:
The app is composed of 3 Activities.

SensorListActivity contains a list of Bluetooth devices that have been paired
with the smartphone. Selecting one of the devices launches DeviceActivity. There
is also a scan button. Clicking this launches ScanActivity.

DeviceActivity contains detailed info about an AMDeSS device and a button to
arm/disarm it. This info included in this Activity is as follows:
* Device name
* Device MAC address
* Connection status
* Arm state
* Alarm state
* Battery level
* Capacitor reading
* Graph of past 200 capacitor readings

ScanActivity, when launched, scans for Bluetooth Low Energy (BLE) devices. All
devices that are found are added to a list. When a device from the list is
selected, the smartphone is paired with that device. The scan lasts for 8
seconds. There are 2 buttons in this Activity: "Rescan" and "Done". "Rescan"
starts a new 8 second scan. "Done" returns the user to SensorListActivity.

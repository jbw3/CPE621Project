------------------------------
AMDeSS BL600 Sensor Device
------------------------------

- amdess.sb is the main source file
- to load it onto a BL600:
	* connect to the BL600 in UwTerminal
	* right-click on the terminal window and select XCompile+Load
	* select the amdess.sb file
	* reboot and run amdess.sb!
- For best results, reboot the BL600 before running again.
  If you don't, the BLE services will get created again, but the 
  existing ones are still around.
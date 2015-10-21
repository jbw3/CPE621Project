###########################################################################

----------------------------
Upgrade via JLINK Adapter
----------------------------
To download firmware using the JLINK, please refer to the instructions in
the document BL600_FW_UPGRADE_APPNOTE_v2_2.pdf

----------------------------
Upgrade via UART
----------------------------
THIS IS ONLY POSSIBLE IF THE FIRMWARE VERSION IN THE MODULE MATCHES
THE FIRST TWO NUMBERS IN THE VERSION OF THIS UPGRADE FILE.
Firmware version from the module can be extracted by submitting the
AT I 3
command and you will get a version in the format w.x.y.z
IF w.x do NOT match, then you will end up with a non functioning
module. 

You can recover from that by reinstalling using the JLINK method
which will work always.

To upgrade ...
Unzip the exe from UpgradeViaUart(OnlyIfNordicStackIsSame).zip and
place in this folder and launch it

###########################################################################

----------------------------------------------------------------------------
After downloading the new firmware, validate it as follows:-

   * Go to the subfolder 'smartBASIC_Sample_Apps'
   * Start UwTerminal (version 6.30 or newer)
   * Set comms parameters as 9600, n, 8, 1 with CTS/RTS handshaking
   * type AT and check you get a 00 response
   * type AT i 3 and check you get a version number as per the version
     number embedded in the hex filename with the term 'BL600Phase1COMBINED' 
   * Right click and select "XCompile+Load"
   * Select the file "hw.hello.world.sb" in thw'smartBASIC_Sample_Apps' folder 
     and you MUST see many lines starting with AT+FWRH and then finally a 
     line AT+FCL
   * type 
       at+dir
     and you should see a line with '06    hw'
   * type
       at+run "hw" 
   * Check that you see "Hello World" and then a "00"
   * type
       at i 4
     and you will see the mac address of the module. It must NOT have'C0FFEE' 
     embedded inside. If you do, the module does not have a licence.
     If it does have 'C0FFEE' then you will need to contact Laird for the 
     licence 
     
   
----------------------------------------------------------------------------

****************************************************************************
Have you saved the licence key?

If not, then you can do so by submitting the command : ATi 49406 
to the module and it will respond with 

    10	49406	A_20_hex_digit_number

Where 'A_20_hex_digit_number' is either all F's, empty or some random hex digits
If random hex digits then you have a valid key and so copy and paste it to
a text file for later use if required.
.
After the firmware upgrade is complete re-enter the licence key by submitting
the command (with double quotes around the key)

   AT+LIC "A_20_hex_digit_number"

If your key is all F's or empty , then after upgrade, contact Laird and quote
the response to the command AT i 14
****************************************************************************


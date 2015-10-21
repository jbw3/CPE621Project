@echo off
rem **********************************************************************
rem This utility locks the flash so that content cannot be read using
rem the jlink adapter
rem **********************************************************************

cls
echo .
echo This will LOCK the flash from jlink access.
echo The only way to unlock is to completely erase the module ....
echo .
echo   You can launch this utility with the parameter autoconfirm, as follows:-
echo      _LockFlash autoconfirm
echo   and it will not ask for confirmation.
echo .

if "%1" == "autoconfirm" goto :lockflash
echo WARNING:
echo  Any subsequence firmware upgrades will not be able to read back
echo  the licence and you will need to contact Laird for a new replacement
echo  licence. When you do contact Laird you will save time by providing
echo  the response to the command AT i 14 which is a serial number that
echo  is used to create the licence number.
echo .
echo Please press CTRL-C to abort, otherwise ...
echo .
pause

rem --------------------------------
rem  Locking flash
rem --------------------------------
:lockflash
echo ........          Locking flash  (please wait)
nrfjprog  --memwr 0x10001004 --val 0
echo .........         Locking flash  (done)

rem --------------------------------
rem Reset the module
rem --------------------------------
echo ................  Resetting module
nrfjprog  --reset --quiet

rem **********************************************************************
echo .................
echo #########################
echo Flash Locking is complete
echo #########################
echo .
rem **********************************************************************

:end
if "%1" == "autoconfirm" goto :abort
pause
:abort


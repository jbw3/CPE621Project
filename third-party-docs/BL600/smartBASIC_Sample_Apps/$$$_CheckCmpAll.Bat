@echo off
REM **************************************************************
REM This batch file is used to confirm all apps compile
REM **************************************************************

set fl=NO

echo.
for %%F IN (*.sb) DO call $$$_xcmp %%F
echo.

if not "%fl%"=="YES" goto :pass
echo.
echo ****
echo FAIL: At least one app failed to compile
echo ****

:pass
del *.uwc >NUL
pause


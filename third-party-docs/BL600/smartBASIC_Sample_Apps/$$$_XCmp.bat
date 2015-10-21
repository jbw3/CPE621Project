echo Compiling .. %1
XComp_BL600r2_D24D_8092 %1
if ERRORLEVEL 1 goto cmperr
goto :end1

:cmperr
echo ****
echo FAIL: %app% failed to compile
echo ****
set fl=YES
goto :done


:done
:end1

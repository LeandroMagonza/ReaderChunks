@echo off
echo ================================
echo   Building BookBits APK
echo ================================
echo.

cd android
echo Building APK...
gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================
    echo   ✅ BUILD SUCCESSFUL!
    echo ================================
    echo.
    echo APK location:
    echo android\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Ready to install on your phone!
    echo ================================
) else (
    echo.
    echo ❌ Build failed with errors
    echo Check the output above for details
)

pause
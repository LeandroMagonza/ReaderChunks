@echo off
echo Building APK with new file support...
echo ====================================

cd android

echo.
echo Cleaning previous build...
call gradlew clean

echo.
echo Building debug APK...
call gradlew assembleDebug

echo.
echo Build completed!
echo APK location: app\build\outputs\apk\debug\app-debug.apk

echo.
echo To install: adb install app\build\outputs\apk\debug\app-debug.apk
echo.

pause
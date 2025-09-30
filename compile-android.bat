@echo off
echo ================================
echo     BookBits Android Build
echo ================================

echo.
echo Necesitas instalar Android SDK para compilar:
echo.
echo 1. Descarga Android Command Line Tools:
echo    https://developer.android.com/studio#command-tools
echo.
echo 2. Extrae en: C:\android-sdk\cmdline-tools\latest\
echo.
echo 3. Agrega a PATH:
echo    C:\android-sdk\cmdline-tools\latest\bin
echo    C:\android-sdk\platform-tools
echo.
echo 4. Instala dependencias:
echo    sdkmanager "platforms;android-34"
echo    sdkmanager "build-tools;34.0.0"
echo.
echo 5. Ejecuta en directorio android/:
echo    gradlew assembleDebug
echo.
echo ================================
echo El APK se generará en:
echo android\app\build\outputs\apk\debug\
echo ================================

pause
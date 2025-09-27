@echo off
echo Buscando Android SDK...
echo.

REM Ubicaciones comunes del SDK
set "locations[0]=%USERPROFILE%\AppData\Local\Android\Sdk"
set "locations[1]=%LOCALAPPDATA%\Android\Sdk"
set "locations[2]=C:\Android\Sdk"
set "locations[3]=C:\Users\%USERNAME%\Android\Sdk"
set "locations[4]=C:\Program Files\Android\Sdk"
set "locations[5]=C:\Program Files (x86)\Android\Sdk"

for /L %%i in (0,1,5) do (
    call set "path=%%locations[%%i]%%"
    if exist "!path!\platform-tools\adb.exe" (
        echo ✅ Android SDK encontrado en: !path!
        echo.
        echo Agregando a local.properties...
        echo sdk.dir=!path:\=\\!> S:\ReaderChunks\android\local.properties
        echo ✅ Configurado correctamente
        goto :found
    )
)

echo ❌ Android SDK no encontrado en ubicaciones comunes
echo.
echo Opciones:
echo 1. Abre Android Studio → File → Settings → Appearance & Behavior → System Settings → Android SDK
echo 2. Copia la ruta del "Android SDK Location"
echo 3. Agrega esa ruta a: S:\ReaderChunks\android\local.properties
echo    Formato: sdk.dir=C\\:\\ruta\\al\\sdk
echo.
pause
goto :end

:found
echo.
echo Ahora ejecuta: gradlew.bat assembleDebug
pause

:end
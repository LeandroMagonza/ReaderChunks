# 🔧 Debug Instructions - File Processing Issue

## 🚨 Problema Actual
- Usuario selecciona archivo (PDF/TXT/EPUB/MD)
- App procesa unos segundos
- Error: "Formato de archivo no soportado"

## 🔍 Diagnóstico Implementado

### 1. Logs Agregados
Hemos agregado logs detallados en:

**MainActivity.java:**
```java
android.util.Log.d("MainActivity", "Processing file: " + fileName);
android.util.Log.d("MainActivity", "URI: " + uri.toString());
android.util.Log.d("MainActivity", "Extension: " + extension);
```

**BookCacheManager.java:**
```java
android.util.Log.d("BookCacheManager", "Processing file: " + fileName + " with extension: " + extension);
android.util.Log.d("BookCacheManager", "Using extractor: " + extractor.getClass().getSimpleName());
```

**TextExtractorFactory.java:**
```java
android.util.Log.d("TextExtractorFactory", "Looking for extractor for extension: " + extension);
android.util.Log.d("TextExtractorFactory", "Initialized " + EXTRACTORS.size() + " extractors");
```

### 2. Cambios Implementados

1. **BookCacheManager**: Cambió de usar `TextExtractorFactory.getExtractorForUri(uri)` a `TextExtractorFactory.getExtractorForExtension(extension)` usando el fileName.

2. **MainActivity**: Mejoró `getFileNameFromUri()` para manejar URIs `content://` usando ContentResolver.

3. **TextExtractorFactory**: Agregó debug logging para diagnosticar problemas de inicialización.

## 🛠️ Pasos para Resolver

### Paso 1: Recompilar APK
```bash
cd android
gradlew clean
gradlew assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Paso 2: Verificar Logs
Usar Android Studio o ADB para ver los logs:
```bash
adb logcat -s MainActivity BookCacheManager TextExtractorFactory
```

### Paso 3: Test de Archivo Simple
1. Crear archivo test.txt con contenido simple
2. Copiarlo al dispositivo
3. Intentar abrirlo con ReaderChunks
4. Observar logs

### Paso 4: Verificar Errores Específicos

**Si los logs muestran:**
- `"Extension is null"` → Problema con extracción de filename
- `"No extractor found"` → Problema con inicialización de extractores
- `"No se pudo determinar el tipo de archivo"` → Problema con ContentResolver

## 🎯 Casos de Prueba Sugeridos

### Test 1: Archivo Local Directo
1. Crear archivo `test.txt` en `/storage/emulated/0/Download/`
2. Usar explorador de archivos
3. Tocar archivo → Abrir con ReaderChunks

### Test 2: Desde App Internal
1. Abrir ReaderChunks
2. Tocar FAB "Agregar Documento"
3. Seleccionar archivo conocido

### Test 3: Verificar Clases Cargadas
Los logs deberían mostrar:
```
TextExtractorFactory: Initialized 4 extractors:
TextExtractorFactory:   - PDFTextExtractorImpl
TextExtractorFactory:   - TXTTextExtractorImpl
TextExtractorFactory:   - MDTextExtractorImpl
TextExtractorFactory:   - EPUBTextExtractorImpl
```

## 🚨 Posibles Causas del Error

### Causa 1: Clases No Compiladas
- Las nuevas clases no están en la APK
- **Solución**: Clean rebuild

### Causa 2: Excepción en Constructor
- Error al crear instancias de extractores
- **Solución**: Ver logs de inicialización

### Causa 3: Problema con ContentResolver
- URI content:// no se puede leer
- **Solución**: Verificar permisos y logs de MainActivity

### Causa 4: Extensión No Detectada
- getFileExtension() retorna null
- **Solución**: Ver logs de extension detection

## 📱 Comandos Debug Útiles

### Ver todos los logs de la app:
```bash
adb logcat | grep "com.leandromg.readerchunks"
```

### Ver solo logs de debugging:
```bash
adb logcat -s MainActivity BookCacheManager TextExtractorFactory
```

### Clear logs y test fresh:
```bash
adb logcat -c
# Ahora probar abrir archivo
adb logcat -v time
```

### Verificar permisos:
```bash
adb shell dumpsys package com.leandromg.readerchunks | grep permission
```

## 🔄 Próximos Pasos

1. **INMEDIATO**: Recompilar APK con los nuevos logs
2. **TESTING**: Probar con archivo TXT simple
3. **DEBUGGING**: Analizar logs para identificar punto exacto de falla
4. **FIX**: Aplicar solución específica basada en logs

Una vez que tengas los logs, podremos identificar exactamente dónde está fallando el proceso.
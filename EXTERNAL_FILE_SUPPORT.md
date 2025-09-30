# 🔗 Soporte para Apertura de Archivos Externos

Esta documentación explica cómo se ha implementado la funcionalidad para que Android ofrezca BookBits como opción para abrir archivos PDF, TXT, Markdown y EPUB directamente desde otras aplicaciones.

## ⚙️ Configuración Implementada

### 1. Intent Filters en AndroidManifest.xml

Se han agregado Intent Filters que permiten a Android reconocer nuestra app como capaz de manejar archivos específicos:

#### Por MIME Type
```xml
<!-- PDF files -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:mimeType="application/pdf" />
</intent-filter>

<!-- TXT files -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:mimeType="text/plain" />
</intent-filter>

<!-- Markdown files -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:mimeType="text/markdown" />
</intent-filter>

<!-- EPUB files -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:mimeType="application/epub+zip" />
</intent-filter>
```

#### Por Extensión de Archivo (Compatibilidad Adicional)
```xml
<!-- PDF by extension -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="file" android:pathPattern=".*\\.pdf" />
</intent-filter>

<!-- Y así para .txt, .md, .markdown, .epub -->
```

### 2. Configuración de Activity

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop"
    android:theme="@style/Theme.BookBits">
```

- `android:exported="true"`: Permite que otras apps lancen nuestra actividad
- `android:launchMode="singleTop"`: Evita crear múltiples instancias de MainActivity

### 3. Permisos Necesarios

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_DOCUMENTS" />
```

## 🔧 Implementación en MainActivity

### Manejo de Intent Externos

```java
private void handleExternalFileIntent() {
    Intent intent = getIntent();
    if (Intent.ACTION_VIEW.equals(intent.getAction())) {
        Uri fileUri = intent.getData();
        if (fileUri != null) {
            String fileName = getFileNameFromUri(fileUri);
            Toast.makeText(this, "Abriendo " + fileName + "...", Toast.LENGTH_SHORT).show();
            processDocument(fileUri, fileName);
        }
    }
}

@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleExternalFileIntent();
}
```

### Extracción Mejorada de Nombres de Archivo

```java
private String getFileNameFromUri(Uri uri) {
    String fileName = "documento";

    // Handle content:// URIs using ContentResolver
    if ("content".equals(uri.getScheme())) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    String name = cursor.getString(nameIndex);
                    if (name != null && !name.trim().isEmpty()) {
                        fileName = name;
                    }
                }
            }
        } catch (Exception e) {
            // Fall back to path extraction
        }
    }

    // Handle file:// URIs
    if ("documento".equals(fileName)) {
        String path = uri.getPath();
        if (path != null) {
            int index = path.lastIndexOf('/');
            if (index != -1 && index < path.length() - 1) {
                fileName = path.substring(index + 1);
            }
        }
    }

    return fileName;
}
```

## 📱 Experiencia del Usuario

### Cómo Funciona

1. **Usuario toca un archivo** PDF, TXT, MD o EPUB en:
   - Explorador de archivos
   - Aplicación de descargas
   - Gmail (adjuntos)
   - WhatsApp (documentos)
   - Google Drive
   - Cualquier app que permita abrir documentos

2. **Android muestra opciones** incluyendo "BookBits"

3. **Usuario selecciona BookBits**:
   - Si la app no está abierta: Se lanza MainActivity
   - Si la app ya está abierta: Se activa onNewIntent()

4. **BookBits procesa el archivo**:
   - Muestra toast "Abriendo [nombre_archivo]..."
   - Detecta el formato automáticamente
   - Extrae el texto usando el extractor apropiado
   - Segmenta en párrafos
   - Guarda en cache
   - Agrega a la biblioteca
   - Usuario puede comenzar a leer inmediatamente

### Tipos de URI Soportados

- **file://**: Archivos locales (`/storage/emulated/0/...`)
- **content://**: Archivos de Content Providers (Google Drive, Descargas, etc.)

## 🎯 Formatos Soportados

| Formato | Extensiones | MIME Types | Estado |
|---------|-------------|------------|---------|
| **PDF** | `.pdf` | `application/pdf` | ✅ Completo |
| **Texto Plano** | `.txt` | `text/plain` | ✅ Completo |
| **Markdown** | `.md`, `.markdown` | `text/markdown`, `text/x-markdown` | ✅ Completo |
| **EPUB** | `.epub` | `application/epub+zip` | ✅ Completo |

## 🚀 Ventajas de Esta Implementación

1. **Integración Nativa**: BookBits aparece naturalmente en el menú "Abrir con..."
2. **Experiencia Fluida**: No es necesario abrir la app primero
3. **Compatibilidad Universal**: Funciona con cualquier app que comparta documentos
4. **Detección Automática**: El usuario no necesita especificar el tipo de archivo
5. **Procesamiento Inmediato**: El archivo se procesa y agrega a la biblioteca automáticamente

## 🔍 Testing

### Casos de Prueba Implementados

✅ **Intent Pattern Matching**: Verifica que los patrones de archivo coincidan correctamente
✅ **File Extension Handling**: Prueba todas las extensiones soportadas
✅ **MIME Type Handling**: Verifica el mapeo correcto de tipos MIME
✅ **URI Scheme Support**: Maneja tanto `file://` como `content://`
✅ **Filename Extraction**: Extrae nombres correctamente de diferentes fuentes

### Cómo Probar

1. **Método 1 - Explorador de Archivos**:
   - Copia un archivo PDF/TXT/MD/EPUB al dispositivo
   - Abre el explorador de archivos
   - Toca el archivo
   - Selecciona "BookBits"

2. **Método 2 - Compartir desde otra App**:
   - Abre Gmail, WhatsApp, Drive, etc.
   - Busca un documento adjunto
   - Toca "Abrir con..."
   - Selecciona "BookBits"

3. **Método 3 - ADB (Desarrollo)**:
   ```bash
   adb shell am start -a android.intent.action.VIEW -t "application/pdf" -d "file:///storage/emulated/0/Download/document.pdf"
   ```

## 🛠️ Troubleshooting

### Si no aparece BookBits en "Abrir con..."

1. **Verificar instalación**: Asegurar que la app esté instalada correctamente
2. **Verificar permisos**: Android puede necesitar permisos explícitos para algunos archivos
3. **Limpiar defaults**: En Configuración > Apps > [App que abre el archivo] > Abrir por defecto > Borrar valores predeterminados
4. **Reinstalar**: Como último recurso, desinstalar y reinstalar BookBits

### Limitaciones Conocidas

- **Archivos muy grandes**: Pueden tardar más en procesarse
- **PDFs protegidos**: Algunos PDFs con DRM pueden no abrirse
- **Archivos corruptos**: Se mostrarán errores apropiados
- **Formato no reconocido**: Solo se procesan los formatos soportados

## 📈 Próximas Mejoras

- [ ] **Soporte para más formatos** (DOC, RTF, HTML)
- [ ] **Preview antes de procesar** archivos grandes
- [ ] **Procesamiento en background** para mejor UX
- [ ] **Configuración de defaults** para tipos de archivo específicos
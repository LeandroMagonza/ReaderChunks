# TTS Functionality Backup - BookBits

## Descripción
Este archivo contiene toda la funcionalidad TTS que se implementó en el proyecto BookBits, incluyendo las mejoras y correcciones realizadas.

## Archivos Involucrados

### 1. TTSManager.java
**Ubicación**: `android/app/src/main/java/com/leandromg/readerchunks/TTSManager.java`

#### Funcionalidades principales:
- **Gestión de Text-to-Speech (TTS)**
- **Detección automática de idioma del texto**
- **Configuración por libro de idioma TTS**
- **Preload de texto para reducir delays**
- **Gestión de voces disponibles**
- **Control de velocidad de lectura**
- **Auto-scroll automático tras lectura**

#### Métodos clave agregados:
- `speakText(String text)` - Hablar texto con validaciones
- `preloadText(String text)` - Precargar texto siguiente
- `setLanguage(Locale locale)` - Configurar idioma con fallbacks
- `detectLanguage(String text)` - Detectar idioma del texto
- `getAvailableLanguages()` - Obtener idiomas disponibles
- `setToDeviceDefaultLanguage()` - Fallback seguro
- `trySetLanguage(Locale locale)` - Helper para configurar idioma
- `handleTTSError()` - Manejo de errores

### 2. SentenceReaderActivity.java
**Ubicación**: `android/app/src/main/java/com/leandromg/readerchunks/SentenceReaderActivity.java`

#### Funcionalidades TTS agregadas:
- **Integración con TTSManager**
- **Configuración automática de idioma por texto**
- **Dialog de configuración TTS**
- **Control de auto-scroll**
- **Botón TTS con estados visuales**

#### Métodos TTS agregados:
- `setupTTS()` - Inicializar TTS
- `speakCurrentText()` - Hablar texto actual
- `setupTTSLanguageForCurrentText(String text)` - Configurar idioma
- `showTTSSettingsDialog()` - Mostrar configuración
- `loadSavedLanguage()` - Cargar idioma guardado
- `preloadNextText()` - Precargar siguiente texto
- `updateTTSButtonIcon()` - Actualizar icono botón
- `getAutoScrollDelay()` - Calcular delay auto-scroll

### 3. SettingsManager.java
**Ubicación**: `android/app/src/main/java/com/leandromg/readerchunks/SettingsManager.java`

#### Configuraciones TTS agregadas:
- `isTTSEnabled()` / `setTTSEnabled(boolean)`
- `isTTSAutoScrollEnabled()` / `setTTSAutoScrollEnabled(boolean)`
- `getTTSSpeechRate()` / `setTTSSpeechRate(float)`
- `getTTSVoiceName()` / `setTTSVoiceName(String)`
- `getTTSLanguageForBook(String bookId)` / `setTTSLanguageForBook(String, String)`

### 4. Layout TTS Dialog
**Ubicación**: `android/app/src/main/res/layout/dialog_tts_settings.xml`

#### Componentes del dialog:
- Switch para habilitar/deshabilitar TTS
- SeekBar para velocidad de lectura
- Spinner para selección de idioma
- Switch para auto-scroll
- Botones Cancelar/Guardar

### 5. Recursos agregados
**Ubicación**: `android/app/src/main/res/`

#### Iconos:
- `drawable/ic_volume_up.xml` - Icono TTS activado
- `drawable/ic_volume_off.xml` - Icono TTS desactivado

#### Strings (en values/strings.xml y variantes de idioma):
- `tts_settings` - "Configuración de Voz"
- `tts_enabled` - "Lectura en voz alta"
- `tts_enabled_description` - "Activar síntesis de voz"
- `tts_speed` - "Velocidad"
- `tts_language` - "Idioma"
- `tts_language_description` - "Idioma para este libro"
- `tts_auto_scroll` - "Avance automático"
- `tts_auto_scroll_description` - "Avanzar automáticamente tras leer"
- `language_auto_detect` - "Detectar automáticamente"
- `slow` - "Lento"
- `fast` - "Rápido"
- `normal_speed` - "Normal"
- `tts_enabled_message` - "Lectura en voz alta activada"
- `tts_disabled_message` - "Lectura en voz alta desactivada"

## Problemas Detectados

### Error "Error de síntesis de voz"
**Causas identificadas:**
1. Configuración de idioma que no está disponible en el dispositivo
2. Falta de validación del estado TTS antes de usar
3. Manejo insuficiente de errores y fallbacks
4. Idiomas configurados que no tienen datos TTS instalados

### Soluciones Implementadas
1. **Fallbacks múltiples** en configuración de idioma
2. **Validación de estado** antes de cada operación TTS
3. **Manejo robusto de excepciones**
4. **Logging detallado** para debug
5. **Recuperación automática** de errores

## Configuración en build.gradle
No se requieren dependencias adicionales ya que TTS es parte del Android SDK.

## Uso del TTS

### Flujo básico:
1. Usuario abre libro en SentenceReaderActivity
2. Se inicializa TTSManager con listener
3. Se detecta/configura idioma del texto
4. Al navegar, se puede hablar texto actual
5. Auto-scroll opcional tras completar lectura

### Configuración por usuario:
- Habilitar/deshabilitar TTS
- Ajustar velocidad (0.5x - 2.0x)
- Seleccionar idioma por libro
- Activar auto-scroll

## Estados del Botón TTS
- **Activado**: Icono volumen alto, opacidad 100%
- **Desactivado**: Icono volumen bajo, opacidad 60%

## Detección Automática de Idioma
Basada en heurísticas simples:
- **Español**: Caracteres ñ, ¿, ¡ y palabras comunes
- **Portugués**: Caracteres ã, õ, ç y palabras comunes
- **Francés**: Caracteres à, é, è, ç y palabras comunes
- **Alemán**: Caracteres ä, ö, ü, ß y palabras comunes
- **Italiano**: Palabras comunes específicas
- **Inglés**: Fallback por defecto

## Notas Importantes
- TTS se detiene automáticamente al cambiar de texto
- Se preloads el siguiente texto para reducir delays
- Configuración se guarda por libro individualmente
- Compatible con modo párrafo y modo oración
- Auto-scroll tiene delays diferentes según contexto
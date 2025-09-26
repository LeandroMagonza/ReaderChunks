# 📱 ReaderChunks - Native Android App

## 📌 Objetivo
**ReaderChunks** es una aplicación nativa de Android que transforma la lectura de PDFs en una experiencia más digerible y gamificada.
El usuario puede cargar un documento y leerlo **una oración a la vez**, con navegación simple y visualización de progreso.
El foco está en **mejorar la concentración**, ofrecer "lectura por bocados" y fomentar el hábito con pequeños logros diarios.

---

## ✨ Funcionalidades (MVP)

- **Carga de PDF**: el usuario selecciona un archivo desde su dispositivo.
- **Validación**: si el PDF no contiene texto extraíble (ej. escaneo), se muestra un error.
- **Extracción de texto**: se procesa el PDF y se obtiene el contenido completo en texto plano usando PDFBox.
- **Segmentación**: el texto se divide en **oraciones** (posteriormente, se podrá extender a párrafos o chunks configurables).
- **Lectura progresiva**:
  - Se muestra **una oración por pantalla**.
  - Botón flotante para pasar a la siguiente oración.
  - Tap lateral para retroceder.
- **Progreso visual**: barra inferior que refleja el avance respecto al total de oraciones.
- **Mensajes de error claros**: cuando el documento no contiene texto o no puede abrirse.

---

## 🚀 Roadmap (futuras mejoras)

- **OCR integrado** (para PDFs escaneados con imágenes).
- **Persistencia de progreso** por documento.
- **Animaciones de gamificación** (confetti, logros, misiones diarias).
- **Modo offline total**: lectura sin depender de internet.
- **Biblioteca de documentos** y rachas de lectura.

---

## 🛠️ Tecnologías

### Base de la aplicación
- **Android Native (Java)**
  UI nativa, navegación, estados, manejo de archivos y chunking de texto.

### Extracción de texto de PDF
- **PDFBox-Android** (Java)
  Permite abrir PDFs y extraer texto plano de manera **offline**.

### Librerías auxiliares
- Intents de Android para seleccionar archivos PDF desde el sistema.
- Componentes nativos de Android para UI y navegación.

---

## 🧩 Arquitectura y justificación

### ¿Por qué Android Nativo?
- **Máximo rendimiento** y acceso completo a las APIs del sistema.
- **Tamaño de APK reducido** comparado con frameworks híbridos.
- **Mejor integración** con el sistema de archivos y permisos de Android.
- **Desarrollo enfocado** en una sola plataforma para el MVP.

### ¿Por qué PDFBox para extracción?
- **Librería madura y probada** para manipulación de PDFs.
- **Funciona offline** sin necesidad de APIs externas.
- **Rápida y precisa** para PDFs con texto real.
- **Bien soportada** en el ecosistema Java/Android.

---

## 📱 Estructura del Proyecto

```
ReaderChunks/
├── android/                 # Proyecto Android nativo
│   ├── app/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/com/leandromg/ReaderChunks/
│   │   │       │   ├── MainActivity.java
│   │   │       │   ├── PDFTextExtractor.java
│   │   │       │   └── SentenceReader.java
│   │   │       ├── res/                 # Recursos (layouts, strings, etc.)
│   │   │       └── AndroidManifest.xml
│   │   ├── build.gradle
│   │   └── proguard-rules.pro
│   ├── gradle/
│   ├── build.gradle
│   └── settings.gradle
├── PDFTextExtractor.java    # Versión standalone para testing
├── pdfbox-app-2.0.27.jar   # Librería PDFBox
└── README.md
```

---

## 🚀 Instalación y Setup

### Prerrequisitos
- **Android Studio** instalado
- **Java SDK 8+** configurado
- **Android SDK** con nivel API 21+ (Android 5.0)
- **PDFBox JAR** (ya incluido en el proyecto)

### Instalación
1. Abrir el proyecto en Android Studio
2. Sincronizar dependencias de Gradle
3. Conectar dispositivo Android o configurar emulador
4. Compilar y ejecutar la aplicación

### Testing con la versión standalone
```bash
# Compilar el extractor standalone
javac -cp pdfbox-app-2.0.27.jar PDFTextExtractor.java

# Probar con un PDF
java -cp ".;pdfbox-app-2.0.27.jar" PDFTextExtractor example.pdf
```

---

## 📋 Funcionalidades Implementadas

- [x] **Extracción de texto de PDF** usando PDFBox
- [x] **Validación de archivos** (existencia, formato, encriptación)
- [x] **Manejo de errores** robusto con mensajes claros
- [x] **Testing standalone** para verificar extracción

### Próximos pasos
- [ ] **Actividad principal** con selección de archivos
- [ ] **Segmentación en oraciones** del texto extraído
- [ ] **UI de lectura** con navegación entre oraciones
- [ ] **Barra de progreso** visual
- [ ] **Persistencia** del progreso de lectura

---

## 🔧 Comandos útiles

```bash
# Compilar proyecto Android
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug

# Ejecutar tests
./gradlew test

# Limpiar proyecto
./gradlew clean
```

---

## 📝 Notas de desarrollo

- El proyecto migró de React Native a Android nativo para mejor rendimiento y menor complejidad.
- PDFBox se mantiene como dependencia principal para extracción de texto.
- La arquitectura está diseñada para ser simple y mantenible.
- El foco inicial es Android, con posible expansión a iOS en el futuro.
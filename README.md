# 📱 ReaderChunks - Android App

## 📌 Objetivo
**ReaderChunks** es una aplicación Android que transforma la lectura de PDFs en una experiencia más digerible y gamificada.
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
- **Java para Android**
  UI nativa, navegación, estados, manejo de archivos y chunking de texto.

### Extracción de texto de PDF
- **PDFBox** (Java)
  Permite abrir PDFs y extraer texto plano de manera **offline**.

### Librerías auxiliares
- Intents de Android para seleccionar archivos PDF desde el sistema.
- Componentes nativos de Android para UI y navegación.

---

## 🧩 Arquitectura y justificación

### ¿Por qué Java nativo para Android?
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
├── PDFTextExtractor.java    # Extractor de texto de PDF (standalone)
├── PDFTextExtractor.class   # Compilado del extractor
├── example.pdf              # PDF de prueba
└── README.md               # Este archivo
```

---

## 🚀 Testing en PC

### Prerrequisitos
- **Java JDK 8+** instalado
- **PDFBox JAR** descargado

### Descargar PDFBox
```bash
# Descargar PDFBox JAR
wget https://archive.apache.org/dist/pdfbox/2.0.27/pdfbox-app-2.0.27.jar
```

### Compilar y probar
```bash
# Compilar el extractor
javac -cp pdfbox-app-2.0.27.jar PDFTextExtractor.java

# Probar con el PDF de ejemplo
java -cp ".;pdfbox-app-2.0.27.jar" PDFTextExtractor example.pdf
```

---

## 📱 Estructura Android (próxima fase)

Una vez validado el extractor, se creará:
```
android/
├── app/
│   ├── src/main/java/com/leandromg/readerchunks/
│   │   ├── MainActivity.java
│   │   ├── PDFTextExtractor.java
│   │   └── SentenceReader.java
│   ├── src/main/res/
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

---

## 📋 Estado Actual

- [x] **Extracción de texto de PDF** usando PDFBox
- [x] **Validación de archivos** (existencia, formato, encriptación)
- [x] **Manejo de errores** robusto con mensajes claros
- [x] **Testing standalone** para verificar extracción
- [x] **Proyecto Android completo** creado
- [x] **MainActivity** con selector de archivos
- [x] **SentenceReaderActivity** para navegación
- [x] **Integración PDFBox-Android** funcional
- [x] **UI completa** con Material Design

### Para compilar
1. **Instalar Android SDK**
2. **Ejecutar**: `cd android && gradlew assembleDebug`
3. **APK generado en**: `android/app/build/outputs/apk/debug/`
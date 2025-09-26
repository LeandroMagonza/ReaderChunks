# 📱 ReaderChunks

## 📌 Objetivo
**ReaderChunks** es una aplicación móvil que transforma la lectura de PDFs en una experiencia más digerible y gamificada.  
El usuario puede cargar un documento y leerlo **una oración a la vez**, con navegación simple y visualización de progreso.  
El foco está en **mejorar la concentración**, ofrecer “lectura por bocados” y fomentar el hábito con pequeños logros diarios.

---

## ✨ Funcionalidades (MVP)

- **Carga de PDF**: el usuario selecciona un archivo desde su dispositivo.
- **Validación**: si el PDF no contiene texto extraíble (ej. escaneo), se muestra un error.
- **Extracción de texto**: se procesa el PDF y se obtiene el contenido completo en texto plano.
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
- **React Native (JavaScript/TypeScript)**  
  UI, navegación, estados, manejo de archivos y chunking de texto.

### Extracción de texto de PDF
- **Android**: `PDFBox-Android` (Java/Kotlin)  
  Permite abrir PDFs y extraer texto plano de manera **offline**.
- **iOS**: `PDFKit` (Swift)  
  Framework oficial de Apple para manipulación de PDFs.

### Librerías auxiliares
- `react-native-document-picker` → para seleccionar archivos PDF desde el sistema.
- Hooks de React → manejo de estado e índice de oración.

---

## 🧩 Arquitectura y justificación

### ¿Por qué React Native?
- Permite mantener **una sola base de código** para UI y lógica compartida.
- Ecosistema maduro para animaciones, navegación y desarrollo rápido.
- Comunidad amplia y soporte multiplataforma.

### ¿Por qué extracción nativa (Java/Swift) y no JS puro?
- En RN no existen librerías fiables y mantenidas para **extraer texto de PDFs on-device**.
- Soluciones JS como `pdf.js` no se integran bien en móviles (rendimiento, compatibilidad).
- Una API externa funcionaría, pero:
  - ❌ Requiere internet.
  - ❌ Puede afectar privacidad y costos.
  - ❌ Añade latencia en la lectura.
- La extracción nativa con **PDFBox-Android** y **PDFKit**:
  - ✅ Es offline.
  - ✅ Rápida y precisa para PDFs con texto real.
  - ✅ Bien soportada y probada en producción.
- El módulo nativo expone un método simple a React Native:
  ```ts
  const text = await NativeModules.TextExtractor.extractText(filePath);

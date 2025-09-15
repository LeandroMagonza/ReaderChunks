# 📱 App: **ReaderChunks**

---

## 📌 Resumen

**ReaderChunks** es una aplicación móvil mínima (MVP) desarrollada en **Expo (React Native + TypeScript)** que permite cargar un PDF, verificar si contiene texto extraíble y mostrarlo al usuario de manera progresiva, una oración a la vez. El objetivo es crear un flujo de lectura sencillo y gamificado, que fomente la concentración y el avance paso a paso.

---

## ⚙️ Flujo del MVP

1. **Inicio**

   * Pantalla inicial con botón **"Elegir PDF"**.
   * Se abre un selector de archivos usando **expo-document-picker**.

2. **Validación del PDF**

   * El archivo seleccionado se procesa usando **expo-file-system**.
   * Si no se puede extraer texto (escaneo o PDF vacío) → se muestra error.

3. **Extracción de texto**

   * Usando **pdf-lib** o **react-native-pdf** para extraer texto del PDF.
   * Se obtiene el texto crudo del documento.

4. **Procesamiento**

   * El texto se divide en **oraciones** usando expresiones regulares.
   * Se arma una lista de strings cortos, listos para navegar.

5. **Lectura progresiva**

   * La app muestra **una oración por pantalla**.
   * Botón flotante (abajo a la derecha) permite avanzar.
   * Tap en el lado izquierdo permite retroceder.

6. **Indicador de progreso**

   * Barra de progreso lineal, que muestra cuántas oraciones se han leído respecto al total.

---

## 🛠️ Tecnologías seleccionadas

* **Expo (React Native + TypeScript)** → Base del proyecto.
* **expo-document-picker** → Selección de PDFs desde el sistema.
* **expo-file-system** → Manejo de archivos.
* **react-native-pdf-lib** → Extracción de texto de PDFs (requiere prebuild).
* **Hooks y estados de React** → Manejo del índice de oración y navegación.
* **Expresiones regulares** → Segmentación en oraciones.
* **GitHub Actions** → Compilación automática sin instalaciones locales.
* **Expo Prebuild** → Acceso a librerías nativas para extracción real de PDF.

---

## 🚧 Limitaciones del MVP

1. **Sin OCR**: PDFs escaneados (imágenes) no son soportados.
2. **Texto plano**: no hay manejo especial de columnas, tablas o notas al pie.
3. **Sin persistencia**: no se guarda progreso entre sesiones.
4. **UI mínima**: no incluye animaciones, gamificación avanzada ni personalización de fuentes/temas.
5. **Un solo archivo a la vez**: no hay biblioteca de documentos ni historial.
6. **Lenguaje**: segmentación de oraciones simple, puede fallar en abreviaciones o signos de puntuación complejos.

---

## ✅ Próximos pasos posibles

* Añadir **OCR (ML Kit / Tesseract)** para soportar escaneos.
* Guardar **progreso de lectura** con AsyncStorage o base local.
* Incluir **animaciones y confetti** al finalizar un bloque.
* Misiones diarias y rachas.
* Soporte para **temas visuales** (oscuro/claro, tamaño de fuente).
* Biblioteca de PDFs con progreso independiente.

---

## 🚀 Instalación y ejecución

### **Desarrollo Local (Managed Expo)**

⚠️ **NOTA**: En Expo Go solo puedes testear la **interfaz y navegación** con texto de ejemplo. La **extracción real de PDF** requiere compilación nativa.

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/ReaderChunks.git
   cd ReaderChunks
   ```

2. **Instalar dependencias:**
   ```bash
   npm install
   ```

3. **Ejecutar en modo desarrollo:**
   ```bash
   npm start
   ```
   Escanea el QR con **Expo Go** desde tu móvil.

**En Expo Go podrás testear:**
✅ Interfaz de usuario y navegación
✅ Selector de archivos
✅ División de texto en oraciones (con texto demo)
✅ Navegación entre oraciones
❌ Extracción real de PDF (requiere build nativo)

### **Compilación Nativa con GitHub Actions**

Este proyecto usa **GitHub Actions** para compilar automáticamente sin necesidad de instalaciones locales.

1. **Push a GitHub** → **Builds automáticos** se generan
2. **Descargar APK/IPA** desde GitHub Releases
3. **Sin Android Studio ni Xcode** requerido

#### **Setup inicial para compilación:**

1. **Hacer prebuild localmente** (solo una vez):
   ```bash
   npx expo prebuild --clean
   ```

2. **Instalar dependencias nativas:**
   ```bash
   npm install react-native-pdf-lib
   ```

3. **Configurar GitHub Actions** (archivo `.github/workflows/build.yml` incluido)

4. **Push al repositorio** → Compilación automática

### **Dependencias principales:**
```bash
npx expo install expo-document-picker expo-file-system
npm install react-native-pdf-lib  # Requiere prebuild
```

### **Comandos útiles:**
```bash
npm run lint      # Verificar código
npm run typecheck # Verificar tipos TypeScript
npm test          # Ejecutar pruebas
npx expo prebuild # Generar archivos nativos
```

---

## 📦 Distribución y Costos

### **Estrategia de distribución gratuita:**

* **GitHub Público** → Código abierto, compilación gratuita
* **GitHub Actions** → 2000 minutos/mes gratis para builds
* **GitHub Releases** → Distribución APK/IPA gratuita
* **Google Play Store** → $25 una sola vez (registro desarrollador)
* **Apple App Store** → $99/año (registro desarrollador)

### **Costos totales:**
- **Desarrollo**: $0 (sin instalaciones locales)
- **Hosting código**: $0 (GitHub público)
- **Compilación**: $0 (GitHub Actions)
- **Distribución Android**: $25 una vez
- **Distribución iOS**: $99/año
- **Backend**: $0 (sin costos recurrentes)

### **Ventajas del enfoque:**
✅ App **totalmente offline** (sin servidor)
✅ **Privacidad total** (PDFs no salen del dispositivo)
✅ **Sin costos recurrentes** (mantener gratis indefinidamente)
✅ **Compilación en la nube** (sin Android Studio/Xcode)
✅ **Código abierto** (comunidad puede contribuir)

---

📖 Con este MVP, se logra una app mínima pero funcional que demuestra el concepto de "lectura por oraciones" con navegación simple y extracción real de PDFs válidos.

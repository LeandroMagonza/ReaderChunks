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
  UI nativa con Material Design, RecyclerView, navegación entre Activities.

### Extracción y cache de texto
- **PDFBox-Android** (Java)
  Extracción de texto de PDFs de manera **offline**.
- **Sistema de cache persistente**
  Procesamiento único por libro, almacenamiento en archivos de texto.
- **Buffer inteligente**
  Carga por bloques con pre-carga anticipada para navegación fluida.
- **Segmentación inteligente**
  Respeta la estructura del documento (títulos, párrafos, secciones).

### Gestión de datos
- **JSON** para metadata de libros (progreso, fechas, configuración).
- **Archivos de texto plano** para contenido (una oración por línea).
- **MD5 hash** para identificación única de PDFs.

### Librerías auxiliares
- Intents de Android para seleccionar archivos PDF desde el sistema.
- RecyclerView y CardView para lista de libros.
- FloatingActionButton para agregar nuevos libros.

---

## 📝 Sistema de Segmentación Inteligente

### Algoritmo de dos niveles

**1. Nivel archivo (persistente)**
- Dividir por **dobles saltos de línea** (párrafos y secciones)
- Preservar **títulos cortos** como oraciones independientes
- Detectar **estructura del documento** (autor, capítulos, etc.)
- Insertar marcadores `[BREAK]` entre párrafos para visualización

**2. Nivel presentación (futuro)**
- División dinámica de oraciones largas (>400 chars)
- Adaptación según tamaño de pantalla y fuente
- Corte inteligente en puntuación: `;`, `:`, `,`, espacios

### Detección automática de estructura
```
PDF original:
Miguel de Cervantes     <- Línea vacía debajo
                        <- Línea vacía
El ingenioso hidalgo... <- Línea vacía debajo
                        <- Línea vacía
Parte I                 <- Línea vacía debajo

Resultado segmentado:
1. Miguel de Cervantes El ingenioso hidalgo don quijote de la Mancha.
2. [BREAK]
3. Parte I.
4. [BREAK]
5. Tasa.
6. [BREAK]
7. Yo, Juan Gallo de Andrada... (párrafo completo)
```

### Ventajas del sistema
- ✅ **Archivos portables**: La segmentación es consistente entre dispositivos
- ✅ **Preserva estructura**: Títulos, autores y secciones se mantienen separados
- ✅ **Visualización mejorada**: Indicadores visuales de cambio de párrafo
- ✅ **Escalable**: Funciona con libros de cualquier tamaño y estructura

---

## 🧩 Arquitectura y justificación

### ¿Por qué Java nativo para Android?
- **Máximo rendimiento** y acceso completo a las APIs del sistema.
- **Tamaño de APK reducido** comparado con frameworks híbridos.
- **Mejor integración** con el sistema de archivos y permisos de Android.
- **Desarrollo enfocado** en una sola plataforma para el MVP.

### ¿Por qué sistema de cache persistente?
- **Procesamiento único** - Cada PDF se procesa solo una vez.
- **Acceso instantáneo** - Sin esperas al retomar lectura.
- **Escalabilidad** - Funciona con libros de cualquier tamaño.
- **Memoria eficiente** - Solo 50-100 oraciones en RAM simultáneamente.

### ¿Por qué buffer inteligente?
- **Pre-carga anticipada** - Carga siguiente bloque al 70% de progreso.
- **Navegación fluida** - Usuario nunca espera por contenido.
- **Retroceso rápido** - Buffer anterior disponible inmediatamente.
- **Limpieza automática** - Libera memoria de contenido lejano.

---

## 📱 Estructura del Proyecto

```
ReaderChunks/
├── android/                           # Proyecto Android completo
│   ├── app/src/main/java/com/leandromg/readerchunks/
│   │   ├── MainActivity.java          # Biblioteca de libros
│   │   ├── SentenceReaderActivity.java # Lectura con buffer
│   │   ├── Book.java                  # Modelo de libro
│   │   ├── BookCacheManager.java      # Gestión de cache
│   │   ├── BufferManager.java         # Buffer inteligente
│   │   ├── BookAdapter.java           # Adaptador RecyclerView
│   │   ├── PDFTextExtractor.java      # Extracción para Android
│   │   └── SentenceSegmenter.java     # Segmentación de oraciones
│   ├── app/src/main/res/
│   │   ├── layout/                    # Layouts de Activities
│   │   └── values/                    # Strings, colores, temas
│   └── app/build.gradle               # Dependencias Android
├── PDFTextExtractor.java              # Versión standalone (testing)
├── example.pdf                        # PDF de prueba
└── README.md                          # Este archivo
```

### Estructura de datos persistente:
```
/data/app/books/
├── library.json                       # Lista de todos los libros
├── {hash_libro_1}/
│   ├── content.txt                    # Oraciones (una por línea)
│   └── meta.json                      # Progreso, título, fechas
└── {hash_libro_2}/
    ├── content.txt
    └── meta.json
```

---

## 🧪 Testing

### Testing en PC (versión standalone)
```bash
# Compilar extractor standalone
javac -cp pdfbox-app-2.0.27.jar PDFTextExtractor.java

# Probar extracción
java -cp ".;pdfbox-app-2.0.27.jar" PDFTextExtractor example.pdf
```

### Testing Android
```bash
# Compilar APK
cd android && gradlew assembleDebug

# Instalar en dispositivo
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Casos de prueba recomendados
- **PDF pequeño** (< 50 páginas) - Funcionamiento básico
- **PDF grande** (Don Quijote, +1000 páginas) - Sistema de buffer
- **PDF con imágenes** - Extracción solo del texto
- **PDF encriptado** - Manejo de errores
- **Múltiples libros** - Gestión de biblioteca

---

## 🏗️ Arquitectura del sistema

### Flujo de datos
```
PDF seleccionado → Hash MD5 → ¿Existe cache?
                                 ↓ No
                    PDFBox → Texto → Segmentación → Cache
                                 ↓ Sí
                    Cargar metadata → BufferManager → Lectura
```

### Gestión de memoria para libros grandes
```
Libro de 10,000 oraciones:
├── En disco: 10,000 oraciones (archivo content.txt)
├── En memoria: ~75 oraciones máximo
│   ├── Buffer anterior: 25 oraciones
│   ├── Ventana actual: 50 oraciones
│   └── Buffer siguiente: 25 oraciones (pre-cargado)
└── Carga bajo demanda según navegación del usuario
```

---

## 📋 Estado Actual

### ✅ Sistema Core
- [x] **Extracción de texto de PDF** usando PDFBox-Android
- [x] **Sistema de cache persistente** con archivos JSON + TXT
- [x] **Buffer inteligente** con pre-carga y limpieza automática
- [x] **Identificación única** de PDFs por hash MD5
- [x] **Manejo de errores** robusto en toda la aplicación
- [x] **Segmentación inteligente** respetando estructura del documento
- [x] **Detección automática** de títulos, secciones y párrafos
- [x] **Marcadores [BREAK]** para preservar puntos y aparte

### ✅ Interfaz y UX
- [x] **Biblioteca personal** con lista de libros procesados
- [x] **Progreso persistente** - retomar donde quedaste
- [x] **Material Design** moderno con RecyclerView
- [x] **Navegación fluida** sin esperas de carga
- [x] **Estados visuales** (vacío, cargando, lista)
- [x] **Indicadores visuales** de cambio de párrafo

### ✅ Funcionalidades principales
- [x] **Agregar libros** desde selector de archivos
- [x] **Procesamiento único** - cache automático
- [x] **Lectura por oraciones** con navegación
- [x] **Guardado automático** de progreso
- [x] **Gestión de memoria** eficiente para libros grandes

### 🚀 Para usar
1. **Compilar**: `cd android && gradlew assembleDebug`
2. **APK**: `android/app/build/outputs/apk/debug/app-debug.apk`
3. **Instalar** en dispositivo Android
4. **Agregar libro** → Seleccionar PDF → ¡Leer!

### 📈 Siguientes mejoras
- [ ] **División dinámica de oraciones largas** según pantalla
- [ ] **Configuración de longitud máxima** de visualización
- [ ] **Navegación por sub-oraciones** en textos densos
- [ ] **Botón eliminar libro** de la biblioteca
- [ ] **Botón resetear progreso** de lectura
- [ ] **Soporte TXT y EPUB** (formatos adicionales)
- [ ] **Configuración de tamaño de fuente**
- [ ] **Modo oscuro** y temas personalizables
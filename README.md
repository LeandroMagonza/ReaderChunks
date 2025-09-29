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

## 🚀 Roadmap (próximas mejoras)

- **OCR integrado** (para PDFs escaneados con imágenes).
- **Animaciones de gamificación** (confetti, logros, misiones diarias).
- **Rachas de lectura** y estadísticas detalladas.
- **Menú de opciones por libro** (eliminar, renombrar, restablecer progreso).
- **Mejoras en indicadores de progreso** (barra de párrafo + círculo de progreso total).

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
- **División dinámica en tiempo real**
  Oraciones largas se dividen automáticamente para mejor lectura.

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

**2. Nivel presentación (implementado)**
- División dinámica de oraciones largas (>150 chars)
- Corte inteligente en puntuación: `;`, `:`, `,`, espacios
- Navegación por sub-oraciones con indicador visual
- Adaptación automática sin reprocesar archivos

### Funcionamiento completo del sistema
```
PDF original:
Miguel de Cervantes     <- Línea vacía debajo
                        <- Línea vacía
El ingenioso hidalgo... <- Línea vacía debajo
                        <- Línea vacía
Parte I                 <- Línea vacía debajo

Archivo procesado:
1. Miguel de Cervantes El ingenioso hidalgo don quijote de la Mancha.
2. [BREAK]
3. Parte I.
4. [BREAK]
5. Tasa.
6. [BREAK]
7. Yo, Juan Gallo de Andrada... (627 caracteres)

Visualización dinámica:
Oración 7.1 (128 chars): Yo, Juan Gallo de Andrada, escribano de Cámara del Rey...
Oración 7.2 (133 chars): habiendo visto por los señores dél un libro intitulado...
Oración 7.3 (98 chars): tasaron cada pliego del dicho libro...
[navegación por sub-oraciones con indicador (2/6)]
```

### Ventajas del sistema
- ✅ **Archivos portables**: La segmentación es consistente entre dispositivos
- ✅ **Preserva estructura**: Títulos, autores y secciones se mantienen separados
- ✅ **Visualización mejorada**: Indicadores visuales de cambio de párrafo
- ✅ **División inteligente**: Oraciones largas se dividen automáticamente
- ✅ **Navegación fluida**: Sub-oraciones con indicador de progreso (2/6)
- ✅ **Sin reprocesamiento**: Archivos cache no cambian, solo la presentación
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

### ¿Por qué división dinámica?
- **Lectura mejorada** - Oraciones de máximo 150 caracteres.
- **Corte inteligente** - División por puntuación: `;`, `:`, `,`, espacios.
- **Sin reprocesamiento** - Los archivos cache no cambian.
- **Navegación por partes** - Indicador visual de sub-oraciones.
- **Adaptable** - Longitud configurable según preferencias futuras.

---

## 📱 Estructura del Proyecto

```
ReaderChunks/
├── android/                           # Proyecto Android completo
│   ├── app/src/main/java/com/leandromg/readerchunks/
│   │   ├── MainActivity.java          # Biblioteca de libros
│   │   ├── SentenceReaderActivity.java # Lectura con navegación inteligente
│   │   ├── Book.java                  # Modelo de libro
│   │   ├── BookCacheManager.java      # Gestión de cache
│   │   ├── BufferManager.java         # Buffer inteligente
│   │   ├── BookAdapter.java           # Adaptador RecyclerView
│   │   ├── PDFTextExtractor.java      # Extracción para Android
│   │   ├── SentenceSegmenter.java     # Segmentación de oraciones
│   │   └── DynamicSentenceSplitter.java # División dinámica en tiempo real
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

### ✅ Sistema Core Completo
- [x] **Extracción de texto de PDF** usando PDFBox-Android
- [x] **Sistema de cache persistente** con archivos JSON + TXT
- [x] **Buffer inteligente 3-párrafos** con pre-carga asíncrona
- [x] **Identificación única** de PDFs por hash MD5
- [x] **Manejo de errores** robusto en toda la aplicación
- [x] **Segmentación por párrafos** preservando texto original exacto
- [x] **División dinámica en tiempo real** con posiciones pre-calculadas
- [x] **Validación inteligente de cortes** (evita cortar en URLs, IPs, abreviaciones)
- [x] **Algoritmo resiliente** - posiciones por carácter, no por algoritmo

### ✅ Interfaz y UX Completa
- [x] **Biblioteca personal** con lista de libros procesados
- [x] **Persistencia de progreso completa** - retomar exacto donde quedaste
- [x] **Modo completamente offline** - sin necesidad de internet
- [x] **Material Design** moderno con RecyclerView
- [x] **Navegación fluida** sin esperas de carga
- [x] **Estados visuales** (vacío, cargando, lista, error)
- [x] **Indicadores visuales** de cambio de párrafo
- [x] **Controles táctiles** responsivos (botones anterior/siguiente)

### ✅ Algoritmo de Lectura Avanzado
- [x] **División inteligente por prioridades**: `:` > `;` > `,` > espacio
- [x] **Preservación de texto original** - cero modificaciones al contenido
- [x] **Validación de caracteres de corte** - solo si seguidos de espacio
- [x] **Navegación por oraciones** dentro de párrafos
- [x] **Tracking por posición de carácter** para resistencia a cambios de algoritmo
- [x] **Buffer de 3 párrafos** (anterior, actual, siguiente)
- [x] **Carga asíncrona** sin bloquear la UI

### 🎯 Experiencia de Lectura Optimizada
- [x] **Oraciones optimizadas** - máximo 150 caracteres para lectura cómoda
- [x] **Navegación intuitiva** - botones anterior/siguiente con estados inteligentes
- [x] **Indicadores de progreso avanzados** - sistema dual de progreso (ver detalles abajo)
- [x] **Separación visual** - divisores entre párrafos
- [x] **Lectura fluida** - sin esperas, cortes, o texto corrupto
- [x] **Preservación exacta** - "!a Mancha" se mantiene como "!a Mancha"

### 📊 Sistema de Progreso Dual (IMPLEMENTADO)
- [x] **Progreso de párrafos** - "28 / 284" en línea superior del header
- [x] **Progreso de oraciones** - "(1/18)" en línea inferior (solo si hay >1 oración)
- [x] **Corona circular de progreso** - círculo que se llena con % total del libro (ej: "28.3%")
- [x] **Consistencia de porcentajes** - mismo valor con 1 decimal en lista y modo lectura
- [x] **Base de cálculo**: porcentaje basado en **párrafos completados**, no caracteres
- [x] **Cálculo**: `(párrafo_actual / párrafos_totales) * 100`

**Ventajas del sistema dual**:
- ✅ **Progreso inmediato**: Ver avance dentro del párrafo actual
- ✅ **Progreso general**: Corona circular muestra % total del libro
- ✅ **Sin aumentar altura**: Mantiene dimensiones originales del header
- ✅ **Información clara**: Párrafos y oraciones en líneas separadas

### 🚀 Para usar
1. **Compilar**: `cd android && gradlew assembleDebug`
2. **APK**: `android/app/build/outputs/apk/debug/app-debug.apk`
3. **Instalar** en dispositivo Android
4. **Agregar libro** → Seleccionar PDF → ¡Leer!

### 📈 Próximas mejoras

#### 🎯 Gestión de libros (Prioridad Alta)
- [ ] **Menú de opciones por libro** con:
  - [ ] **Eliminar libro** (con confirmación y limpieza de archivos)
  - [ ] **Restablecer progreso** (volver al inicio con confirmación)
  - [ ] **Renombrar libro** (cambiar título con input de texto)
- [ ] **Estadísticas de lectura** (tiempo, párrafos completados, progreso diario)

#### 🎨 Mejoras adicionales en progreso (Prioridad Media)
- [ ] **Barra de progreso del párrafo actual** (opcional - reemplazar barra inferior)
  - [ ] Muestra visualmente el progreso de oraciones dentro del párrafo actual
  - [ ] Se rellena completamente al terminar cada párrafo
  - [ ] Proporciona satisfacción inmediata al completar párrafos
- [ ] **Animaciones suaves** en la corona circular al cambiar de párrafo
- [ ] **Diferentes colores** para diferentes rangos de progreso (0-25%, 25-50%, etc.)

#### 🎛️ Personalización
- [ ] **Configuración de longitud máxima** de corte dinámico (150 chars por defecto)
- [ ] **Configuración de tamaño de fuente** (pequeña, mediana, grande, extra grande)
- [ ] **Tema oscuro** (fondo negro, texto blanco) y personalización de colores
- [ ] **Velocidad de lectura** y métricas de progreso

#### 👆 Navegación y gestos
- [ ] **Navegación por swipe**:
  - [ ] **Swipe izquierda** → Siguiente oración/sub-oración
  - [ ] **Swipe derecha** → Oración/sub-oración anterior
  - [ ] **Combinación** con botones existentes para máxima flexibilidad
- [ ] **Gestos adicionales** para navegación rápida entre párrafos

#### 📚 Formatos y funcionalidades avanzadas
- [ ] **Soporte TXT y EPUB** (formatos adicionales)
- [ ] **Marcadores y favoritos** en posiciones específicas
- [ ] **Búsqueda de texto** dentro de libros
- [ ] **Exportar progreso** y sincronización entre dispositivos
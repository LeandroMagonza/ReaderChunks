# 📱 BookBits - Android App

## 📖 Lectura Segmentada

La Lectura Segmentada es un enfoque de lectura que consiste en dividir un texto en fragmentos breves y manejables —ya sean oraciones o párrafos— y presentarlos de forma secuencial.

Este método busca:

- Reducir la sobrecarga cognitiva, al enfocar la atención en una unidad pequeña de información.
- Favorecer la concentración sostenida, evitando distracciones propias de bloques largos de texto.
- Generar motivación intrínseca, al permitir micro-avances medibles y celebrados (gamificación).
- Adaptarse a contextos móviles, donde leer en "bocados" es más natural que abordar páginas enteras.

En el caso de BookBits, la Lectura Segmentada se implementa mostrando una oración por vez, con navegación simple y un indicador de progreso que convierte el acto de leer en una experiencia más dinámica y gratificante.

## 📌 Objetivo
**BookBits** es una aplicación Android que transforma la lectura de PDFs en una experiencia más digerible y gamificada.
El usuario puede cargar un documento y leerlo **una oración a la vez**, con navegación simple y visualización de progreso.
El foco está en **mejorar la concentración**, ofrecer "lectura por bocados" y fomentar el hábito con pequeños logros diarios.

---

## ✨ Funcionalidades Implementadas

### 📄 Gestión de Documentos
- **Carga de PDF**: selección desde el dispositivo con validación completa
- **Soporte múltiple**: PDF (implementado), TXT, MD, EPUB (próximamente)
- **Identificación única**: hash MD5 para evitar duplicados
- **Sistema de cache**: procesamiento único por documento

### 📚 Modos de Lectura
- **Modo bite-size**: una oración por pantalla (máximo 150 caracteres)
- **Modo párrafo completo**: párrafo completo con scroll
- **Toggle intuitivo**: botón |—| / |☰| para cambiar entre modos
- **Persistencia individual**: cada libro recuerda su modo de lectura preferido

### 🎮 Navegación Inteligente
- **Navegación por botones**: anterior/siguiente con estados inteligentes
- **Gestos swipe**: deslizar izquierda/derecha para navegar
- **División dinámica**: oraciones largas se dividen automáticamente
- **Indicadores visuales**: separadores entre párrafos

### 📊 Sistema de Progreso Avanzado
- **Progreso dual**: párrafos (28/284) y oraciones (1/18) por separado
- **Corona circular**: porcentaje preciso del libro completado
- **Persistencia completa**: retomar exactamente donde se quedó
- **Cálculo preciso**: basado en posición de carácter, no estimaciones

---


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
BookBits/
├── android/                           # Proyecto Android completo
│   ├── app/src/main/java/com/leandromg/bookbits/
│   │   ├── MainActivity.java          # Biblioteca de libros
│   │   ├── SentenceReaderActivity.java # Lectura dual: bite-size ↔ párrafo
│   │   ├── Book.java                  # Modelo con persistencia de modo
│   │   ├── BookCacheManager.java      # Cache + persistencia de preferencias
│   │   ├── BufferManager.java         # Buffer con soporte dual
│   │   ├── BookAdapter.java           # Adaptador RecyclerView
│   │   ├── PDFTextExtractor.java      # Extracción PDF (PDFBox)
│   │   ├── SentenceSegmenter.java     # Segmentación de oraciones
│   │   ├── DynamicSentenceSplitter.java # División dinámica en tiempo real
│   │   └── ThemeManager.java          # Gestión de temas (modo oscuro/claro)
│   ├── app/src/main/res/
│   │   ├── layout/                    # Layouts con ScrollView y toggle
│   │   └── values/                    # Strings con iconografía |—| |☰|
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

## 📋 Estado Actual - Funcionalidades Implementadas

### ✅ Sistema Core Completo
- [x] **Extracción de texto PDF** usando PDFBox-Android
- [x] **Sistema de cache persistente** con archivos JSON + TXT
- [x] **Buffer inteligente 3-párrafos** con pre-carga asíncrona
- [x] **Identificación única** por hash MD5
- [x] **Manejo de errores robusto** en toda la aplicación
- [x] **Segmentación por párrafos** preservando texto original exacto
- [x] **División dinámica en tiempo real** con posiciones pre-calculadas
- [x] **Validación inteligente de cortes** (evita cortar URLs, IPs, abreviaciones)
- [x] **Algoritmo resiliente** - posiciones por carácter, no por algoritmo

### ✅ Interfaz y UX Completa
- [x] **Biblioteca personal** con lista de libros procesados
- [x] **Persistencia de progreso completa** - retomar exactamente donde se quedó
- [x] **Modo completamente offline** - sin necesidad de internet
- [x] **Material Design moderno** con RecyclerView
- [x] **Navegación fluida** sin esperas de carga
- [x] **Estados visuales** (vacío, cargando, lista, error)
- [x] **Indicadores visuales** de cambio de párrafo
- [x] **Controles táctiles responsivos** (botones anterior/siguiente)

### ✅ Sistema de Lectura Dual (NUEVO)
- [x] **Toggle de modos**: |—| (bite-size) ↔ |☰| (párrafo completo)
- [x] **Modo bite-size**: una oración por pantalla (máx 150 chars)
- [x] **Modo párrafo**: párrafo completo con scroll vertical
- [x] **Persistencia por libro**: cada libro recuerda su modo preferido
- [x] **Iconografía intuitiva**: símbolos que muestran el modo actual
- [x] **Navegación por gestos**: swipe izquierda/derecha compatible con ambos modos
- [x] **Transiciones fluidas**: cambio de modo sin pérdida de posición

### ✅ Algoritmo de Lectura Avanzado
- [x] **División inteligente por prioridades**: `:` > `;` > `,` > espacio
- [x] **Preservación de texto original** - cero modificaciones al contenido
- [x] **Validación de caracteres de corte** - solo si seguidos de espacio
- [x] **Navegación por oraciones** dentro de párrafos
- [x] **Tracking por posición de carácter** para resistencia a cambios
- [x] **Buffer de 3 párrafos** (anterior, actual, siguiente)
- [x] **Carga asíncrona** sin bloquear la UI

### ✅ Sistema de Progreso Dual
- [x] **Progreso de párrafos**: "28 / 284" en header
- [x] **Progreso de oraciones**: "(1/18)" solo en modo bite-size
- [x] **Corona circular**: porcentaje preciso del libro (ej: "28.3%")
- [x] **Consistencia**: mismo porcentaje en biblioteca y modo lectura
- [x] **Cálculo preciso**: basado en párrafos completados + posición de carácter
- [x] **Adaptación por modo**: UI se adapta según modo de lectura activo

### ✅ Experiencia de Lectura Optimizada
- [x] **Oraciones optimizadas** - máximo 150 caracteres
- [x] **Navegación intuitiva** - botones con estados inteligentes
- [x] **Separación visual** - divisores entre párrafos
- [x] **Lectura fluida** - sin esperas, cortes, o texto corrupto
- [x] **Preservación exacta** del contenido original
- [x] **Scroll suave** en modo párrafo para textos largos
- [x] **Transiciones de modo sin pérdida** - cambio bite-size ↔ párrafo mantiene posición exacta
- [x] **Tracking de posición por carácter** - persistencia precisa entre modos de lectura
- [x] **UI optimizada** - FAB reubicado, botón [+] en header y estado vacío
- [x] **Buffer inteligente sin sesgo** - eliminación de drift acumulativo en navegación

### 🚀 Cómo Usar
1. **Compilar**: `cd android && gradlew assembleDebug`
2. **APK**: `android/app/build/outputs/apk/debug/app-debug.apk`
3. **Instalar** en dispositivo Android
4. **Agregar libro** → Seleccionar PDF → ¡Leer!
5. **Toggle de modo** → Usar botón |—| / |☰| para cambiar entre bite-size y párrafo completo
6. **Navegación** → Botones o swipe izquierda/derecha para avanzar/retroceder

## 🚀 Roadmap - Próximas Mejoras

### ✅ Formatos de Archivo
- [x] **TXT**: Soporte para archivos de texto plano
- [x] **Markdown**: Archivos .md con formato básico
- [x] **EPUB**: Libros electrónicos estándar
- [x] **Detección automática**: Identificar formato por extensión

### ✅ Gestión de Libros
- [x] **Menú de opciones por libro**:
  - [x] Eliminar libro (con confirmación y limpieza)
  - [x] Restablecer progreso (volver al inicio)
  - [x] Renombrar libro (cambiar título)
- [x] **Estadísticas de lectura** (progreso % mostrado en biblioteca)
- [ ] **Ordenación de biblioteca** (por fecha de última lectura)

### 🎨 Experiencia de Usuario
- [x] **Temas y personalización**:
  - [x] Modo oscuro/claro
  - [ ] Tamaños de fuente configurables
  - [ ] Colores personalizables
- [x] **Animaciones y feedback**:
  - [x] Transiciones suaves en cambio de modo
  - [x] Animaciones de progreso
  - [ ] Feedback háptico en navegación
- [ ] **Pantalla de configuración**:
  - [ ] Selección de idioma
  - [ ] Métodos de navegación (botones/swipe lateral o vertical/tocar bordes)
  - [ ] Configuración de fuentes 
  - [ ] Configuración de márgenes

### 🎛️ Configuración Avanzada
- [ ] **Parámetros de lectura**:
  - [ ] Longitud máxima de división (150 chars por defecto)
  - [ ] Velocidad de auto-avance
  - [ ] Configuración de gestos
- [ ] **Métricas y objetivos**:
  - [ ] Metas diarias de lectura
  - [ ] Estadísticas detalladas
  - [ ] Rachas de lectura

### 📈 Funcionalidades Avanzadas
- [ ] **Búsqueda y navegación**:
  - [ ] Búsqueda de texto dentro de libros
  - [ ] Marcadores y favoritos
  - [ ] Salto rápido a posiciones
- [ ] **Sincronización y respaldo**:
  - [ ] Exportar/importar progreso
  - [ ] Respaldo en la nube
  - [ ] Sincronización entre dispositivos

### 🖼️ Soporte de Imágenes en Libros
- [ ] **Extracción y almacenamiento**:
  - [ ] Extraer imágenes de PDFs usando PDFBox (PDImageXObject)
  - [ ] Extraer imágenes de EPUBs (archivos JPG/PNG del ZIP)
  - [ ] Almacenar en carpeta `/images/` dentro del directorio del libro
  - [ ] Generar hash único para cada imagen
  - [ ] Comprimir imágenes grandes (máximo 1024x1024, JPEG 85%)

- [ ] **Sistema de marcadores en texto**:
  - [ ] Formato de marcador: `[IMG:hash_único:descripción_alt]`
  - [ ] Insertar marcadores en posición correcta durante extracción
  - [ ] Preservar texto alternativo (alt text) de HTML en EPUBs
  - [ ] Mapear referencias `<img src="">` a marcadores en texto
  - [ ] Mantener orden correcto imagen-texto en el flujo de lectura

- [ ] **Visualización en la aplicación**:
  - [ ] Detectar marcadores `[IMG:...]` al renderizar contenido
  - [ ] En modo bite-size: mostrar imagen como "oración" completa
  - [ ] En modo párrafo: insertar ImageView en posición del marcador
  - [ ] Implementar zoom con pellizco (pinch-to-zoom)
  - [ ] Escalado adaptativo según ancho de pantalla
  - [ ] Lazy loading de imágenes según navegación
  - [ ] Cache de imágenes decodificadas en memoria

- [ ] **Estructura de almacenamiento**:
  ```
  /data/app/books/{hash_libro}/
  ├── content.txt           # Texto con marcadores [IMG:...]
  ├── meta.json            # Metadata del libro
  └── images/              # Nueva carpeta para imágenes
      ├── img_001.jpg
      ├── img_002.png
      └── manifest.json  # Mapeo de IDs a archivos y metadata
  ```

- [ ] **Detalles técnicos de implementación**:
  - [ ] PDFBox ya incluido soporta extracción con `PDPage.getResources()`
  - [ ] EPUB: las imágenes están en `/images/` o `/OEBPS/images/` del ZIP
  - [ ] Modificar `PDFTextExtractorImpl.java` para extraer imágenes
  - [ ] Modificar `EPUBTextExtractorImpl.java` para procesar `<img>` tags
  - [ ] Crear `ImageExtractor.java` como clase helper
  - [ ] Actualizar `BookCacheManager.java` para gestionar carpeta images
  - [ ] Modificar `SentenceReaderActivity.java` para renderizar imágenes
  - [ ] Añadir ImageView dinámico en `activity_sentence_reader.xml`
  - [ ] Opcional: usar Glide/Picasso para mejor manejo de imágenes

### 🌐 Configuración de Idiomas (Futura Funcionalidad)
- [ ] **Interfaz multiidioma**:
  - [ ] Español (por defecto)
  - [ ] Inglés
  - [ ] Portugués
  - [ ] Francés
  - [ ] Alemán
- [ ] **Selector en configuración**:
  - [ ] RadioGroup para elegir idioma
  - [ ] Persistencia en SharedPreferences
  - [ ] Aplicación inmediata al cambiar
- [ ] **Estructura de archivos**:
  - [ ] values-en/strings.xml (Inglés)
  - [ ] values-pt/strings.xml (Portugués)
  - [ ] values-fr/strings.xml (Francés)
  - [ ] values-de/strings.xml (Alemán)
- [ ] **LanguageManager.java**:
  - [ ] Clase helper para cambio programático
  - [ ] Métodos: setLocale(), getStoredLanguage(), saveLanguage()
  - [ ] Aplicar configuración al iniciar actividades

### 🔧 Mejoras Técnicas
- [ ] **OCR integrado** para PDFs escaneados
- [ ] **Optimización de memoria** para libros muy grandes
- [ ] **Formato de cache mejorado** con compresión
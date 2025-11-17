# Colectivo Base Maven

## Descripción
Proyecto académico Java (Maven, Java 21+) para gestión de recorridos de transporte público. 
Incluye múltiples implementaciones de DAO (Secuencial, Aleatorio, PostgreSQL) e interfaces (Consola, JavaFX con WebView).

## Estructura del Proyecto
- **Maven estándar**: Código fuente en `src/main/java`, tests en `src/test/java`
- **Recursos**: Archivos de configuración en `src/main/resources/`

---

## ⚙️ Configuración del Sistema

El sistema utiliza 6 archivos `.properties` ubicados en `src/main/resources/` para configurar diferentes aspectos de la aplicación. A continuación se explica cada uno en detalle:

---

### 📄 1. `factory.properties` - Selección de Implementaciones

**Propósito**: Define qué implementaciones de DAO e Interfaz se utilizarán en tiempo de ejecución.

**⚠️ REGLA IMPORTANTE**: Se debe descomentar **SOLO UNA** implementación de cada tipo (INTERFAZ, y UN grupo completo de DAOs).

```properties
# ===== INTERFAZ (descomentar SOLO UNA) =====
#INTERFAZ=colectivo.interfaz.consola.InterfazConsola
INTERFAZ=colectivo.interfaz.javafx.InterfazJavaFX

# ===== DAO - PostgreSQL (recomendado para mapas) =====
LINEA=colectivo.dao.postgresql.LineaPostgresqlDAO
PARADA=colectivo.dao.postgresql.ParadaPostgresqlDAO
TRAMO=colectivo.dao.postgresql.TramoPostgresqlDAO

# ===== DAO - Secuencial (archivos .txt) =====
#LINEA=colectivo.dao.secuencial.LineaSecuencialDAO
#PARADA=colectivo.dao.secuencial.ParadaSecuencialDAO
#TRAMO=colectivo.dao.secuencial.TramoSecuencialDAO

# ===== DAO - Aleatorio (archivos .dat binarios) =====
#LINEA=colectivo.dao.aleatorio.LineaAleatorioDAO
#PARADA=colectivo.dao.aleatorio.ParadaAleatorioDAO
#TRAMO=colectivo.dao.aleatorio.TramoAleatorioDAO
```

**Combinaciones válidas**:
- ✅ JavaFX + PostgreSQL → **Mapas interactivos con coordenadas GPS**
- ✅ JavaFX + Secuencial → Interfaz gráfica sin mapas
- ✅ JavaFX + Aleatorio → Interfaz gráfica sin mapas
- ✅ Consola + PostgreSQL/Secuencial/Aleatorio → Interfaz de texto

---

### 📄 2. `jdbc.properties` - Conexión a Base de Datos PostgreSQL

**Propósito**: Configura la conexión a PostgreSQL y selecciona la ciudad (schema) a visualizar.

**⚠️ REGLA IMPORTANTE**: Se debe descomentar **SOLO UN** schema (ciudad) a la vez.

```properties
usr=estudiante
pwd=estudiante
driver=org.postgresql.Driver
url=jdbc:postgresql://pgs.fi.mdn.unp.edu.ar:30000/bd1

# ===== SELECCIONAR CIUDAD (descomentar SOLO UNA línea) =====
schema=colectivo_PM    # Puerto Madryn (por defecto)
#schema=colectivo_AZL  # Azul
#schema=colectivo_CO   # Comodoro Rivadavia
#schema=colectivo_GP   # General Pico
#schema=colectivo_HL   # HonoLulu
#schema=colectivo_TW   # Trelew
```

**Uso**: Este archivo solo es relevante cuando `factory.properties` tiene activos los DAOs PostgreSQL.

**Cambio de ciudad**: Comentar el schema actual y descomentar el deseado, luego reiniciar la aplicación.

---

### 📄 3. `secuencial.properties` - Selección de Archivos de Texto

**Propósito**: Define qué archivos de texto (`.txt`) leer cuando se usan los DAOs Secuenciales.

**⚠️ REGLA IMPORTANTE**: Descomentar **SOLO UN** conjunto de archivos (una ciudad) a la vez.

```properties
# ===== Ciudad: Puerto Madryn (comentada actualmente) =====
#linea=linea_PM.txt
#parada=parada_PM.txt
#tramo=tramo_PM.txt
#frecuencia=frecuencia_PM.txt

# ===== Ciudad: Trelew (activa) =====
linea=linea_TW.txt
parada=parada_TW.txt
tramo=tramo_TW.txt
frecuencia=frecuencia_TW.txt
```

**Uso**: Este archivo solo es relevante cuando `factory.properties` tiene activos los DAOs Secuenciales.

**Cambio de ciudad**: Comentar el conjunto actual y descomentar el conjunto deseado (los 4 archivos deben corresponder a la misma ciudad).

**Ubicación de archivos**: Los archivos deben estar en `src/main/resources/` o en el directorio raíz del proyecto ejecutable.

---

### 📄 4. `aleatorio.properties` - Ubicación de Archivos Binarios

**Propósito**: Define la ubicación relativa de los archivos binarios (`.dat`) para lectura/escritura cuando se usan los DAOs Aleatorios.

```properties
linea=data/linea.dat
parada=data/parada.dat
tramo=data/tramo.dat
```

**Comportamiento**:
- **Primera ejecución**: Si los archivos `.dat` no existen, se crean automáticamente en `data/` poblándolos desde los DAOs Secuenciales.
- **Ejecuciones posteriores**: Lee directamente de los archivos binarios (acceso aleatorio más rápido).

**Uso**: Este archivo solo es relevante cuando `factory.properties` tiene activos los DAOs Aleatorios.

**Rutas relativas**: Las rutas son relativas al directorio de trabajo de la aplicación (típicamente la raíz del proyecto Maven).

---

### 📄 5. `config.properties` - Configuración General de la Aplicación

**Propósito**: Configuración de internacionalización, rutas de vistas FXML y estilos CSS.

**⚠️ REGLA IMPORTANTE**: Descomentar **SOLO UN** idioma (language/country) a la vez.

```properties
# ===== Internacionalización (descomentar SOLO UN par) =====
labels=labels
language=es
country=ES
#language=en
#country=US

# ===== Interfaz JavaFX =====
vista=/colectivo/interfaz/view.fxml
estiloOscuro=/colectivo/interfaz/modoOscuro.css
estiloClaro=/colectivo/interfaz/modoClaro.css
```

**Funcionalidad de idioma**:
- **Consola**: El idioma seleccionado aquí determina el idioma de la interfaz de texto.
- **JavaFX**: El idioma aquí define el idioma **inicial** al arrancar. Una vez abierta, la interfaz gráfica permite **cambiar el idioma dinámicamente** desde un menú o botón.

**Archivos de recursos de idioma**:
- `labels_es_ES.properties` → Español
- `labels_en_US.properties` → Inglés

---

### 📄 6. `log4j2.properties` - Configuración de Logging

**Propósito**: Controla el nivel de logging, formato de mensajes y destinos (consola/archivo).

```properties
# ===== Nivel de logging en CONSOLA (cambiar según necesidad) =====
appender.consola.filter.level.level = WARN  # Opciones: DEBUG, INFO, WARN, ERROR
appender.consola.filter.level.onMatch = ACCEPT
appender.consola.filter.level.onMismatch = DENY

# ===== Archivo de log diario (solo nivel INFO) =====
appender.infoFile.filePattern = logs/info-%d{yyyy-MM-dd}.log
appender.infoFile.filter.infoOnly.level = INFO

# ===== Nivel del logger raíz =====
rootLogger.level = info  # Opciones: debug, info, warn, error
```

**Niveles de logging** (de más detallado a menos):
1. **DEBUG**: Información detallada para diagnóstico (ej: "Parada 123 agregada a línea XYZ")
2. **INFO**: Mensajes informativos generales (guardados en archivo)
3. **WARN**: Advertencias (ej: parada no encontrada)
4. **ERROR**: Errores que requieren atención

**Configuración recomendada**:
- **Desarrollo/Debug**: `consola.level = DEBUG` y `rootLogger.level = debug`
- **Producción**: `consola.level = WARN` y `rootLogger.level = info` (configuración actual)

**Ubicación de logs**:
- **Consola**: Salida estándar de Eclipse/terminal (solo WARN y ERROR por defecto)
- **Archivo**: `logs/info-YYYY-MM-DD.log` (un archivo por día, rotación automática)
- **Retención**: Los archivos de log se eliminan automáticamente después de 30 días

**Cambiar nivel de consola a DEBUG** (para ver mensajes de duplicados):
```properties
appender.consola.filter.level.level = DEBUG
```

---

## 📋 Resumen de Configuración por Caso de Uso

### 🗺️ Caso 1: Mapas Interactivos (Recomendado)
```
factory.properties:
  ✓ INTERFAZ=...InterfazJavaFX
  ✓ DAOs PostgreSQL activos

jdbc.properties:
  ✓ Descomentar UN schema (ej: colectivo_PM)

config.properties:
  ✓ Descomentar UN idioma (ej: es/ES)

log4j2.properties:
  ✓ consola.level = WARN (o DEBUG para diagnóstico)
```

### 📄 Caso 2: Datos desde Archivos de Texto
```
factory.properties:
  ✓ INTERFAZ=...InterfazJavaFX o InterfazConsola
  ✓ DAOs Secuenciales activos

secuencial.properties:
  ✓ Descomentar UN conjunto de archivos (ej: *_TW.txt)

config.properties:
  ✓ Descomentar UN idioma

log4j2.properties:
  ✓ consola.level = WARN (o DEBUG)
```

### 💾 Caso 3: Archivos Binarios (Acceso Aleatorio)
```
factory.properties:
  ✓ INTERFAZ=...InterfazJavaFX o InterfazConsola
  ✓ DAOs Aleatorios activos

aleatorio.properties:
  ✓ Verificar rutas en data/*.dat

config.properties:
  ✓ Descomentar UN idioma

Nota: Primera ejecución crea archivos .dat desde datos secuenciales
```


---

## 🚀 Compilación y Ejecución

### Opción 1: Maven (Línea de Comandos)

```bash
# Compilar proyecto
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar aplicación JavaFX
mvn javafx:run

# Empaquetar JAR
mvn package
```

### Opción 2: Eclipse - Maven Build

1. **Run → Run Configurations... → Maven Build** (nuevo)
2. **Base Directory**: `${project_loc:colectivo-base-maven}`
3. **Goals**: `javafx:run`
4. **Apply** → **Run**

✅ **Esta es la forma recomendada** - Maven gestiona automáticamente JavaFX y sus módulos.

### Opción 3: Eclipse - Java Application (Run Configuration Manual)

Para ejecutar directamente como aplicación Java (sin Maven):

1. **Run → Run Configurations... → Java Application** (nuevo)
2. **Main class**: `colectivo.aplicacion.AplicacionConsultas`
3. **Arguments tab → VM arguments**:

```
--module-path "E:\javafx-sdk-21.0.8\lib" --add-modules javafx.controls,javafx.fxml,javafx.web --add-exports=javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED --add-exports=javafx.web/com.sun.javafx.sg.prism.web=ALL-UNNAMED --add-exports=javafx.web/com.sun.javafx.scene.web=ALL-UNNAMED
```

⚠️ **Importante**:
- Ajustar la ruta `--module-path` a tu instalación de JavaFX SDK
- **Incluir `javafx.web`** en `--add-modules` (requerido para WebView/mapas)
- **Incluir todas las exportaciones** mostradas (necesarias para componentes internos de JavaFX)

---

## 🗺️ Uso de la Aplicación con Mapas

### Requisitos para visualización de mapas:
1. ✅ `INTERFAZ=colectivo.interfaz.javafx.InterfazJavaFX` en `factory.properties`
2. ✅ DAO PostgreSQL activo (contiene coordenadas geográficas)
3. ✅ Schema correcto seleccionado en `jdbc.properties` (sin comentar)
4. ✅ Conexión a internet (para cargar tiles de OpenStreetMap)

### Ciudades disponibles:
- **colectivo_PM** - Puerto Madryn
- **colectivo_AZL** - Azul
- **colectivo_CO** - Comodoro Rivadavia
- **colectivo_GP** - General Pico
- **colectivo_HL** - HonoLulu
- **colectivo_TW** - Trelew

---

## 🔧 Solución de Problemas Comunes

### Error: "Unknown module: javafx.web"
**Causa**: Falta incluir `javafx.web` en `--add-modules`  
**Solución**: Usar los VM arguments completos mostrados arriba (incluir `javafx.web`)

### Error: "IllegalAccessError ... WebViewHelper"
**Causa**: Faltan exportaciones de paquetes internos de JavaFX  
**Solución**: Usar todos los `--add-exports` mostrados en la configuración

### Error: No se conecta a la base de datos
**Causa**: Schema comentado o configuración incorrecta en `jdbc.properties`  
**Solución**: Verificar que solo UNA línea `schema=...` esté sin comentar

### El mapa no se visualiza
**Verificar**:
- Conexión a internet activa
- Schema PostgreSQL seleccionado (los archivos secuenciales no tienen coordenadas)
- Consola de Eclipse/logs para errores de JavaFX WebView

---

## 📦 Dependencias Principales

- **Java 21+** (required)
- **JavaFX 21.0.8** (controls, fxml, web)
- **PostgreSQL JDBC Driver 42.7.8**
- **JUnit 5.11.3** (tests)

---

## 📝 Notas de Desarrollo

- Siempre seleccionar **solo UNA** implementación de cada tipo (DAO, INTERFAZ, schema)
- Para cambiar entre interfaces, editar `factory.properties` y reiniciar
- Para internacionalización, cambiar `language`/`country` en `config.properties`
- En Windows, entrecomillar rutas con espacios en VM arguments

---

## 📚 Recursos Adicionales

- **Repositorio**: Proyecto académico POO 2025
- **Licencia**: Seguir reglas del curso y licencia del repositorio

# Colectivo Base (Sin Maven)

## Descripción
Proyecto académico Java (Java 21+) para gestión de recorridos de transporte público. 
Este proyecto **NO usa Maven** - las dependencias se gestionan manualmente mediante archivos JAR.

Incluye múltiples implementaciones de DAO (Secuencial, Aleatorio, PostgreSQL) e interfaces (Consola, JavaFX con WebView).

## Estructura del Proyecto
```
colectivo-base/
├── src/                          # Código fuente Java
├── bin/                          # Clases compiladas
├── lib-JavaFX/                   # ⚠️ Librerías JavaFX (agregar manualmente)
├── lib-log4j/                    # ⚠️ Log4j JARs (agregar manualmente)
├── lib-postgre/                  # ⚠️ PostgreSQL JDBC Driver (agregar manualmente)
├── config.properties             # Configuración principal
├── jdbc.properties               # Configuración de base de datos
└── factory.properties            # Selección de implementaciones
```

---

## 📦 Configuración de Dependencias (Librerías JAR)

### ⚠️ IMPORTANTE: Este proyecto requiere configuración manual de librerías

Debes crear las siguientes carpetas en la raíz del proyecto y agregar los JARs correspondientes:

### 1. **lib-JavaFX/** 
Descargar JavaFX SDK 21+ desde: https://openjfx.io/

Agregar los siguientes JARs a la carpeta `lib-JavaFX/`:
- `javafx.base.jar`
- `javafx.controls.jar`
- `javafx.fxml.jar`
- `javafx.graphics.jar`
- `javafx.media.jar`
- `javafx.swing.jar`
- `javafx.web.jar`
- Archivos nativos de la plataforma (`.dll` en Windows, `.so` en Linux)

### 2. **lib-log4j/**
Descargar Log4j 2.25.2 desde: https://logging.apache.org/log4j/2.x/download.html

Agregar los siguientes JARs a la carpeta `lib-log4j/`:
- `log4j-api-2.25.2.jar`
- `log4j-core-2.25.2.jar`

### 3. **lib-postgre/**
Descargar PostgreSQL JDBC Driver desde: https://jdbc.postgresql.org/download/

Agregar el JAR a la carpeta `lib-postgre/`:
- `postgresql-42.7.8.jar` (o versión compatible)

### 4. **JUnit 4 y JUnit 5** (Configuración en IDE)
**No crear carpeta** - agregar mediante configuración del IDE:

#### En Eclipse:
1. **Click derecho en el proyecto** → **Build Path** → **Configure Build Path...**
2. **Libraries tab** → **Add Library...** → **JUnit**
3. Seleccionar **JUnit 5** → **Next** → **Finish**
4. Repetir para agregar **JUnit 4** (si es necesario para tests legacy)

#### En IntelliJ IDEA:
1. **File** → **Project Structure** → **Libraries**
2. **+** → **From Maven...** → Buscar `junit:junit:4.13.2` y `org.junit.jupiter:junit-jupiter:5.11.3`

---

## 🔧 Configuración del Classpath en el IDE

### Eclipse - Agregar Librerías al Build Path

1. **Click derecho en el proyecto** → **Build Path** → **Configure Build Path...**
2. **Libraries tab** → **Add External JARs...**
3. Navegar y seleccionar **todos** los JARs de:
   - `lib-JavaFX/`
   - `lib-log4j/`
   - `lib-postgre/`
4. **Apply and Close**

### IntelliJ IDEA - Agregar Librerías

1. **File** → **Project Structure** → **Modules**
2. **Dependencies tab** → **+** → **JARs or directories...**
3. Seleccionar las carpetas `lib-JavaFX/`, `lib-log4j/`, `lib-postgre/`
4. **Apply** → **OK**

---

## ⚙️ Configuración de Archivos de Propiedades

### 📄 `factory.properties` (raíz del proyecto)
Selecciona qué implementaciones usar (DAO e Interfaz):

```properties
# Seleccionar INTERFAZ (descomentar UNA línea)
#INTERFAZ=colectivo.interfaz.consola.InterfazConsola
INTERFAZ=colectivo.interfaz.javafx.InterfazJavaFX

# Seleccionar DAO (descomentar UN grupo)
# PostgreSQL (recomendado para JavaFX con mapas)
LINEA=colectivo.dao.postgresql.LineaPostgresqlDAO
PARADA=colectivo.dao.postgresql.ParadaPostgresqlDAO
TRAMO=colectivo.dao.postgresql.TramoPostgresqlDAO

# Secuencial (archivos .txt)
#LINEA=colectivo.dao.secuencial.LineaSecuencialDAO
#PARADA=colectivo.dao.secuencial.ParadaSecuencialDAO
#TRAMO=colectivo.dao.secuencial.TramoSecuencialDAO
```

### 📄 `jdbc.properties` (raíz del proyecto)
**Configuración de base de datos PostgreSQL:**

```properties
usr=estudiante
pwd=estudiante
driver=org.postgresql.Driver
url=jdbc:postgresql://pgs.fi.mdn.unp.edu.ar:30000/bd1

# IMPORTANTE: Descomentar el schema de la ciudad que deseas visualizar
schema=colectivo_PM    # Puerto Madryn (por defecto)
#schema=colectivo_AZL  # Azul
#schema=colectivo_CO   # Comodoro Rivadavia
#schema=colectivo_GP   # General Pico
#schema=colectivo_HL   # HonoLulu
# Colectivo Base (Sin Maven)

## Descripción

Proyecto académico Java (Java 21+) para gestión de recorridos de transporte público. Este proyecto **NO usa Maven**: las dependencias se gestionan manualmente mediante archivos JAR.

Incluye múltiples implementaciones de DAO (Secuencial, Aleatorio, PostgreSQL) e interfaces (Consola, JavaFX con WebView).

## Estructura del proyecto

```
colectivo-base/
├── src/                  # Código fuente Java
├── bin/                  # Clases compiladas
├── lib-JavaFX/           # Librerías JavaFX (agregar manualmente)
├── lib-log4j/            # Log4j JARs (agregar manualmente)
├── lib-postgre/          # PostgreSQL JDBC Driver (agregar manualmente)
├── config.properties     # Configuración principal (modo secuencial)
├── jdbc.properties       # Configuración de base de datos (Postgres)
└── factory.properties    # Selección de implementaciones
```

---

## Dependencias y configuración de librerías

Este proyecto requiere agregar manualmente los JARs necesarios a las carpetas indicadas. Ver la sección detallada más abajo para instrucciones por IDE (Eclipse / IntelliJ / VS Code).

### Librerías clave a incluir

- JavaFX SDK 21+ (`lib-JavaFX/`) — incluir `javafx.controls`, `javafx.fxml`, `javafx.web`, etc.
- Log4j (`lib-log4j/`) — `log4j-api` y `log4j-core`.
- PostgreSQL JDBC (`lib-postgre/`) — driver JDBC compatible.

## Archivos de propiedades (resumen)

1) `factory.properties` — seleccionar implementación de interfaz y DAO (descomentar/ajustar las líneas relevantes).

2) `jdbc.properties` — configuración de conexión a PostgreSQL. IMPORTANTE: descomentar UNA línea `schema=...` para elegir la ciudad (schema) que quieres visualizar. Ejemplo por defecto:

```properties
usr=estudiante
pwd=estudiante
driver=org.postgresql.Driver
url=jdbc:postgresql://pgs.fi.mdn.unp.edu.ar:30000/bd1

# IMPORTANTE: Descomentar el schema de la ciudad que deseas visualizar
schema=colectivo_PM    # Puerto Madryn (por defecto)
#schema=colectivo_AZL  # Azul
#schema=colectivo_CO   # Comodoro Rivadavia
#schema=colectivo_GP   # General Pico
#schema=colectivo_HL   # HonoLulu
#schema=colectivo_TW   # Trelew
```

3) `config.properties` — usado por la versión Secuencial (archivos .txt). Ejemplo:

```properties
# Archivos de datos (modo secuencial)
linea=linea_PM.txt
parada=parada_PM.txt
tramo=tramo_PM.txt
frecuencia=frecuencia_PM.txt

# Interfaz JavaFX
vista=/colectivo/interfaz/view.fxml
estilo=/colectivo/interfaz/style.css

# Internacionalización
language=es
country=ES
```

### Seleccionar ciudad cuando se usa Secuencial (DAO por archivos)

Si en `factory.properties` seleccionas las implementaciones *Secuencial* (lectura desde ficheros), puedes elegir qué ciudad cargar configurando los nombres de archivos en `config.properties`. Ejemplo (Puerto Madryn — PM):

```properties
linea=linea_PM.txt
parada=parada_PM.txt
tramo=tramo_PM.txt
frecuencia=frecuencia_PM.txt
```

Para otra ciudad, sustituye las entradas por los archivos correspondientes (por ejemplo `linea_TW.txt` para Trelew). Asegúrate de que `factory.properties` seleccione las implementaciones secuenciales antes de ejecutar.

Nota: Para `PostgresqlDAO` la selección de ciudad se hace mediante `schema` en `jdbc.properties` (ver arriba).

---

## Ejecutar / Run configuration (Eclipse y otros IDE)

Para ejecutar la aplicación con JavaFX y WebView necesitarás los VM arguments apropiados. Ejemplo (Eclipse / Run Configuration):

```text
--module-path "E:\javafx-sdk-21.0.8\lib" --add-modules javafx.controls,javafx.fxml,javafx.web --add-exports=javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED --add-exports=javafx.web/com.sun.javafx.sg.prism.web=ALL-UNNAMED --add-exports=javafx.web/com.sun.javafx.scene.web=ALL-UNNAMED
```

Adaptar `--module-path` a la ubicación de tu JavaFX SDK. Incluir `javafx.web` es necesario para WebView (mapas).

---

## Uso de la aplicación con mapas

### Requisitos

1. `INTERFAZ=colectivo.interfaz.javafx.InterfazJavaFX` en `factory.properties`.
2. DAO PostgreSQL activo (contiene coordenadas geográficas).
3. Schema correcto seleccionado en `jdbc.properties` (una sola línea sin comentar).
4. Run configuration con los VM arguments completos (incluyendo `javafx.web`).
5. Conexión a internet (para tiles de OpenStreetMap).

### Ciudades disponibles

- **colectivo_PM** — Puerto Madryn (por defecto)
- **colectivo_AZL** — Azul
- **colectivo_CO** — Comodoro Rivadavia
- **colectivo_GP** — General Pico
- **colectivo_HL** — HonoLulu
- **colectivo_TW** — Trelew

---

## Solución de problemas comunes

- Error: "Unknown module: javafx.web"
  - Causa: `javafx.web` no incluido en `--add-modules` o no está en module-path.
  - Solución: Asegurarse que `javafx.web.jar` está en `lib-JavaFX/` y que los VM args incluyen `javafx.web`.

- Error: "IllegalAccessError ... WebViewHelper"
  - Causa: Faltan exportaciones de paquetes internos de JavaFX.
  - Solución: Usar los `--add-exports` mostrados arriba.

- Error: No se conecta a la base de datos
  - Causa: `schema` comentado o configuración errónea en `jdbc.properties`.
  - Solución: Verificar que solo UNA línea `schema=...` esté sin comentar y que credenciales/url sean correctas.

- El mapa no se visualiza
  - Verificar: conexión a internet, `javafx.web` en VM args, schema Postgres seleccionado (los ficheros secuenciales no contienen coordenadas).

---

## Checklist previo a ejecutar

- [ ] Carpetas de librerías creadas (`lib-JavaFX/`, `lib-log4j/`, `lib-postgre/`).
- [ ] JARs agregados al Build Path del IDE.
- [ ] `factory.properties` configurado (INTERFAZ y DAO).
- [ ] `jdbc.properties` con `schema=...` descomentado (para Postgres) o `config.properties` ajustado (para Secuencial).
- [ ] Run Configuration con VM args correctos.

---

## Diferencias con la versión Maven

La carpeta `colectivo-base` es la versión sin Maven (gestión manual de librerías). Si prefieres gestión automática, revisa el proyecto hermano `colectivo-base-maven`.

---

## Recursos

- JavaFX SDK: https://openjfx.io/
- Log4j: https://logging.apache.org/log4j/2.x/
- PostgreSQL JDBC: https://jdbc.postgresql.org/

---

## Licencia

Proyecto con fines educativos — seguir las reglas del curso y la licencia del repositorio.

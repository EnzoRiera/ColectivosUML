# Formato de Archivos Binarios - DAOs de Acceso Aleatorio

**Proyecto**: Sistema de Gestión de Líneas de Colectivo  
**Autor**: Equipo POO-2025  
**Versión**: 2.0  
**Fecha**: 20 de octubre de 2025  

---

## 📋 Tabla de Contenidos

1. [Introducción](#introduccion)
2. [Decisiones de Diseño](#decisiones-de-diseno)
3. [Formato de Parada (ParadaAleatorioDAO)](#formato-de-parada)
4. [Formato de Línea (LineaAleatorioDAO)](#formato-de-linea)
5. [Formato de Tramo (TramoAleatorioDAO)](#formato-de-tramo)
6. [Utilidades y Convenciones](#utilidades-y-convenciones)
7. [Ejemplos de Registros](#ejemplos-de-registros)

---

## 🎯 Introduccion

Este documento describe el formato de almacenamiento binario utilizado por los DAOs de acceso aleatorio (`ParadaAleatorioDAO`, `LineaAleatorioDAO`, y `TramoAleatorioDAO`). Estos archivos permiten acceso directo y modificaciones eficientes de los datos del sistema de transporte.

### Características Generales

- **Tipo de archivo**: Binario (`.dat`)
- **Codificación de caracteres**: UTF-16 (2 bytes por char en Java)
- **Acceso**: Aleatorio mediante `RandomAccessFile`
- **Eliminación**: Lógica mediante flag de eliminación
- **Inicialización**: Poblado automático desde DAOs secuenciales si el archivo está vacío

---

## 🏗️ Decisiones de Diseno

### 1. **Archivo Único vs Múltiples Archivos**

Cada entidad utiliza **un solo archivo binario** que contiene todos sus datos relacionados:

- ✅ **Parada**: Un archivo `parada.dat` con código, nombre, altura y líneas asociadas
- ✅ **Línea**: Un archivo `linea.dat` con código, nombre, paradas y frecuencias
- ✅ **Tramo**: Un archivo `tramo.dat` con paradas de origen/destino y duración

**Ventajas del archivo único:**
- Simplifica la gestión de archivos
- Reduce operaciones de I/O
- Mantiene consistencia atómica en actualizaciones
- Facilita backups y migraciones

### 2. **Registros de Longitud Variable**

Debido a que las entidades tienen relaciones variables (una línea puede tener N paradas, una parada puede tener M líneas), se utiliza formato de **longitud variable**:

```
[Header fijo] + [Cantidad: int] + [Datos variables] + [Cantidad: int] + [Datos variables]
```

### 3. **Flag de Eliminación Lógica**

Todos los registros incluyen un **flag de eliminación** como primer campo:
- `' '` (espacio) = Registro activo
- `'X'` = Registro eliminado lógicamente
 - `FileUtil.DELETED` (actualmente `'*'`) = Registro eliminado lógicamente

**Ventajas:**
- No requiere reorganización del archivo
- Permite recuperación de datos
- Mantiene posiciones de registros estables

## 🚍 Formato de Parada

### Archivo: `parada.dat`

**Estructura del Registro: **

```

```

## 🚍 Formato de Linea

### Archivo: `linea.dat`

**Estructura del Registro (longitud variable, según `LineaAleatorioDAO`):**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ HEADER (Longitud fija para campos de texto)                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ char    deleted           (2 bytes)   - Flag eliminación                    │
│ char[10] codigo           (20 bytes)  - Código línea (string padded)        │
│ char[50] nombre           (100 bytes) - Nombre línea (string padded)        │
├─────────────────────────────────────────────────────────────────────────────┤
│ PARADAS (Longitud Variable)                                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ int     numParadas        (4 bytes)   - Cantidad paradas                    │
│ int     codigoParada[0]   (4 bytes)   - Código parada 1                     │
│ ...                                                                         │
│ int     codigoParada[N-1] (4 bytes)   - Código parada N                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ FRECUENCIAS (Longitud Variable)                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ int     numFrecuencias    (4 bytes)   - Cantidad total de frecuencias       │
│ ┌─ Frecuencia (diaSemana:int + hora:SIZE_HORA) ───────────────────────────┐ │
│ │ int     diaSemana       (4 bytes)                                       │ │
│ │ char[5] hora            (10 bytes)  - hora escrita como 5 chars (UTF-16)│ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Constantes de Tamaño (impl.)

```java
private static final int SIZE_CODIGO = 10;  // caracteres
private static final int SIZE_NOMBRE = 50;  // caracteres
private static final int SIZE_HORA = 5;     // caracteres (formato "HH:mm")
```

### Cálculo de Tamaño del Registro (observación)

Cada carácter Java se escribe como `char` (2 bytes). Por eso, cada campo `char[n]` ocupa `n * 2` bytes en disco. En el DAO las frecuencias se escriben como `file.writeInt(dia)` seguido de `FileUtil.writeString(..., SIZE_HORA)` donde `SIZE_HORA == 5`. Eso significa que cada frecuencia ocupa 4 (int) + (5 × 2) = 14 bytes en el archivo.

Fórmula práctica:

```java
int tamañoRegistro = /* header fijo en bytes */ 2 + (SIZE_CODIGO*2) + (SIZE_NOMBRE*2)
                    + 4 + (4 * numParadas)
                    + 4 + ( (4 + (SIZE_HORA*2)) * numFrecuencias );
```

Ejemplo (calculado según implementación):
- Línea con 25 paradas y 84 frecuencias: headerBytes + 4 + (4*25) + 4 + (14*84) = (compute según headerBytes)

### Formato de Frecuencia (implementación)

Cada frecuencia en disco equivale a:
- `diaSemana` (int, 4 bytes)
- `hora` (SIZE_HORA chars; en Java cada char = 2 bytes, por eso hora ocupa 10 bytes en disco cuando SIZE_HORA==5)

Al leer, el DAO usa `LocalTime.parse(horaStr)` para convertir la cadena en `LocalTime`.

### Notas Especiales

- Las paradas se almacenan como `int` (4 bytes cada una) en la sección de paradas.
- El DAO cuenta el total de frecuencias sumando las horas por día y escribe ese total antes de las entradas de frecuencia.

```
┌──────────────────────────────────────────────────────────────┐
│ int     numFrecuencias    (4 bytes)   - Cantidad total       │
│ ┌─ Frecuencia[0] (14 bytes) ───────────────────────────────┐ │
│ │ int     diaSemana       (4 bytes)   - Día (1=Lun...7=Dom)│ │
│ │ char[5] hora            (10 bytes)  - Hora "HH:mm"       │ │
│ └──────────────────────────────────────────────────────────┘ │
│ ┌─ Frecuencia[1] (14 bytes) ───────────────────────────────┐ │
│ │ int     diaSemana       (4 bytes)                        │ │
│ │ char[5] hora            (10 bytes)                       │ │
│ └──────────────────────────────────────────────────────────┘ │
│ ...                                                          │
│ ┌─ Frecuencia[M-1] (12 bytes) ─────────────────────────────┐ │
│ │ int     diaSemana       (4 bytes)                        │ │
│ │ char[5] hora            (10 bytes)                       │ │
│ └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘

Total: 122 bytes (header) + 4 bytes + (4 × numParadas) + 4 bytes + (14 × numFrecuencias)

```

### Constantes de Tamaño

```java
private static final int SIZE_CODIGO = 10;  // caracteres
private static final int SIZE_NOMBRE = 50;  // caracteres
private static final int SIZE_HORA = 5;     // caracteres (formato "HH:mm")
```

### Cálculo de Tamaño del Registro

```java
int tamañoRegistro = 122 + 4 + (4 * numParadas) + 4 + (12 * numFrecuencias);
```

**Ejemplo:**
- Línea con 25 paradas y 84 frecuencias: 122 + 4 + (4 × 25) + 4 + (12 × 84) = **1,238 bytes**
- Línea con 10 paradas y 20 frecuencias: 122 + 4 + (4 × 10) + 4 + (12 × 20) = **410 bytes**

### Formato de Frecuencia

Cada frecuencia ocupa **12 bytes**:
- `diaSemana` (int, 4 bytes): 1=Lunes, 2=Martes, ..., 7=Domingo
- `hora` (char[5], 10 bytes): Formato "HH:mm" (ejemplo: "08:30", "14:45")

**Nota**: La hora se almacena como String en formato ISO-8601 simplificado. Al leer, se parsea a `LocalTime` usando `LocalTime.parse()`.

### Notas Especiales

- **Todas las frecuencias juntas**: No se separan por día, cada registro incluye el día de la semana
- **Orden**: Las frecuencias se escriben iterando días 1-7 y dentro de cada día, sus horas
- **Códigos de parada**: Se almacenan como `int` (no objetos Parada completos)

---

## 🛣️ Formato de Tramo

### Archivo: `tramo.dat`

**Estructura del Registro (longitud fija según `TramoAleatorioDAO`):**

```
┌────────────────────────────────────────────────────────────────────────────┐
│ REGISTRO (longitud fija calculada en bytes)                                 │
├────────────────────────────────────────────────────────────────────────────┤
│ char    deleted           (2 bytes)   - Flag eliminación (char)            │
│ char[10] codigoOrigen     (20 bytes)  - Código parada origen (string)      │
│ char[10] codigoDestino    (20 bytes)  - Código parada destino (string)     │
│ int     tiempo            (4 bytes)   - Tiempo de viaje (minutos)          │
│ int     tipo              (4 bytes)   - Tipo de tramo (1=colectivo, 2=caminando)
└────────────────────────────────────────────────────────────────────────────┘


Total por registro: 2 + (10*2) + (10*2) + 4 + 4 = 50 bytes

```

### Notas importantes (coincidentes con `TramoAleatorioDAO`)

- En la implementación actual los códigos de parada se almacenan como `String` con padding (`SIZE_CODIGO == 10`) y no como `int`.
- `tiempo` se guarda como `int` y representa minutos.
- `tipo` se guarda como `int` (1 = colectivo, 2 = caminando).
- El Javadoc del DAO indica "Total record size: 49 bytes" pero la suma de campos usando `char` de 2 bytes da **50 bytes** por registro; es recomendable corregir el Javadoc en `TramoAleatorioDAO.java` para reflejar 50 bytes.
---

## 🛠️ Utilidades y Convenciones

### FileUtil - Métodos de Lectura/Escritura

La clase `FileUtil` proporciona métodos estáticos para trabajar con strings en archivos binarios:

```java
public class FileUtil {
    
    /** Flag de eliminación - registro activo */
    public static final char ACTIVE = ' ';
    
    /** Flag de eliminación - registro eliminado */
    public static final char DELETED = 'X';
    
    /**
     * Lee una cantidad específica de caracteres desde el archivo.
     * 
     * @param file archivo RAF abierto
     * @param numChars cantidad de caracteres a leer
     * @return String leído (puede contener padding)
     * @throws IOException si falla la lectura
     */
    public static String readString(RandomAccessFile file, int numChars) 
            throws IOException {
        StringBuilder sb = new StringBuilder(numChars);
        for (int i = 0; i < numChars; i++) {
            sb.append(file.readChar());
        }
        return sb.toString();
    }
    
    /**
     * Escribe un string de longitud específica al archivo.
     * 
     * @param file archivo RAF abierto
     * @param value string a escribir (será truncado o rellenado)
     * @param numChars longitud exacta a escribir
     * @throws IOException si falla la escritura
     */
    public static void writeString(RandomAccessFile file, String value, int numChars) 
            throws IOException {
        for (int i = 0; i < numChars; i++) {
            file.writeChar(i < value.length() ? value.charAt(i) : ' ');
        }
    }
}
```

### Método padOrTrim - Normalización de Strings

Todos los DAOs implementan un método privado `padOrTrim()` para ajustar strings a longitud fija:

```java
/**
 * Ajusta un string a la longitud especificada.
 * - Si es más corto: rellena con espacios a la derecha
 * - Si es más largo: trunca al tamaño máximo
 * - Si es null: trata como string vacío
 * 
 * @param s string a ajustar (puede ser null)
 * @param lengthChars longitud deseada en caracteres
 * @return string de exactamente lengthChars caracteres
 */
private static String padOrTrim(String s, int lengthChars) {
    if (s == null) {
        s = "";
    }
    if (s.length() == lengthChars) {
        return s;
    }
    if (s.length() > lengthChars) {
        return s.substring(0, lengthChars);
    }
    return String.format("%-" + lengthChars + "s", s);
}
```

### Convenciones de Lectura

Al leer campos de texto desde el archivo:

```java
// ✅ CORRECTO - Elimina padding al leer
String codigo = FileUtil.readString(file, SIZE_CODIGO).strip();
String nombre = FileUtil.readString(file, SIZE_NOMBRE).strip();

// ❌ INCORRECTO - Mantiene espacios de padding
String codigo = FileUtil.readString(file, SIZE_CODIGO);
```

### Convenciones de Escritura

Al escribir campos de texto al archivo:

```java
// ✅ CORRECTO - Normaliza antes de escribir
FileUtil.writeString(file, padOrTrim(linea.getCodigo(), SIZE_CODIGO), SIZE_CODIGO);

// ❌ INCORRECTO - Puede escribir longitud incorrecta
FileUtil.writeString(file, linea.getCodigo(), SIZE_CODIGO);
```

---

## 📚 Ejemplos de Registros

### Ejemplo 1: Parada Completa

**Datos:**
- Código: 1001
- Nombre: "Plaza de Mayo"
- Calle: "Av. de Mayo"
- Altura: 1500
- Latitud: -34.6083
- Longitud: -58.3712
- Líneas: ["152", "39", "86"]

**Representación Binaria:**

```
Offset  Tipo        Valor               Bytes
------  ----------  ------------------  -----
0       char        ' '                 2
2       int         1001                4
6       char[50]    "Plaza de Mayo..."  100
106     char[30]    "Av. de Mayo..."    60
166     int         1500                4
170     double      -34.6083            8
178     double      -58.3712            8
186     int         3                   4
190     char[10]    "152       "        20
210     char[10]    "39        "        20
230     char[10]    "86        "        20
                                        ----
                                Total:  250 bytes
```

### Ejemplo 2: Línea con Frecuencias

**Datos:**
- Código: "152"
- Nombre: "Plaza de Mayo - Veterinaria"
- Paradas: [1001, 1002, 1003] (3 paradas)
- Frecuencias:
  - Lunes 06:00, 06:30, 07:00
  - Martes 06:00, 06:30

**Representación Binaria:**

```
Offset  Tipo        Valor                       Bytes
------  ----------  --------------------------  -----
0       char        ' '                         2
2       char[10]    "152       "                20
22      char[50]    "Plaza de Mayo - Vet..."    100
122     int         3                           4
126     int         1001                        4
130     int         1002                        4
134     int         1003                        4
138     int         5                           4
142     int         1                           4
146     char[5]     "06:00"                     10
156     int         1                           4
160     char[5]     "06:30"                     10
170     int         1                           4
174     char[5]     "07:00"                     10
184     int         2                           4
188     char[5]     "06:00"                     10
198     int         2                           4
202     char[5]     "06:30"                     10
                                                ----
                                        Total:  212 bytes
```

### Ejemplo 3: Tramo Simple

**Datos:**
- Origen: 1001
- Destino: 1002
- Duración: 300 segundos (5 minutos)
- Distancia: 1.5 km

**Representación Binaria:**

```
Offset  Tipo        Valor       Bytes
------  ----------  ----------  -----
0       char        ' '         2
2       int         1001        4
6       int         1002        4
10      int         300         4
14      double      1.5         8
                                ----
                        Total:  22 bytes
```

### Ejemplo 4: Registro Eliminado (Parada)

**Datos:**
- Código: 2050 (eliminada lógicamente)

**Representación Binaria:**

```
Offset  Tipo        Valor       Notas
------  ----------  ----------  ---------------------------
0       char        'X'         ← Flag de eliminación
2       int         2050        Datos preservados
6       char[50]    "..."       
...     ...         ...         El resto se lee pero se descarta
```

**Comportamiento:**
- El método `readRecord()` lee el flag
- Si es `'X'`, lee el resto del registro para avanzar el file pointer
- Retorna `null` (registro ignorado)
- No se agrega al mapa en memoria

---

## 🔍 Patrones de Lectura y Escritura

### Patrón de Lectura General

```java
private Entidad readRecord() throws IOException {
    // 1. Leer flag de eliminación
    char deleted = file.readChar();
    
    // 2. Leer campos del header (longitud fija)
    TipoClave clave = leerClave();
    String campo1 = FileUtil.readString(file, SIZE_1).strip();
    // ...
    
    // 3. Verificar si está eliminado o es inválido
    if (deleted == FileUtil.DELETED || claveInvalida(clave)) {
        // Leer resto del registro para avanzar file pointer
        leerYDescartarRestoDatos();
        return null;
    }
    
    // 4. Crear entidad
    Entidad entidad = new Entidad(clave, campo1, ...);
    
    // 5. Leer colecciones variables
    int numItems = file.readInt();
    for (int i = 0; i < numItems; i++) {
        ItemRelacionado item = leerItem();
        if (itemValido(item)) {
            entidad.agregarItem(item);
        }
    }
    
    // 6. Retornar entidad construida
    return entidad;
}
```

### Patrón de Escritura General

```java
private void writeRecord(Entidad entidad) throws IOException {
    // 1. Validar
    if (entidad == null) {
        throw new IllegalArgumentException("Entidad no puede ser null");
    }
    
    // 2. Escribir header
    file.writeChar(' '); // activo
    escribirClave(entidad.getClave());
    FileUtil.writeString(file, padOrTrim(entidad.getCampo1(), SIZE_1), SIZE_1);
    // ...
    
    // 3. Escribir colecciones variables
    Collection items = entidad.getItems();
    file.writeInt(items.size());
    for (Item item : items) {
        escribirItem(item);
    }
}
```

### Patrón de Carga Inicial

```java
private void cargarEntidadesDesdeArchivo() throws IOException {
    file.seek(0L);
    
    while (file.getFilePointer() < file.length()) {
        try {
            Entidad entidad = readRecord();
            if (entidad != null) {  // null si está eliminado
                mapa.put(entidad.getClave(), entidad);
            }
        } catch (EOFException e) {
            // Fin de archivo alcanzado naturalmente
            return;
        }
    }
}
```

### Patrón de Población desde Secuencial

```java
private void populateFromSequential() {
    try {
        // 1. Obtener dependencias
        DependenciaDAO dependenciaDAO = Factory.getInstance("DEPENDENCIA", DependenciaDAO.class);
        if (dependenciaDAO == null) {
            System.err.println("Error: DependenciaDAO no disponible");
            return;
        }
        
        // 2. Obtener DAO secuencial
        EntidadSecuencialDAO secDAO = new EntidadSecuencialDAO(dependenciaDAO);
        Map<Clave, Entidad> secEntidades = secDAO.buscarTodos();
        
        if (secEntidades == null || secEntidades.isEmpty()) {
            return;
        }
        
        // 3. Escribir cada entidad al final del archivo
        for (Entidad entidad : secEntidades.values()) {
            file.seek(file.length());
            writeRecord(entidad);
            mapa.put(entidad.getClave(), entidad);
        }
        
    } catch (Exception e) {
        System.err.println("Error poblando archivo aleatorio: " + e.getMessage());
    }
}
```

---

## ⚡ Optimizaciones y Consideraciones

### 1. Carga en Memoria

**Estrategia actual:**
- Todos los registros activos se cargan en memoria al inicializar el DAO
- Se usa `TreeMap` para orden automático por clave
- `buscarTodos()` retorna copia defensiva: `new TreeMap<>(mapa)`

**Ventajas:**
- Acceso O(log n) para búsquedas
- No requiere lectura de archivo en cada consulta
- Ideal para conjuntos de datos pequeños-medianos (< 100,000 registros)

**Desventajas:**
- Consumo de memoria proporcional al tamaño del dataset
- Tiempo de inicialización proporcional al tamaño del archivo

### 2. Registros de Longitud Variable

**Implicaciones:**
- **No se puede calcular offset directo** a un registro específico
- Requiere **lectura secuencial** para encontrar un registro por posición
- **Actualización in-place** es compleja si cambia el tamaño del registro

**Alternativas consideradas (no implementadas):**
1. **Índice separado**: Archivo `.idx` con offsets de cada registro
2. **Longitud fija máxima**: Desperdiciar espacio pero permitir acceso directo
3. **Estructura B-Tree**: Para archivos muy grandes

### 3. Eliminación Lógica vs Física

**Eliminación Lógica (implementada):**
- Marca registro con flag 'X'
- Mantiene registro en archivo
- Permite recuperación de datos

**Eliminación Física (no implementada):**
- Reorganiza archivo eliminando registro
- Reduce tamaño del archivo
- Requiere reescritura completa

**Recomendación:** Implementar proceso de "compactación" periódico que elimine físicamente registros marcados.

### 4. Sincronización y Concurrencia

**Estado actual:**
- No hay sincronización thread-safe
- Asume uso single-threaded

**Mejoras sugeridas:**
```java
// Para uso multi-threaded
private final Map<Clave, Entidad> mapa = 
    Collections.synchronizedMap(new TreeMap<>());

// Para operaciones de archivo
private final Object fileLock = new Object();

public void writeRecord(Entidad e) throws IOException {
    synchronized(fileLock) {
        file.seek(file.length());
        // ... escritura ...
    }
}
```

---

## 📊 Comparación de Tamaños

### Tabla Comparativa
```

| Entidad    | Header Fijo | Parte Variable      | Ejemplo Real |
|------------|-------------|------------------------------------------------|------------------------------------|
| **Parada** | 172 bytes   | 4 + (20 × N líneas)                            | 25 líneas = 672 bytes              |
| **Línea**  | 122 bytes   | 4 + (4 × N paradas) + 4 + (12 × M frecuencias) | 30 paradas + 84 freq = 1,254 bytes |
| **Tramo**  | 22 bytes    | 0 (fijo)                                       |  **22 bytes**                      |

### Estimación de Tamaño de Archivo

**Ejemplo: Sistema de 500 paradas, 50 líneas**

```
Paradas:
  500 paradas × (172 + 4 + 20×5) bytes promedio
  = 500 × 272 bytes
  = 136,000 bytes ≈ 133 KB

Líneas:
  50 líneas × (122 + 4 + 4×25 + 4 + 12×60) bytes promedio
  = 50 × 850 bytes
  = 42,500 bytes ≈ 41.5 KB

Tramos:
  500 paradas × 4 conexiones promedio × 22 bytes
  = 2,000 × 22 bytes
  = 44,000 bytes ≈ 43 KB

TOTAL: 133 + 41.5 + 43 ≈ 217.5 KB
```

---

## 🔧 Herramientas de Diagnóstico

### Verificador de Integridad de Archivo

```java
public class BinaryFileInspector {
    
    public static void inspectParadaFile(String filePath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            System.out.println("=== Inspección de parada.dat ===");
            System.out.println("Tamaño total: " + file.length() + " bytes");
            
            int recordCount = 0;
            int deletedCount = 0;
            
            while (file.getFilePointer() < file.length()) {
                long startPos = file.getFilePointer();
                char deleted = file.readChar();
                int codigo = file.readInt();
                String nombre = FileUtil.readString(file, 50).strip();
                
                // Skip resto del header
                FileUtil.readString(file, 30); // calle
                file.readInt(); // altura
                file.readDouble(); // latitud
                file.readDouble(); // longitud
                
                int numLineas = file.readInt();
                for (int i = 0; i < numLineas; i++) {
                    FileUtil.readString(file, 10);
                }
                
                long endPos = file.getFilePointer();
                int recordSize = (int)(endPos - startPos);
                
                recordCount++;
                if (deleted == 'X') {
                    deletedCount++;
                    System.out.println("  [DELETED] Registro #" + recordCount 
                        + " - Código: " + codigo + " - Tamaño: " + recordSize);
                } else {
                    System.out.println("  [ACTIVE] Registro #" + recordCount 
                        + " - Código: " + codigo + " - Nombre: " + nombre 
                        + " - Líneas: " + numLineas + " - Tamaño: " + recordSize);
                }
            }
            
            System.out.println("\nResumen:");
            System.out.println("  Total registros: " + recordCount);
            System.out.println("  Activos: " + (recordCount - deletedCount));
            System.out.println("  Eliminados: " + deletedCount);
        }
    }
}
```

---

## 📚 Referencias

### Documentos Relacionados

- **Especificación del Modelo**: `UML-Modelo-intellij.png`
- **Patrones de Diseño**: `patrones-diseno-md.md`
- **Alcance del Proyecto**: `Documento_de_alcance.md`

### Clases Relevantes

- `colectivo.util.FileUtil` - Utilidades para I/O de strings
- `colectivo.conexion.AConnection` - Factory de conexiones RAF
- `colectivo.util.Factory` - Factory de DAOs
- `colectivo.dao.aleatorio.*` - Implementaciones de DAOs
- `colectivo.dao.secuencial.*` - DAOs secuenciales para población inicial

### Estándares de Código

- Instrucciones de cátedra: `buenas-practicas-POO.instructions.md`
- Convenciones Java: `.github/prompts/java.instructions.md`
- Documentación JavaDoc: `.github/prompts/java-docs.prompt.md`

---

## ✅ Checklist de Implementación

Al implementar un nuevo DAO de acceso aleatorio:

- [ ] Definir constantes de tamaño (`SIZE_CAMPO`)
- [ ] Implementar método `padOrTrim()` privado
- [ ] Diseñar formato binario (documentar en este archivo)
- [ ] Implementar `readRecord()` con manejo de `EOFException`
- [ ] Implementar `writeRecord()` con validaciones
- [ ] Implementar `cargarDesdeArchivo()` con carga completa
- [ ] Implementar `populateFromSequential()` para inicialización
- [ ] Cargar dependencias vía `Factory.getInstance()`
- [ ] Usar `TreeMap` para mantener orden
- [ ] Retornar copia defensiva en `buscarTodos()`
- [ ] Implementar flag de eliminación lógica
- [ ] Documentar JavaDoc completo
- [ ] Crear tests unitarios
- [ ] Actualizar este documento con el nuevo formato

---

**Fin del Documento**

*Este documento es un documento vivo y debe actualizarse con cada cambio en los formatos binarios.*

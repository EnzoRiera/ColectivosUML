# Patrones de Diseño en colectivo-base

**Proyecto:** Sistema de Gestión de Líneas de Colectivo  
**Versión:** 2.0  
**Fecha:** 20 de Octubre de 2025  
**Autor:** Equipo POO-2025  

---

## 📋 Resumen Ejecutivo

Este documento describe los patrones de diseño y arquitectónicos implementados en el proyecto **colectivo-base**. El sistema está organizado en capas bien definidas que separan responsabilidades entre:

- **Persistencia** (DAO con múltiples implementaciones)
- **Lógica de Negocio** (Services y Strategy)
- **Coordinación** (Coordinator)
- **Presentación** (Interfaz con múltiples implementaciones)

La arquitectura soporta configuración dinámica mediante reflexión, múltiples estrategias de cálculo de rutas, y diferentes interfaces de usuario sin modificar el código de negocio.

---

## 🎯 Patrones Implementados

### 1. DAO (Data Access Object)

**Propósito:** Abstraer y encapsular el acceso a datos, separándolo completamente de la lógica de negocio.

**Estructura:**

```
dao/
├── LineaDAO.java          (Interface)
├── ParadaDAO.java         (Interface)
├── TramoDAO.java          (Interface)
├── secuencial/
│   ├── LineaSecuencialDAO.java
│   ├── ParadaSecuencialDAO.java
│   └── TramoSecuencialDAO.java
└── aleatorio/
    ├── LineaAleatorioDAO.java
    ├── ParadaAleatorioDAO.java
    └── TramoAleatorioDAO.java
```

**Interfaces:**

```java
public interface ParadaDAO {
    Map<Integer, Parada> buscarTodos() throws Exception;
}

public interface LineaDAO {
    Map<String, Linea> buscarTodos() throws Exception;
}

public interface TramoDAO {
    Map<String, Tramo> buscarTodos() throws Exception;
}
```

**Implementaciones:**

- **Secuencial**: Lee archivos de texto delimitados (`linea_PM.txt`, `parada_PM.txt`, `tramo_PM.txt`, `frecuencia_PM.txt` )
- **Aleatorio**: Gestiona archivos binarios de acceso directo (`linea.dat`, `parada.dat`, `tramo.dat`)
- **Base de Datos**: Obtiene los datos de una base de datos PostgreSQL.

**Beneficios:**

✅ Cambiar fuente de datos sin tocar lógica de negocio  
✅ Facilita testing con implementaciones mock  
✅ Cumple con el Principio de Inversión de Dependencias (SOLID)  
✅ Permite múltiples estrategias de persistencia simultáneas  

**Ejemplo de uso:**

```java
ParadaDAO paradaDAO = Factory.getInstance("PARADA", ParadaDAO.class);
Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
```

---

### 2. Abstract Factory + Registry (con Reflection)

**Propósito:** Crear instancias de componentes dinámicamente mediante configuración externa y cachear instancias compartidas.

**Archivo clave:** `conexion/Factory.java`

**Implementación:**

```java

public final class Factory {

    private static final Map<String, Object> INSTANCES = new ConcurrentHashMap<>();

    private Factory() { /* utility class */ }

    public static Object getInstance(String name) {
        return INSTANCES.computeIfAbsent(name, Factory::createInstance);
    }
    
    public static <T> T getInstance(String name, Class<T> type) {
        Object obj = getInstance(name);
        return type.cast(obj);
    }

    private static Object createInstance(String name) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("factory");
            String className = rb.getString(name);
            return Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance for: " + name, e);
        }
    }
}
```

**Archivos de configuración:**

**`factory.properties`:**
- Se deja sin Comentar la implementacion a utilizar, por ejemplo:

```properties
LINEA = colectivo.dao.aleatorio.LineaAleatorioDAO
PARADA = colectivo.dao.aleatorio.ParadaAleatorioDAO
TRAMO = colectivo.dao.aleatorio.TramoAleatorioDAO
INTERFAZ = colectivo.interfaz.consola.InterfazConsola
```

**`secuencial.properties`:**
```properties
linea = linea_PM.txt
parada = parada_PM.txt
tramo = tramo_PM.txt
frecuencia = frecuencia_PM.txt
```

**`aleatorio.properties`:**
```properties
linea = linea.dat
parada = parada.dat
tramo = tramo.dat
```

**Comportamiento:**

1. **Primera llamada** → Crea instancia usando reflexión desde `factory.properties`
2. **Llamadas posteriores** → Devuelve instancia cacheada (Singleton por tipo)
3. **Thread-safety** → Usa `ConcurrentHashMap` para acceso concurrente seguro

**Ventajas:**

✅ Configuración externa sin recompilar  
✅ Instancias compartidas (ahorro de memoria)  
✅ Thread-safe por diseño  
✅ Fácil cambio entre implementaciones (secuencial ↔ aleatorio ↔ base de datos)  

---

### 3. Singleton

**Propósito:** Garantizar una única instancia compartida del repositorio de datos en memoria.

#### 3.1. Ciudad Singleton (Repository)

**Archivo:** `logica/Ciudad.java`

```java
public class Coordinador {
    public class Ciudad { private static Ciudad ciudad;

private Map<Integer, Parada> paradas;
private Map<String, Linea> lineas;
private Map<String, Tramo> tramos;

private Ciudad() throws Exception {
    cargarDatos();
}

public static Ciudad getCiudad() throws Exception {
    if (ciudad == null) {
        ciudad = new Ciudad();
    }
    return ciudad;
}

private void cargarDatos() throws Exception {
    ParadaService paradaService = new ParadaServiceImpl();
    LineaService lineaService = new LineaServiceImpl();
    TramoService tramoService = new TramoServiceImpl();
    
    this.paradas = paradaService.buscarTodos();
    this.lineas = lineaService.buscarTodos();
    this.tramos = tramoService.buscarTodos();
}

// Getters para acceso a colecciones
public Map<Integer, Parada> getParadas() { return paradas; }
public Map<String, Linea> getLineas() { return lineas; }
public Map<String, Tramo> getTramos() { return tramos; }

}
```
**Responsabilidades:**

- Cargar todas las entidades una sola vez al inicio
- Mantener colecciones en memoria para acceso rápido
- Garantizar consistencia entre referencias bidireccionales
- Proveer acceso centralizado a los datos

**Justificación del Singleton:**

✅ **Integridad referencial**: Todas las entidades comparten las mismas instancias  
✅ **Eficiencia**: Se carga una sola vez, se usa en toda la aplicación  
✅ **Consistencia**: Un solo punto de verdad para el estado del sistema  
✅ **Thread-safety**: Inicialización lazy con double-check (agregar si es necesario)  

#### 3.2. DAO Registry (vía Factory)

El Factory implementa un patrón **Multiton/Registry** donde cada clave lógica tiene su instancia compartida:

- `"PARADA"` → Una única instancia de `ParadaAleatorioDAO`
- `"LINEA"` → Una única instancia de `LineaAleatorioDAO`
- `"TRAMO"` → Una única instancia de `TramoAleatorioDAO`

**Beneficio crítico:** Garantiza que todas las referencias a `Parada`, `Linea` y `Tramo` apunten a los mismos objetos en memoria a través del repositorio `Ciudad`.

#### 3.3. Coordinador NO es Singleton

**IMPORTANTE:** `Coordinador` **NO** es un Singleton. Su ciclo de vida está gestionado por `InicializadorSistema`:

```java
public class InicializadorSistema {
	public void iniciar() throws Exception {
	    Ciudad ciudad = Ciudad.getCiudad();  // ✅ Singleton
	    Calculo calculo = new Calculo();
	    Interfaz interfaz = Factory.getInstance("INTERFAZ", Interfaz.class);
	    
	    coordinador = new Coordinador();  // ❌ NO Singleton
	    coordinador.setCiudad(ciudad);
	    coordinador.setCalculo(calculo);
	    coordinador.setInterfaz(interfaz);
	    
	    calculo.setCoordinador(coordinador);
	    interfaz.setCoordinador(coordinador);
	}
	
	public Coordinador getCoordinador() {
	    return coordinador;
	}

}
```
**Razones para NO usar Singleton en Coordinador:**

❌ No tiene estado compartido que requiera ser único  
❌ Su propósito es coordinar, no almacenar datos  
✅ Mejor testabilidad (se pueden crear múltiples instancias para tests)  
✅ Ciclo de vida claro (se crea en `iniciar()`, se usa, se descarta)  
✅ Evita acoplamiento global innecesario  
---

### 4. Service Layer (Facade)

**Propósito:** Proveer una API de alto nivel que encapsula la complejidad de los DAOs y aplica lógica de negocio.

**Estructura:**

```
servicio/
├── LineaService.java          (Interface)
├── ParadaService.java         (Interface)
├── TramoService.java          (Interface)
├── LineaServiceImpl.java
├── ParadaServiceImpl.java
└── TramoServiceImpl.java
```

**Ejemplo de implementación:**

```java
public class ParadaServiceImpl implements ParadaService {
    private final ParadaDAO paradaDAO;

    public ParadaServiceImpl() {
        this.paradaDAO = Factory.getInstance("PARADA", ParadaDAO.class);
    }

    @Override
    public Map<Integer, Parada> buscarTodos() throws Exception {
        return paradaDAO.buscarTodos();
    }
}
```

**Responsabilidades:**

- Orquestación de múltiples DAOs
- Validación de reglas de negocio
- Transformación de datos entre capas
- Manejo centralizado de excepciones

**Beneficios:**

✅ Desacopla la capa de presentación de la persistencia  
✅ Punto único para agregar lógica transversal (logging, seguridad, caché)  
✅ Facilita testing unitario  

---

### 5. MVC (Model-View-Controller)

**Propósito:** Separar responsabilidades entre modelo de datos, presentación y lógica de control.

**Componentes:**

** Modelo:**

		1.	Responsabilidad: Entidades de dominio.
		
		2.	Archivos: modelo/Linea.java, modelo/Parada.java, modelo/Tramo.java, modelo/Recorrido.java

** Vista:**

	1. Responsabilidad: Interacción con el usuario.
	
	2. Archivos: interfaz/Interfaz.java, interfaz/InterfazConsola.java, interfaz/Controller.java

** Controlador: **

	1.	Responsabilidad: Coordinación y flujo de la aplicación.
	
	2.	Archivos: aplicacion/Coordinador.java


**Flujo de comunicación:**

```
┌─────────────────────┐
│ AplicacionConsultas │  (Entry Point)
└──────────┬──────────┘
           │
           ▼
    ┌──────────────┐
    │ Coordinador  │  (Controller)
    └──┬───────┬───┘
       │       │
       ▼       ▼
┌──────────┐  ┌─────────┐
│ Interfaz │  │ Calculo │
│  (View)  │  │(Business)│
└──────────┘  └────┬────┘
                   │
                   ▼
              ┌────────┐
              │ Ciudad │ (Repository)
              └───┬────┘
                  │
        ┌─────────┼─────────┬────────┐
        ▼         ▼         ▼        ▼
    ┌────────┬────────┬───────────┬────────┐
    │ Linea  │ Parada │ Recorrido │ Tramo  │  (Model)
    └────────┴────────┴───────────┴────────┘
```

**Ventajas de esta arquitectura:**

✅ Clara separación de responsabilidades  
✅ Fácil mantenimiento y extensibilidad  
✅ Testeable en cada capa independientemente  
✅ Múltiples vistas sin modificar el modelo  

---

### 6. Strategy (Algoritmos Intercambiables)

**Propósito:** Permitir diferentes estrategias de búsqueda de rutas sin modificar el código cliente.

**Estructura:**

```
logica/
├── EstrategiaBusqueda.java          	(Interface)
├── BusquedaDirecta.java          	(Strategy 1)
├── BusquedaConTransbordo.java          (Strategy 2)
└── BusquedaCaminando.java           	(Strategy 3)
```

**Interface común:**

```java
public interface EstrategiaBusqueda {

    List<List<Recorrido>> buscar(Parada origen, Parada destino, int diaSemana, LocalTime hora,
                                 Map<Parada, List<Tramo>> conexionesParadas,
                                 Map<String, Tramo> todosLosTramos);
}
```

**Implementaciones:**

#### Strategy 1: Rutas Directas
```java
public class BusquedaDirecta implements EstrategiaBusqueda {
    @Override
    public List<List<Recorrido>> buscar(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime hora,
    									Map<Parada, List<Tramo>> conexionesParadas, Map<String, Tramo> todosLosTramos) {
        // Busca líneas que conecten directamente origen y destino
        // sin transbordos ni caminata
    }
}
```

#### Strategy 2: Con Transbordo
```java
public class BusquedaConTransbordo implements EstrategiaBusqueda {

    @Override
    public List<List<Recorrido>> buscar(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime hora, 
    									Map<Parada, List<Tramo>> conexionesParadas, Map<String, Tramo> todosLosTramos) {
        // Permite cambio de línea en paradas intermedias
    }
}
```

#### Strategy 3: Con Caminata
```java
public class BusquedaCaminando implements EstrategiaBusqueda {
    @Override
    public List<List<Recorrido>> buscar(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime hora,
                                        Map<Parada, List<Tramo>> conexionesParadas, Map<String, Tramo> todosLosTramos) {
        // Permite caminar entre paradas cercanas
        // Usa información de tramos tipo CAMINANDO
    }
}
```

**Ventajas:**

✅ Agregar nuevas estrategias sin modificar código existente (Open/Closed)  
✅ Cada estrategia es independiente y testeable  
✅ Fácil combinación de múltiples estrategias  
✅ Permitiria ejecutar estrategias en paralelo (threads)  

---

### 7. Repository (In-Memory) + Singleton

**Propósito:** Centralizar el acceso a colecciones de entidades de dominio en memoria como instancia única compartida.

**Archivo:** `logica/Ciudad.java`

**Implementación:**

- Ver [### 3. Singleton]

**Responsabilidades:**

- Cargar todas las entidades al inicio de la aplicación
- Mantener colecciones en memoria para acceso rápido
- Garantizar consistencia entre referencias bidireccionales
- Proveer acceso centralizado a los datos

**Ventajas:**

✅ Acceso O(1) a entidades por clave  
✅ Elimina lecturas repetidas del disco  
✅ Simplifica algoritmos de búsqueda de rutas  
✅ Mantiene integridad referencial (mismas instancias)  

**Combinación con Singleton:**

`Ciudad` implementa tanto el patrón **Repository** como **Singleton** porque:

1. **Repository**: Encapsula el acceso a colecciones de entidades
2. **Singleton**: Garantiza que todos los componentes usen las mismas instancias en memoria

Esta combinación es común en arquitecturas donde:
- El repositorio carga datos una sola vez
- Los datos en memoria representan el estado actual del sistema
- Se requiere consistencia global entre componentes


---

### 8. Utility / Helper

**Propósito:** Encapsular lógica reutilizable, operaciones comunes y objetos de valor.

**Componentes:**

#### 8.1. Clase Inmutable de Tiempo

**Archivo:** `util/Tiempo.java`

**Creado por: ** Catedra

```java
 public static LocalTime segundosATiempo(int totalSegundos)

```

#### 8.2. Operaciones Busquedas Transbordos y Creacion de Recorridos

**Archivo:** `logica/CalculosAuxiliares.java`

**Creado por: ** Equipo Desarrollo

**Propósito:** Métodos Compartidos entre las distintas Estrategias de busqueda de recorridos.

```java
public class CalculosAuxiliares {
    public static Recorrido crearRecorridoColectivo(...){}
    
    public static void asignarHorasSalida(...){}
    
    private static int calcularDuracion(...){}
    
    private LocalTime obtenerProximaHoraSalida(...){}
    
    public static Map<Parada, List<Tramo>> conexionesParadas(...){}

}
```

#### 8.3. Carga de Configuración y Datos Primer Incremento

**Archivo:** `datos/CargarParametros.java`

**Creado por: ** Catedra

**Propósito:** Carga de parametros desde config.properties (implementacion del primer incremento) -> sigue funcionando en TESTS

```java
public class CargarParametros {
	public static void parametros() throws IOException {}
	
	public static String getArchivoLinea() {}
	
	public static String getArchivoParada() {}

	public static String getArchivoTramo() {}

	public static String getArchivoFrecuencia() {}
}
```

**Archivo:** `datos/CargarDatos.java`

**Creado por: ** Equipo de Desarrollo

**Propósito:** Carga de datos primer incremento, previo a la carga usando DAO, cuenta con App para verificar carga en colectivo.test , y Pruebas Unitarias.


```java
public class CargarDatos {
	public static Map<Integer, Parada> cargarParadas(String nombreArchivo) throws IOException {}
	
	public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas)
		throws FileNotFoundException {}
			
	public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia,
			Map<Integer, Parada> paradas) throws FileNotFoundException {}
			
	private static void cargarFrecuencias(String nombreArchivoFrecuencia, Map<String, Linea> lineasCargadas)
			throws FileNotFoundException {}
}
```

#### 8.4. Utilidades de Archivo

**Archivo:** `util/FileUtil.java`

**Creado por: ** Catedra

**Propósito:** Clase de utilidad provista por la catedra en el ejemplo subte , reutilizada para carga acceso aleatorio.

```java
public class FileUtil {

	public static final char DELETED = '*';
	public static final int SIZE_DATE = Integer.BYTES * 3;
	public static final int SIZE_DATE1 = Integer.BYTES * 3;
    
	public static String readString(RandomAccessFile file, int length) throws IOException {}
    
	public static void writeString(RandomAccessFile file, String s, int length) throws IOException {}
	
	public static LocalDate readDate(RandomAccessFile file) throws IOException {}

	public static void writeDate(RandomAccessFile file, LocalDate date) throws IOException {}

	public static void copyFile(String nameSource, String nameDest) throws IOException {}
}
```

**Ventajas:**

✅ Evita duplicación de código  
✅ Centraliza operaciones comunes  
✅ Facilita mantenimiento  
✅ Mejora legibilidad del código de negocio  

---

### 9. Inicialización Parametrizable + Interfaz Desacoplada

**Propósito:** Permitir que el sistema se inicialice dinámicamente y soporte múltiples interfaces de usuario (Consola, JavaFX, Web) sin modificar la lógica de negocio.

Este patrón combina varios conceptos para lograr máxima flexibilidad:

#### 9.1. Componentes del Patrón

**Inicializador Centralizado:**

**Archivo:** `aplicacion/InicializadorSistema.java`

```java
public class InicializadorSistema {

    private Coordinador coordinador;

    public void iniciar() throws Exception {
        // Instantiate components
        Ciudad ciudad = Ciudad.getCiudad(); // Singleton
        Calculo calculo = new Calculo();
        Interfaz interfaz = Factory.getInstance("INTERFAZ", Interfaz.class); // desde interfaz.properties

        // Configure relationships
        coordinador = new Coordinador();
        coordinador.setCiudad(ciudad);
        coordinador.setCalculo(calculo);
        coordinador.setInterfaz(interfaz);

        calculo.setCoordinador(coordinador);
        interfaz.setCoordinador(coordinador);

    }

    public Coordinador getCoordinador() {
        return coordinador;
    }
}
```

**Interfaz Desacoplada:**

**Archivo:** `interfaz/Interfaz.java`

```java
public interface Interfaz {
    void iniciarDatos();
    void mostrarResultados();
}
```

**Implementación para Consola:**

**Archivo:** `interfaz/InterfazConsola.java`

```java
public class InterfazConsola implements Interfaz {

    private static Scanner scanner = new Scanner(System.in);
    
    private static String formatoHorario(LocalTime hora) {
        if (hora.getSecond() == 0) {
            return hora.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return hora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    private Coordinador coordinador = null;
    
    public InterfazConsola() {
        System.out.println("InterfazConsola instanciada correctamente.");
    }
    
}
```

**Implementación para JavaFX:**

**Archivo:** `interfaz/InterfazJavaFX.java`

```java
public class InterfazJavaFX implements Interfaz {

}
```

#### 9.2. Configuración Externa

**Archivo:** `factory.properties`

```properties
# Selección de interfaz (cambiar sin recompilar)
INTERFAZ = colectivo.interfaz.consola.InterfazConsola
# INTERFAZ = colectivo.interfaz.InterfazJavaFX

# Selección de DAOs
LINEA = colectivo.dao.aleatorio.LineaAleatorioDAO
PARADA = colectivo.dao.aleatorio.ParadaAleatorioDAO
TRAMO = colectivo.dao.aleatorio.TramoAleatorioDAO
```

#### 9.3. Punto de Entrada

**Archivo:** `aplicacion/AplicacionConsultas.java`

```java
public class AplicacionConsultas {

	private Coordinador coordinador;
	
	public static void main(String[] args) throws IOException {
		AplicacionConsultas miAplicacion = new AplicacionConsultas();
		try {
			InicializadorSistema init = new InicializadorSistema();
			init.iniciar();
			miAplicacion.coordinador = init.getCoordinador();

			coordinador.iniciarConsulta();
		} catch (Exception e) {
			System.err.print("Error inesperado: " + e.getMessage());
			System.exit(-1);
		}
	}
```

#### 9.4. Flujo de Ejecución

```
1. main() → AplicacionConsultas
              ↓
2. Coordinador.getCoordinador()
              ↓
3. Factory lee factory.properties
              ↓
4. Crea instancia de InterfazConsola o InterfazJavaFX
              ↓
5. Inicializa Ciudad (carga datos desde DAOs)
              ↓
6. Crea Calculo con Ciudad
              ↓
7. coordinador.iniciarConsulta()
              ↓
8. interfaz.iniciarDatos()
              ↓
9. Usuario interactúa con la interfaz
              ↓
10. Interfaz llama a calculo.calcularRutas()
              ↓
11. Calculo ejecuta estrategias en paralelo
              ↓
12. Interfaz muestra resultados
```

#### 9.5. Ventajas del Patrón

✅ **Configuración dinámica**: Cambiar interfaz sin recompilar  
✅ **Desacoplamiento total**: Lógica de negocio independiente de UI  
✅ **Múltiples interfaces**: Consola, JavaFX, Web, API REST  
✅ **Testeable**: Mock de Interfaz para testing automatizado  
✅ **Extensible**: Agregar nueva interfaz implementando `Interfaz`  
✅ **Thread-safe**: Cálculos en hilos separados no bloquean UI  
✅ **Inicialización centralizada**: Un solo punto de configuración  

#### 9.6. Extensión Futura: Patrón Observer

Para mejorar la reactividad, se puede integrar el patrón Observer:

---

## 📊 Diagrama de Arquitectura Completo

```
┌──────────────────────────────────────────────────────────────┐
│                    AplicacionConsultas                       │
│                    (Entry Point - main)                      │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Coordinador   │ ◄──── Managed by InicializadorSistema
                    │ (Controller)  │       (NOT Singleton)
                    └───┬───────┬───┘
                        │       │
        ┌───────────────┘       └───────────────┐
        ▼                                        ▼
┌───────────────┐                        ┌──────────────┐
│   Interfaz    │◄──── Factory ────────► │   Calculo    │
│    (View)     │      (Config)          │  (Business)  │
└───┬───────┬───┘                        └──────┬───────┘
    │       │                                    │
    │       │                            ┌───────┴────────┐
    │       │                            │                │
    │       │                            ▼                ▼
    │       │                    ┌──────────────┐  ┌─────────────┐
    │       │                    │BuscadorRutas │  │   Ciudad    │
    │       │                    │  (Strategy)  │  │(Repository) │
    │       │                    └──────────────┘  └──────┬──────┘
    │       │                                              │
    │       │                              ┌───────────────┼───────────────┐
    │       │                              │               │               │
    │       │                              ▼               ▼               ▼
    │       │                     ┌────────────┐  ┌────────────┐  ┌────────────┐
    │       │                     │   Linea    │  │   Parada   │  │   Tramo    │
    │       │                     │  Service   │  │  Service   │  │  Service   │
    │       │                     │  (Facade)  │  │  (Facade)  │  │  (Facade)  │
    │       │                     └─────┬──────┘  └─────┬──────┘  └─────┬──────┘
    │       │                           │               │               │
    ▼       ▼                           ▼               ▼               ▼
┌─────────────────┐           ┌─────────────────────────────────────────────┐
│ InterfazConsola │           │              Factory.java                   │
│ InterfazJavaFX  │◄──────────┤    (Abstract Factory + Registry)            │
└─────────────────┘           │    - ConcurrentHashMap<String, Object>     │
                              │    - Reflection-based instantiation         │
                              └──────────────┬──────────────────────────────┘
                                             │
                              ┌──────────────┼──────────────┐
                              │              │              │
                              ▼              ▼              ▼
                      ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
                      │   LineaDAO   │ │   ParadaDAO  │ │   TramoDAO   │
                      │  (Interface) │ │  (Interface) │ │  (Interface) │
                      └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
                             │                │                │
            ┌────────────────┼────────────────┼────────────────┼────────────────┐
            │                │                │                │                │
            ▼                ▼                ▼                ▼                ▼
    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
    │    Linea     │ │   Parada     │ │    Tramo     │ │    Linea     │ │   Parada     │
    │ SecuencialDAO│ │SecuencialDAO │ │SecuencialDAO │ │ AleatorioDAO │ │AleatorioDAO  │
    └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
           │                │                │                │                │
           ▼                ▼                ▼                ▼                ▼
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │                        Persistence Layer                                    │
    │  - linea_PM.txt, parada_PM.txt, tramo_PM.txt (Sequential)                  │
    │  - linea.dat, parada.dat, tramo.dat (Random Access Binary)                 │
    └─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎓 Principios SOLID Aplicados

### Single Responsibility Principle (SRP)
- ✅ Cada DAO se encarga solo de su entidad
- ✅ Services separan lógica de negocio de persistencia
- ✅ Interfaz solo maneja interacción con usuario

### Open/Closed Principle (OCP)
- ✅ Nuevas estrategias de búsqueda sin modificar existentes
- ✅ Nuevas interfaces sin tocar lógica de negocio
- ✅ Nuevas fuentes de datos sin cambiar services

### Liskov Substitution Principle (LSP)
- ✅ Cualquier implementación de `LineaDAO` es intercambiable
- ✅ Cualquier `BuscadorRutas` puede usarse indistintamente
- ✅ Cualquier `Interfaz` cumple el contrato

### Interface Segregation Principle (ISP)
- ✅ Interfaces pequeñas y específicas (LineaDAO, ParadaDAO, TramoDAO)
- ✅ Clientes no dependen de métodos que no usan

### Dependency Inversion Principle (DIP)
- ✅ Services dependen de interfaces DAO, no de implementaciones concretas
- ✅ Coordinador depende de interface Interfaz
- ✅ Factory inyecta dependencias mediante reflexión

---

## 📚 Referencias y Recursos

### Libros
- **Gang of Four**: "Design Patterns: Elements of Reusable Object-Oriented Software" (1994)
- **Martin Fowler**: "Patterns of Enterprise Application Architecture" (2002)
- **Robert C. Martin**: "Clean Architecture" (2017)

### Patrones Relacionados
- **DAO**: Core J2EE Patterns
- **Factory**: Creational Pattern (GoF)
- **Singleton**: Creational Pattern (GoF)
- **Strategy**: Behavioral Pattern (GoF)
- **MVC**: Architectural Pattern
- **Service Layer**: Enterprise Pattern (Fowler)
- **Repository**: Domain-Driven Design Pattern

### Documentación del Proyecto
- `formato-binario-aleatorio.md` - Especificación de archivos `.dat`
- `buenas-practicas-POO.instructions.md` - Lineamientos de la cátedra

---

## ✅ Checklist de Implementación

Al implementar estos patrones en un proyecto similar:

### DAO Pattern
- [ ] Definir interfaces DAO por cada entidad
- [ ] Implementar al menos 2 estrategias de persistencia
- [ ] Usar Factory para obtención de DAOs
- [ ] Garantizar que todas las referencias usen mismas instancias

### Factory Pattern
- [ ] Crear `factory.properties` con mappings clase→implementación
- [ ] Implementar caché con `ConcurrentHashMap`
- [ ] Usar reflexión para instanciación dinámica
- [ ] Manejar excepciones de forma descriptiva

### Service Layer
- [ ] Crear interfaces de servicio por cada DAO
- [ ] Implementar validaciones de negocio en services
- [ ] Inyectar DAOs vía Factory
- [ ] Mantener services sin estado (stateless)

### Strategy Pattern
- [ ] Definir interface común para estrategias
- [ ] Implementar estrategias independientes
- [ ] Permitir composición de estrategias
- [ ] Considerar ejecución paralela

### MVC + Coordinador
- [ ] Usar Coordinador para orquestar flujo (NO Singleton)
- [ ] Gestionar ciclo de vida de Coordinador en InicializadorSistema
- [ ] Usar Repository Singleton (Ciudad) para datos compartidos
- [ ] Separar claramente Model, View, Controller
- [ ] Desacoplar vista de lógica mediante interfaces
- [ ] Permitir múltiples vistas

### Inicialización Parametrizable
- [ ] Crear inicializador centralizado
- [ ] Usar Factory para selección de interfaz
- [ ] Permitir cambio de configuración sin recompilar
- [ ] Ejecutar cálculos en hilos separados (no bloquear UI)

---

**Última actualización:** 20 de Octubre de 2025  
**Versión del documento:** 2.0  
**Autores:** Equipo POO-2025  
**Proyecto:** colectivo-base  

---

*Este documento es un recurso vivo. Si implementas mejoras o identificas nuevos patrones, por favor actualiza esta documentación.*

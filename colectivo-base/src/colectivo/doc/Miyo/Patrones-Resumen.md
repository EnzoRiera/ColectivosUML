# Patrones de Diseño Implementados en colectivo-base

**Proyecto:** Sistema de Gestión de Líneas de Colectivo  
**Versión:** 2.0  
**Fecha:** 22 de Octubre de 2025  
**Autor:** Equipo 2 POO-2025  

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

- **Secuencial**: Lee archivos de texto delimitados
- **Aleatorio**: Gestiona archivos binarios de acceso directo
- **Base de Datos**: Obtiene los datos de una base de datos PostgreSQL

**Ejemplo de uso:**

```java
ParadaDAO paradaDAO = Factory.getInstance("PARADA", ParadaDAO.class);
Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
```

---

### 2. Abstract Factory + Registry (con Reflection)

**Propósito:** Crear instancias de componentes dinámicamente mediante configuración externa y cachear instancias compartidas.

**Archivo clave:** `util/Factory.java`

**Nota importante:** Se realizó un refactor de la versión original de la cátedra que contenía métodos deprecados, modernizando la implementación y mejorando su mantenibilidad.

**¿Qué es Registry con Reflection?**

El patrón Registry mantiene un mapa centralizado de instancias compartidas identificadas por claves lógicas. Cuando se solicita una instancia por primera vez, el Factory utiliza **reflexión** (Reflection API de Java) para:

1. Leer el nombre completo de la clase desde un archivo de propiedades
2. Cargar dinámicamente la clase en tiempo de ejecución usando `Class.forName()`
3. Crear una nueva instancia mediante `getDeclaredConstructor().newInstance()`
4. Almacenar la instancia en el registro para reutilización

Esto permite cambiar implementaciones completas sin recompilar el código, solo modificando archivos de configuración.

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

**Responsabilidades:**

- Cargar todas las entidades una sola vez al inicio
- Mantener colecciones en memoria para acceso rápido
- Garantizar consistencia entre referencias bidireccionales
- Proveer acceso centralizado a los datos

**Justificación del Singleton:**

✅ **Integridad referencial**: Todas las entidades comparten las mismas instancias  
✅ **Eficiencia**: Se carga una sola vez, se usa en toda la aplicación  
✅ **Consistencia**: Un solo punto de verdad para el estado del sistema  
✅ **Thread-safety**: Inicialización lazy con double-check.

#### 3.2. DAO Registry (vía Factory)

El Factory implementa un patrón **Multiton/Registry** donde cada clave lógica tiene su instancia compartida:

- `"PARADA"` → Una única instancia de `ParadaSecuencialDAO`
- `"LINEA"` → Una única instancia de `LineaSecuencialDAO`
- `"TRAMO"` → Una única instancia de `TramoSecuencialDAO`

**Beneficio crítico:** Garantiza que todas las referencias a `Parada`, `Linea` y `Tramo` apunten a los mismos objetos en memoria a través del repositorio `Ciudad`.

#### 3.3. Coordinador NO es Singleton

**IMPORTANTE:** `Coordinador` **NO** es un Singleton. Su ciclo de vida está gestionado por `AplicacionConsulta`.

**Razones para NO usar Singleton en Coordinador:**

❌ No tiene estado compartido que requiera ser único  
❌ Su propósito es coordinar, no almacenar datos  
✅ Mejor testabilidad (se pueden crear múltiples instancias para tests)  
✅ Ciclo de vida claro (se crea en `iniciar()`, se usa, se descarta)  
✅ Evita acoplamiento global innecesario  

---

### 4. Service Layer (Facade)

**Propósito:** Proveer una API de alto nivel que encapsula la complejidad de los DAOs e Interfaces y aplica lógica de negocio.

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

** Modelo :** Cambios Minimos respecto de los originales.

		1.	Responsabilidad: Entidades de dominio.
		
		2.	Archivos: modelo/Linea.java, modelo/Parada.java, modelo/Tramo.java, modelo/Recorrido.java

** Vista :** Ambas Vistas implementan Interfaz.

	1. Responsabilidad: Interacción con el usuario.
	
	2. Archivos: interfaz/Interfaz.java, interfaz/InterfazConsola.java, interfaz/Controller.java , interfaz/view.fxml

** Controlador: ** en el caso de la app JavaFX , comparte rol controllador.

	1.	Responsabilidad: Coordinación y flujo de la aplicación.
	
	2.	Archivos: aplicacion/Coordinador.java


**Ventajas de esta arquitectura:**

✅ Clara separación de responsabilidades  
✅ Fácil mantenimiento y extensibilidad  
✅ Testeable en cada capa independientemente  
✅ Múltiples vistas sin modificar el modelo  

---

### 6. Strategy (Algoritmos Intercambiables)

**Propósito:** Permitir diferentes estrategias de búsqueda de rutas sin modificar el código cliente.

**Interface común:**

```java
public interface EstrategiaBusqueda {
    List<List<Recorrido>> buscar(Parada origen, Parada destino, int diaSemana, LocalTime hora, Map<Parada, List<Tramo>> conexionesParadas, Map<String, Tramo> todosLosTramos);
}
```

**Observación importante sobre la implementación actual:**

Actualmente, el sistema utiliza todas las estrategias de forma secuencial, ejecutándolas una tras otra hasta que la primera encuentre un recorrido válido y lo devuelve inmediatamente. Esta implementación **no aprovecha completamente el patrón Strategy**, ya que el patrón está diseñado para **elegir una estrategia específica según un criterio** (por ejemplo: tiempo mínimo, menor cantidad de transbordos, menor distancia caminando, etc.) y ejecutar solo esa estrategia.

En futuras iteraciones, se podría implementar un mecanismo de selección que permita al usuario o al sistema elegir la estrategia más adecuada según el contexto o preferencias, ejecutando únicamente la estrategia seleccionada en lugar de todas secuencialmente.

**Ventajas:**

✅ Agregar nuevas estrategias sin modificar código existente (Open/Closed)  
✅ Cada estrategia es independiente y testeable  
✅ Fácil combinación de múltiples estrategias  
✅ Permitiría ejecutar estrategias en paralelo (threads)  

---

### 7. Repository (In-Memory) + Singleton

**Propósito:** `Ciudad` implementa el patrón Repository para centralizar el acceso a colecciones de entidades de dominio en memoria, combinado con Singleton para garantizar una única instancia compartida.

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

Esta combinación es esencial porque:

1. **Repository**: Encapsula el acceso a colecciones de entidades
2. **Singleton**: Garantiza que todos los componentes usen las mismas instancias en memoria

El patrón Repository + Singleton es común en arquitecturas donde el repositorio carga datos una sola vez, los datos en memoria representan el estado actual del sistema, y se requiere consistencia global entre componentes.

---

### 8. Utility / Helper

**Propósito:** Encapsular lógica reutilizable, operaciones comunes y objetos de valor.

**Componentes:**

## Archivos del Proyecto

### `util/Tiempo.java`
- **Creado por:** Cátedra
- **Propósito:** Clase inmutable para operaciones con tiempo. Convierte segundos a `LocalTime`

### `logica/CalculosAuxiliares.java`
- **Creado por:** Equipo Desarrollo
- **Propósito:** Métodos compartidos entre las distintas estrategias de búsqueda:
  - Creación de recorridos
  - Asignación de horarios
  - Cálculo de duración
  - Conexiones entre paradas

### `datos/CargarParametros.java`
- **Creado por:** Cátedra
- **Propósito:** Carga de parámetros desde `config.properties`
  - Implementación del primer incremento
  - Sigue funcionando en TESTS

### `datos/CargarDatos.java`
- **Creado por:** Equipo Desarrollo
- **Propósito:** Carga de datos del primer incremento previo a la implementación de DAO
  - Cuenta con aplicación para verificar carga
  - Incluye pruebas unitarias

### `util/FileUtil.java`
- **Creado por:** Cátedra
- **Propósito:** Clase de utilidad provista por la cátedra en el ejemplo subte
  - Reutilizada para carga con acceso aleatorio
  - Operaciones de lectura/escritura en archivos binarios

**Ventajas:**

✅ Evita duplicación de código  
✅ Centraliza operaciones comunes  
✅ Facilita mantenimiento  
✅ Mejora legibilidad del código de negocio  

---

### 9. Inicialización Parametrizable + Interfaz Desacoplada

**Propósito:** Permitir que el sistema se inicialice dinámicamente y soporte múltiples interfaces de usuario (Consola, JavaFX, Web) sin modificar la lógica de negocio.

#### 9.1. Componentes del Patrón

**Inicializador Centralizado:**

- Archivo: `aplicacion/Coordinador.java` -> metodo inicializarAplicacion();

**Interfaz Desacoplada:**

- Archivo: `interfaz/Interfaz.java`

**Implementación para Consola:**

- Archivo: `interfaz/consola/InterfazConsola.java`

**Implementación para JavaFX:**

- Archivo: `interfaz/javafx/InterfazJavaFX.java`

#### 9.2. Configuración Externa

Actualmente, usando Factory solo se crea la instancia de `InterfazConsola` debido a las particularidades del `InterfazJavaFX` de JavaFX, que requiere inicialización especial gestionada por el framework JavaFX mediante anotaciones y el sistema de FXML.

#### 9.3. Punto de Entrada

**Archivos de entrada:**

- `aplicacion/AplicacionConsultas.java` - Punto de entrada unico para la aplicación. Idioma y vista se modifican en config.properties. Interfaz a utilizar y DAO , en factory.properties. Configuraciones de log en log4j.properties


**Nota:** Actualmente existen ambos cambios se realizan "manualmente" en el archivo de configuracion que corresponda , se buscara alternativa para hacerlo desde la interfaz.

#### 9.4. Flujo de Ejecución

```text
1. main() → AplicacionConsultas / AplicacionJavaFX
              ↓
2. Coordinador.inicializarAplicacion()
              ↓
3. Crea instancia de InterfazServiceImpl
              ↓
4. Inicializa Ciudad (carga datos desde DAOs)
              ↓
5. Factory lee factory.properties (se realiza en cada ServiceImpl, para elegir DAO e Interfaz)
              ↓
6. Crea Calculo con Ciudad
              ↓
7. Inyecta Coordinador como dependencia
              ↓
8. coordinador.iniciarConsulta()
              ↓
9. interfaz.iniciarDatos()
              ↓
10. Usuario interactúa con la interfaz
              ↓
11. Interfaz llama a calculo.calcularRutas() a travez de Coordinador
              ↓
12. Calculo ejecuta estrategias
              ↓
13. Interfaz muestra resultados
```

#### 9.5. Ventajas del Patrón

✅ **Configuración dinámica**: Cambiar interfaz sin recompilar  
✅ **Desacoplamiento total**: Lógica de negocio independiente de UI  
✅ **Múltiples interfaces**: Consola, JavaFX, Web, API REST  
✅ **Testeable**: Mock de Interfaz para testing automatizado  
✅ **Extensible**: Agregar nueva interfaz implementando `Interfaz`  
✅ **Thread-safe**: Cálculos en hilos separados no bloquean UI  
✅ **Inicialización centralizada**: Un solo punto de configuración  

---

## 📊 Diagrama de Arquitectura Completo

```text
┌──────────────────────────────────────────────────────────────┐
│            AplicacionConsultas / AplicacionJavaFX            │
│                    (Entry Point - main)                      │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Coordinador   │ ◄──── (NOT Singleton-Orchestrator) 
                    │ (Controller)  │       
                    └───┬───────┬───┘
                        │       │
        ┌───────────────┘       └───────────────┐
        ▼                                        ▼
┌───────────────┐                        ┌──────────────┐
│   Interfaz    │◄──── Factory ────────► │   Calculo    │
│    (View)     │      (Config)          │  (Business)  │
└───┬───────┬───┘                        └──────┬───────┘
    │       │                                   │
    │       │                            ┌──────┴─────────┐
    │       │                            │                │
    │       │                            ▼                ▼
    │       │                    ┌──────────────────┐  ┌─────────────┐
    │       │                    │EstrategiaBusqueda│  │   Ciudad    │
    │       │                    │  (Strategy)      │  │(Repository) │
    │       │                    └──────────────────┘  └───┬─────────┘
    │       │                                              │
    │       │                             ┌────────────────┼───────────┐
    │       │                             │                │           │
    ▼       ▼                             ▼                ▼           ▼
  ┌────────────┐                  ┌────────────┐  ┌────────────┐  ┌────────────┐
  │  Interfaz  │                  │   Linea    │  │   Parada   │  │   Tramo    │
  │  Service   │                  │  Service   │  │  Service   │  │  Service   │
  │  (Facade)  │                  │  (Facade)  │  │  (Facade)  │  │  (Facade)  │
  └─┬───────┬──┘                  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘
    │       │                           │               │               │
    ▼       ▼                           ▼               ▼               ▼
┌─────────────────┐           ┌─────────────────────────────────────────────┐
│ InterfazConsola │           │              Factory.java                   │
│ InterfazJavaFx  │◄──────────┤    (Abstract Factory + Registry)            │
└─────────────────┘           │    - ConcurrentHashMap<String, Object>      │
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
    │ SecuencialDAO│ │SecuencialDAO │ │SecuencialDAO │ │ AleatorioDAO │ │PostgresqlDAO │
    └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
           │                │                │                │                │
           ▼                ▼                ▼                ▼                ▼
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │                        Persistence Layer                                    │
    │  - linea_PM.txt, parada_PM.txt, tramo_PM.txt (Sequential)                   │
    │  - linea.dat, parada.dat, tramo.dat (Random Access Binary)                  │
    │  - PostgreSQL Database (via JDBC)                                           │
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
- ✅ Cualquier `EstrategiaBusqueda` puede usarse indistintamente
- ✅ Cualquier `Interfaz` cumple el contrato

### Interface Segregation Principle (ISP)

- ✅ Interfaces pequeñas y específicas (`LineaDAO`, `ParadaDAO`, `TramoDAO`)
- ✅ Clientes no dependen de métodos que no usan

### Dependency Inversion Principle (DIP)

- ✅ Services dependen de interfaces DAO, no de implementaciones concretas
- ✅ Coordinador depende de interface `Interfaz`
- ✅ Factory inyecta dependencias mediante reflexión

---

**Última actualización:** 30 de Octubre de 2025  
**Versión del documento:** 2.0  
**Autores:** Equipo 2 POO-2025  
**Proyecto:** colectivo-base  

---

> **Nota:** Este documento es un resumen ejecutivo de los patrones implementados. Para detalles de implementación completos, consultar PatronesV2.md

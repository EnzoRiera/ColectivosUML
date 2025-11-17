# Patrones de Diseño en colectivo-base

**Ruta del proyecto:** `E:\git\colectivo-workspace-poo2025\colectivo-base\src\`

## 📋 Resumen

Este documento describe los patrones de diseño y arquitectónicos implementados en el proyecto colectivo-base. El sistema separa responsabilidades entre persistencia (DAO), lógica de negocio (Service), coordinación (Coordinator) y presentación (Interfaz), con soporte para múltiples estrategias de cálculo de rutas.

## 🎯 Patrones Implementados

### 1. DAO (Data Access Object)

**Propósito:** Separar la lógica de acceso a datos del resto de la aplicación.

**Archivos clave:**

- **Interfaces:** `dao/LineaDAO.java`, `dao/ParadaDAO.java`, `dao/TramoDAO.java`
- **Implementaciones secuenciales:** `dao/secuencial/ParadaSecuencialDAO.java`, `dao/secuencial/LineaSecuencialDAO.java`, `dao/secuencial/TramoSecuencialDAO.java`
- **Implementaciones aleatorias:** `dao/aleatorio/EstacionAleatorioDAO.java`, `dao/aleatorio/LineaAleatorioDAO.java`, `dao/aleatorio/TramoAleatorioDAO.java`

**Beneficios:**

- Cambiar fuente de datos sin afectar la lógica de negocio
- Facilita testing con implementaciones mock
- Cumple con el principio de Inversión de Dependencias (DIP)

### 2. Abstract Factory + Registry (con Reflection)

**Propósito:** Crear instancias de DAOs dinámicamente mediante configuración externa y cachear instancias compartidas (Singleton por tipo).

**Archivos clave:**

- **Factory:** `conexion/Factory.java`
- **Configuración:** `factory.properties`, `secuencial.properties`, `aleatorio.properties`

**Implementación:**

```java
public static <T> T getInstance(String name, Class<T> type) {
    return INSTANCES.computeIfAbsent(name, Factory::createInstance);
}
```

**Comportamiento:**

- Primera llamada → crea instancia usando reflexión
- Llamadas posteriores → devuelve instancia cacheada (Singleton)
- Usa `ConcurrentHashMap` para thread-safety

**Ejemplo de uso:**

```java
ParadaDAO paradaDAO = Factory.getInstance("PARADA", ParadaDAO.class);
```

### 3. Singleton

**Propósito:** Garantizar una única instancia compartida de componentes críticos.

**Implementaciones:**

#### 3.1. Coordinator Singleton

**Archivo:** `aplicacion/Coordinador.java`

```java
private static Coordinador coordinador;

public static Coordinador getCoordinador() throws Exception {
    if (coordinador == null) {
        coordinador = new Coordinador();
    }
    return coordinador;
}
```

#### 3.2. DAO Registry (Factory)

**Archivo:** `conexion/Factory.java`

- Cachea DAOs por clave (comportamiento Multiton/Registry)
- Garantiza que todos los componentes usen las mismas instancias de Parada, Linea y Tramo

### 4. Service Layer (Facade)

**Propósito:** Proveer una API de alto nivel que encapsula la complejidad de los DAOs.

**Archivos clave:**

- **Interfaces:** `servicio/LineaService.java`, `servicio/ParadaService.java`, `servicio/TramoService.java`
- **Implementaciones:** `servicio/LineaServiceImpl.java`, `servicio/ParadaServiceImpl.java`, `servicio/TramoServiceImpl.java`

**Responsabilidades:**

- Orquestación de múltiples DAOs
- Validación de reglas de negocio
- Transformación de datos entre capas

**Ejemplo:**

```java
public class ParadaServiceImpl implements ParadaService {
    private ParadaDAO paradaDAO;

    public ParadaServiceImpl() {
        this.paradaDAO = Factory.getInstance("PARADA", ParadaDAO.class);
    }

    @Override
    public Map<Integer, Parada> buscarTodos() throws Exception {
        return paradaDAO.buscarTodos();
    }
}
```

### 5. MVC + Coordinador + Inicializador

**Propósito:** Separar responsabilidades entre vista, controlador, modelo y configuración.

**Componentes:**

| Componente  | Responsabilidad              | Archivo(s)                                                              |
|-------------|------------------------------|-------------------------------------------------------------------------|
| Model       | Entidades de dominio         | `modelo/Linea.java`, `Parada.java`, `Recorrido.java`, `Tramo.java`     |
| View        | Interacción con usuario      | `interfaz/Interfaz.java`, `InterfazConsola.java`, `InterfazJavaFX.java` |
| Controller  | Coordinación y flujo         | `aplicacion/Coordinador.java`                                           |
| Initializer | Configuración dinámica       | `aplicacion/InicializadorSistema.java`                                  |
| Application | Punto de entrada             | `aplicacion/AplicacionConsultas.java`                                   |

**Flujo de comunicación:**

```
AplicacionConsultas → InicializadorSistema → Coordinador → Interfaz
                          ↓
                      Calculo + Ciudad
                          ↓
                    Services → DAOs
```

**Mejoras:**

- La interfaz se selecciona dinámicamente desde `factory.properties`
- `InicializadorSistema` encapsula la lógica de arranque
- Interfaz desacoplada permite múltiples implementaciones sin modificar el flujo

### 6. Strategy (Algoritmos Intercambiables)

**Propósito:** Permitir diferentes estrategias de búsqueda de rutas sin modificar el cliente.

**Jerarquía de clases:**

```
BuscadorRutas (interface)
    ├── BuscadorRutasDirectas
    ├── BuscadorRutasConTransbordo
    └── BuscadorRutasConCaminata
```

**Archivos clave:**

- **Interface:** `logica/BuscadorRutas.java`
- **Implementaciones:**
  - `logica/BuscadorRutasDirectas.java`
  - `logica/BuscadorRutasConTransbordo.java`
  - `logica/BuscadorRutasConCaminata.java`
- **Cliente:** `logica/Calculo.java`, `logica/CalculoMiyoPolimorfismo.java`

**Uso:**

```java
BuscadorRutas buscador = new BuscadorRutasDirectas();
List<Recorrido> recorridos = buscador.buscar(origen, destino, hora, dia);
```
- Las estrategias pueden ejecutarse en **hilos separados** para evitar bloqueo de interfaz, especialmente en entornos gráficos como JavaFX.


### 7. Repository (In-Memory)

**Propósito:** Centralizar el acceso a colecciones de entidades de dominio.

**Implementación:**

- **Archivo:** `logica/Ciudad.java`
- **Estructura:**

```java
private Map<Integer, Parada> paradas;
private Map<String, Linea> lineas;
private Map<String, Tramo> tramos;
```

**Responsabilidades:**

- Cargar datos desde DAOs al inicio
- Proveer acceso centralizado a las entidades
- Mantener consistencia entre referencias bidireccionales

### 8. Utility / Helper

**Propósito:** Encapsular lógica reutilizable y objetos de valor.

**Archivos clave:**

- `util/Tiempo.java` - Clase inmutable para representación de tiempo
- `logica/UtilidadesTiempo.java` - Operaciones de cálculo temporal
- `datos/CargarParametros.java` - Parseo de configuraciones
- `datos/CargarDatos.java` - Inicialización de datos

### 9. Inicialización Parametrizable + Interfaz Desacoplada

**Propósito:** Permitir que el sistema se inicialice dinámicamente y soporte múltiples interfaces (consola, JavaFX) sin modificar la lógica de negocio.

**Componentes clave:**

- **Inicializador:** `aplicacion/InicializadorSistema.java`
- **Configuración:** `factory.properties`
- **Interfaz desacoplada:** `interfaz/Interfaz.java` (interface), `InterfazConsola.java`, `InterfazJavaFX.java`
- **Coordinador:** `aplicacion/Coordinador.java`

**Características:**

- `InicializadorSistema` configura `Ciudad`, `Calculo`, `Coordinador` e `Interfaz` usando `Factory`
- La interfaz se selecciona dinámicamente desde `factory.properties` mediante reflexión
- `Interfaz` define el contrato común para cualquier vista
- `Calculo` se ejecuta en hilo separado para evitar bloqueo de UI (especialmente en JavaFX)
- Se puede integrar Observer para que la interfaz reaccione automáticamente a nuevos resultados
- La clase `AplicacionConsultas` conserva el método `iniciarConsulta()` como entrada lógica reutilizable para pruebas, simulaciones o integración con interfaces.


**Ejemplo de configuración:**

```properties
# factory.properties
INTERFAZ = colectivo.interfaz.consola.InterfazConsola
```

---

## 🔧 Solución a Problemas Conocidos

### ❌ Problema 1: Referencias Duplicadas de Paradas

**Síntoma:**

```
Recorridos calculados: 0
First Parada hashCode: 463345942
  Lineas referenced: 0
First Linea hashCode: 1143839598
  Paradas referenced: 0
```

**Causa:** Cada DAO creaba su propia instancia de `ParadaSecuencialDAO`, generando mapas separados.

**Solución aplicada:**

#### 1. Constructores con Inyección de Dependencias

`TramoSecuencialDAO.java`:

```java
public TramoSecuencialDAO() {
    this(Factory.getInstance("PARADA", ParadaDAO.class));
}

public TramoSecuencialDAO(ParadaDAO paradaDAO) {
    this.paradaDAO = paradaDAO;
    this.paradas = cargarParadas();
    // ...
}
```

`LineaSecuencialDAO.java`:

```java
public LineaSecuencialDAO() {
    this(Factory.getInstance("PARADA", ParadaDAO.class));
}

public LineaSecuencialDAO(ParadaDAO paradaDAO) {
    this.paradaDAO = paradaDAO;
    this.paradas = cargarParadas();
    // ...
}
```

#### 2. Services usando Factory

`LineaServiceImpl.java`:

```java
public TramoServiceImpl() {
    this.tramoDAO = Factory.getInstance("TRAMO", TramoDAO.class);
}
```

`TramoServiceImpl.java`:

```java
public TramoServiceImpl() {
    this.tramoDAO = Factory.getInstance("TRAMO", TramoDAO.class);
}
```

**Resultado:**

```
✅ Recorridos calculados: 2
✅ First Parada hashCode: 1334729950
     Lineas referenced: 5
✅ First Linea hashCode: 2143192188
     Paradas referenced: 18
```

---

### ❌ Problema 2: Case Mismatch en Nombres de Clases

**Síntoma:**

```
java.lang.NoClassDefFoundError: colectivo/dao/secuencial/LineaSecuencialDAO 
(wrong name: colectivo/dao/secuencial/LineaSecuencialDao)
```

**Causa:** Desincronización entre:

- Nombre del archivo: `LineaSecuencialDao.java`
- Declaración de clase: `public class LineaSecuencialDAO`
- Entrada en properties: `LINEA=colectivo.dao.secuencial.LineaSecuencialDAO`

**Solución:**

1. Renombrar archivo: `LineaSecuencialDao.java` → `LineaSecuencialDAO.java`
2. Actualizar properties:
   ```properties
   LINEA=colectivo.dao.secuencial.LineaSecuencialDAO
   ```

---

### ❌ Problema 3: NullPointerException en Coordinador

**Síntoma:**

```
Cannot invoke "colectivo.aplicacion.Coordinador.iniciarConsulta()" 
because "miAplicacion.coordinador" is null
```

**Causa:** No asignar la instancia del Singleton al campo de la clase.

**Solución:**

```java
public class AplicacionConsultas {
    private Coordinador coordinador;

    public AplicacionConsultas() {
        try {
            // ✅ CORRECTO: Asignar instancia
            coordinador = Coordinador.getCoordinador();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## 📊 Diagrama de Dependencias

```
┌─────────────────────┐
│ AplicacionConsultas │
└───────────┬─────────┘
            │
            ▼
    ┌──────────────┐
    │ Coordinador  │ (Singleton)
    └──┬───────┬───┘
       │       │
       ▼       ▼
┌──────────┐  ┌─────────┐
│ Interfaz │  │ Calculo │
└──────────┘  └────┬────┘
                   │
                   ▼
              ┌────────┐
              │ Ciudad │ (Repository)
              └───┬────┘
                  │
        ┌─────────┼─────────┐
        ▼         ▼         ▼
   ┌─────────┬─────────┬─────────┐
   │ Parada  │  Linea  │  Tramo  │ Services
   │ Service │ Service │ Service │
   └────┬────┴────┬────┴────┬────┘
        │         │         │
        ▼         ▼         ▼
   ┌─────────────────────────────┐
   │        Factory.java         │ (Abstract Factory + Registry)
   └─────────────┬───────────────┘
                 │
        ┌────────┼────────┐
        ▼        ▼        ▼
   ┌─────────┬─────────┬─────────┐
   │ Parada  │  Linea  │  Tramo  │ DAOs (Singleton por tipo)
   │   DAO   │   DAO   │   DAO   │
   └─────────┴─────────┴─────────┘
```

---

## 🎓 Recomendaciones para Mejoras

### 1. Testabilidad

- Usar inyección de dependencias explícita en lugar de Singletons
- Crear interfaces para `Coordinador` y `Ciudad`
- Implementar versiones mock para testing

### 2. Configuración

- Agregar test unitario que valide `factory.properties`

### 3. Documentación

- Agregar JavaDoc con descripción de patrones en clases clave:

```java
/**
 * Factory con patrón Registry que cachea instancias DAO.
 * Usa reflexión para crear objetos basándose en factory.properties.
 * Thread-safe mediante ConcurrentHashMap.
 * 
 * @pattern Abstract Factory + Singleton (Multiton)
 */
```

### 4. Manejo de Errores

- Validar que `factory.properties` contenga todas las claves necesarias
- Agregar mensajes de error descriptivos en `Factory.createInstance()`
- Implementar fallbacks para configuraciones inválidas

### 5. Observabilidad

- Integrar el patrón Observer para que las interfaces reaccionen automáticamente a nuevos resultados de búsqueda.
- Permitir que `InterfazConsola` y `InterfazJavaFX` se suscriban a eventos del `Coordinador` o `Calculo`.


---

## 📚 Referencias

- Gang of Four: "Design Patterns: Elements of Reusable Object-Oriented Software"

---

**Última actualización:** 17 de Octubre de 2025  
**Autor:** MiyoBran  
**Proyecto:** colectivo-base (POO-2025)
# Manual de Desarrollo: Sistema de Consultas de Recorridos de Colectivos Urbanos

## Introducción

Este manual proporciona documentación técnica completa para el desarrollo y mantenimiento del Sistema de Consultas de Recorridos de Colectivos Urbanos. Incluye información detallada sobre la arquitectura, diseño, implementación y mejores prácticas utilizadas en el proyecto.

## Arquitectura del Sistema

### Diseño por Capas

El sistema está organizado en tres capas principales siguiendo el patrón de arquitectura en capas:

#### 1. Capa de Presentación
- **Responsabilidades**: Interacción con el usuario, entrada/salida de datos, validación de entrada.
- **Componentes**:
  - `AplicacionConsultas.java`: Punto de entrada para interfaz de consola.
  - `Interfaz.java`: Contrato que deben respetar todas las interfaces.
  - `InterfazService.java y InterfazServiceImpl`: A travez del Factory crea la instancia configurada y abstrae al coordinador del llamado de los metodos especificos.
  - `InterfazConsola.java e InterfazJavaFx.java` : a partir de aqui esta la gestion completa de cada vista.

#### 2. Capa de Lógica de Negocio
- **Responsabilidades**: Cálculo de rutas, algoritmos de búsqueda, lógica de negocio.
- **Componentes**:
  - `Calculo.java`: Algoritmos principales para cálculo de recorridos.
  - Contiene un llamado secuencial a las distintas estrategias (actualmente no se selecciona cual se usa segun un criterio , sino que la primera que devuelve resultado es la que se muestra.
  - `Ciudad.java`: Repositorio donde se mantienen las estructuras con los datos en memoria. (se obtienen a travez de Coordinador)
  - `CalculosAuxiliares.java`: Clase Utilitaria donde se encuentran metodos comúnes que se utilizan en el paquete.
  - `EstrategiaBusqueda.java`: Interface que determina el contrato que debe cumplir cada estrategia de busqueda , actualmente `BusquedaCaminando.java` , `BusquedaConTransbordo.java` y `BusquedaDirecta.java` , cada clase tiene ademas metodos auxiliares no compartidos con otras estrategias.

#### 3. Capa de Datos (DAO)
- **Responsabilidades**: Acceso a datos, persistencia, abstracción de almacenamiento.
- **Componentes**:
  - Interfaces: `ParadaDAO`, `LineaDAO`, `TramoDAO`.
  - Implementaciones: `ParadaDAOSecuencial`, `LineaDAOSecuencial`, `TramoDAOSecuencial`.
  - Implementaciones: `ParadaPostgresqlDAO`, `LineaPostgresqlDAO`, `TramoPostgresqlDAO`.
  - Implementaciones: `ParadaAleatorioDAO`, `LineaAleatorioDAO`, `TramoAleatorioDAO`.

### Patrón DAO (Data Access Object)

Se implementa el patrón DAO para separar la lógica de acceso a datos:

```java
public interface ParadaDAO {
    Map<Integer, Parada> buscarTodos() throws DataAccessException;
}
```

**Ventajas**:
- Abstracción de la fuente de datos.
- Fácil cambio entre diferentes implementaciones (archivos, BD, etc.).
- Manejo centralizado de errores.

### Patrón Factory

Se utiliza el patrón Factory para creación de DAOs e interfaces:

```java
public class DAOFactory {
    public static ParadaDAO crearParadaDAO() {
        return new ParadaDAOSecuencial();
    }
}
```

**Beneficios**:
- Centralización de la creación de objetos.
- Fácil cambio de implementaciones.
- Configuración en un solo lugar.


## Modelo de Datos

La unica modificacion que se debio hacer al modelo de datos original fue agregado de metodo:
- `public List<LocalTime> getHorasFrecuencia(int diaSemana)` : necesario para determinar horario de llegada de linea a la parada en la que se sube el pasajero (y afecta todas las duraciones calculadas) 

### Diagrama UML de Clases

```
+----------------+     +----------------+     +----------------+
|     Parada     |     |     Linea      |     |     Tramo      |
+----------------+     +----------------+     +----------------+
| - codigo: int  |     | - codigo: String|   | - inicio: Parada|
| - direccion: String| | - nombre: String|   | - fin: Parada  |
| - lineas: List<Linea>| | - paradas: List<Parada>| - tiempo: int  |
| - paradaCaminando: | | - frecuencias: |   | - tipo: int     |
|   List<Parada> |     |   List<Frecuencia>| +----------------+
| - latitud: double|   +----------------+   | calcularTiempo()|
| - longitud: double|   | buscarParada() |   +----------------+
+----------------+     | buscarFrecuencia()|
| getLineas()     |     +----------------+
| getParadasCaminando()|
+----------------+
          ^
          |
+----------------+     +----------------+
|   Frecuencia    |     |   Recorrido    |
+----------------+     +----------------+
| - diaSemana: int|     | - linea: Linea |
| - hora: LocalTime|    | - paradas:     |
+----------------+     |   List<Parada> |
                        | - horaSalida:  |
                        |   LocalTime    |
                        | - duracion: int|
                        +----------------+
```

### Estructuras de Datos Utilizadas

#### Map<Integer, Parada>
- **Uso**: Almacenamiento eficiente de paradas por código numérico.
- **Ventajas**: Búsqueda O(1), acceso rápido por ID.
- **Implementación**: `HashMap` para rendimiento óptimo.

#### List<Parada> en Linea
- **Uso**: Paradas ordenadas de una línea.
- **Orden**: Mantiene el orden secuencial de las paradas.

#### Map<String, Tramo>
- **Uso**: Tramos indexados por código (origen-destino).
- **Clave**: Formato "codigoOrigen-codigoDestino-Tipo".

### Relaciones Bidireccionales

- **Parada-Linea**: Una parada conoce sus líneas, una línea conoce sus paradas.
- **Parada-Parada**: Conexiones caminando entre paradas cercanas.

## Algoritmos de Cálculo

### Método `calcularRecorrido`

```java
public static List<List<Recorrido>> calcularRecorrido(
    Parada paradaOrigen,
    Parada paradaDestino,
    int diaSemana,
    LocalTime horaLlegaParada,
    Map<String, Tramo> tramos)
```

#### Algoritmo Principal

1. **Rutas Directas**: Verificar si existe línea que conecte ambas paradas directamente.
2. **Transbordos**: Buscar paradas intermedias donde se pueda cambiar de línea.
3. **Caminando**: Incluir conexiones a pie entre paradas.
4. **Deduplicación**: Eliminar rutas equivalentes.
5. **Ordenamiento**: Por duración total.

#### Lógica de Rutas Directas

```java

--- ACTUALIZAR

```

#### Lógica de Transbordos

```java

---- ACTUALIZAR


```

## Persistencia de Datos

### Formato de Archivos

Los datos se almacenan en archivos de texto plano con formato semicolon-separated:

#### parada_PM.txt
```
codigo;direccion;latitud;longitud
1;Calle Principal;-43.123;-65.456
```

#### linea_PM.txt
```
codigo;nombre
L1;Linea 1
```

#### tramo_PM.txt
```
codigoOrigen;codigoDestino;tiempo;tipo
1;2;10;1
```

#### frecuencia_PM.txt
```
codigoLinea;diaSemana;hora
L1;1;08:00
```

### Carga de Datos (primer incremento)

La clase `CargarDatos` maneja la carga y parsing de archivos:

- **Validación**: Verifica formato y consistencia de datos.
- **Excepciones**: Lanza `DataAccessException` en caso de errores.
- **Encoding**: Utiliza UTF-8 para soporte de caracteres especiales.

## Testing

### Estrategia de Testing

- **Pruebas Manuales** : de cambios de configuracion , adicion de datos validos o invalidos en documentos de datos
- **JUnit 4**: tests aportados por la catedra para verificar funcionamiento de Calculo. Actualmente funcionan tanto la primera version como la version DAO, con cualquier implementacion del mismo.
- **JUnit 5**: Framework de testing unitario. AplicacionesDemo para verificaciones
- **Cobertura Completa**: Tests para métodos críticos de carga de datos. 
- **Tests Compartidos**: Tests para `calcularRecorrido` compartidos entre grupos (catedra).

### Tests Implementados

#### TestcalcularRecorrido.java y TestcalcularRecorridoDAO.java

- **Pruebas de Rutas Directas**: Verificar cálculo correcto de rutas sin transbordos.
- **Pruebas de Transbordos**: Validar rutas con cambios de línea.
- **Pruebas de Caminando**: Comprobar conexiones a pie.
- **Pruebas de Bordes**: Casos sin rutas disponibles, paradas inválidas.


## Documentación del Código

### JavaDoc

Todo el código público incluye documentación JavaDoc completa:

```java
/**
 * Calcula los recorridos posibles entre dos paradas.
 *
 * @param paradaOrigen Parada de inicio
 * @param paradaDestino Parada final
 * @param diaSemana Día de la semana (1-7)
 * @param horaLlegaParada Hora deseada de llegada
 * @param tramos Mapa de tramos disponibles
 * @return Lista de listas de recorridos posibles
 */
public static List<List<Recorrido>> calcularRecorrido(...)
```

### Generación de Documentación
Desde el IDE en el proyecto en el que hay que gestionar las dependencias de forma manual , o :

```bash
mvn javadoc:javadoc
```
---------------------------------------
VERIFICAR SI AGREGAMOS ESTA SECCION
-----------------------------------
## Errores Detectados y Soluciones

### Problemas de Rendimiento

**Problema**: Búsquedas lineales en listas grandes.
**Solución**: Implementación de `Map<Integer, Parada>` para búsquedas O(1).

### Problemas de Memoria

**Problema**: Carga completa de datos en memoria.
**Solución**: Optimización de estructuras de datos, uso eficiente de referencias.

### Problemas de Concurrencia

**Problema**: No implementado (fuera del alcance básico).
**Solución Futura**: Implementar hilos para cálculos pesados.

### Problemas de Validación

**Problema**: Errores en parsing de horas.
**Solución**: Validación robusta de formatos de entrada.

---------------------------------------
FIN SECCION EN DUDA
-----------------------------------

## Mejoras y Extensiones

### Mejoras Implementadas

1. **Estructuras de Datos Optimizadas**: Uso de Maps para acceso rápido.
2. **Configuracion**: Sistema parametrizado y configurable a partir de .properties.
3. **Interfaz JavaFX**: Implementación completa de GUI.
4. **Testing Exhaustivo**: Cobertura completa de lógica crítica.
5. **Persistencia en Base de Datos**: Migrar de archivos a BD relacional.
6. **Logging**: Sistema de logging.
7. **I18N**: Actualmente es_ES y en_US , pero el sistema esta internacionalizado (salvo toString de capa Modelo).

### Extensiones Futuras

1. **Manejo de Errores Robusto**: Excepciones tipadas y propagación adecuada.
2. **Simulación Concurrente**: Implementar hilos para simulación en tiempo real.
3. **Interfaz Web**: Desarrollar aplicación web con mapas interactivos.
4. **APIs Externas**: Integración con APIs de transporte público.
5. **Caching**: Implementar caché para consultas frecuentes.


## Conclusiones

### Estructuras Utilizadas

- **Mapas (HashMap)**: Para acceso O(1) a paradas y tramos.
- **Listas (ArrayList)**: Para mantener orden en recorridos.
- **Excepciones Tipadas**: Para manejo claro de errores.

### Diseño por Capas

El diseño por capas ha demostrado ser efectivo:
- **Separación de Responsabilidades**: Cada capa tiene responsabilidades claras.
- **Mantenibilidad**: Cambios en una capa no afectan otras.
- **Testabilidad**: Capas pueden testearse independientemente.
- **Extensibilidad**: Fácil agregar nuevas funcionalidades.

### Patrones Implementados

- **DAO**: Abstracción perfecta de acceso a datos.
- **Factory**: Creación centralizada de objetos.
- **MVC**: Separación clara en interfaces.
- **Repository**: para mantener estructuras de datos en tiempo de ejecucion.
- **Facade**: tanto la implementacion del DAO como de la Interfaz , se hace a travez de estas fachadas- **Strategy**: en calculos de rutas
- **Strategy**: en calculos de rutas

### Lecciones Aprendidas

1. **Importancia de las Estructuras de Datos**: La elección correcta (Map vs List) impacta significativamente el rendimiento.

2. **Manejo de Errores**: Las excepciones tipadas facilitan debugging y mantenimiento.

3. **Testing Primero**: Los tests exhaustivos previenen regresiones y validan lógica.

4. **Documentación Continua**: Mantener documentación actualizada es crucial para equipos.

5. **Arquitectura Escalable**: El diseño por capas permite crecimiento ordenado.

### Tecnologías

- **Java 21**: LTS mas actual ( java 25 es de Septiembre 2025 , es decir salio al momento de creacion del proyecto).
- **Maven**: Gestión excelente de dependencias y build.
- **JUnit 5**: Testing moderno.
- **JavaFX**: GUI nativa y potente.
- **Git**: Control de versiones.

## Referencias

- [Java SE 21 Documentation](https://docs.oracle.com/en/java/javase/21/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JavaFX Documentation](https://openjfx.io/javadoc/21/)
- [Design Patterns - Gang of Four](https://en.wikipedia.org/wiki/Design_Patterns)

---

*Manual de Desarrollo - Sistema de Colectivos Urbanos - Versión 2.0*
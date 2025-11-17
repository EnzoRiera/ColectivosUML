# Guion Propuesto para la Presentación Final

## Sección 1: Introducción (2 Diapositivas)

## Diapositiva 1: Portada y Visión General

* **Título Principal:** Sistema de Gestión de Colectivos

* **Subtítulo:** Proyecto Final - Programación II

* **Equipo de Desarrollo:**

  * Matías Agustín Sepúlveda

  * Carlos Miyen Brandolino

  * Enzo Sebastián Riera

* **Descripción del Proyecto:**

  * Se presenta como una aplicación Java diseñada para consultar rutas de transporte público urbano.

  * Soporta dos interfaces: consola de comandos y una interfaz gráfica (GUI) desarrollada con JavaFX.

* **Características Principales (Capacidades del Sistema):**

  * Cálculo de recorridos: directos (una sola línea), con transbordos (combinación de líneas) y conexiones a pie.

  * Flexibilidad de datos: Capaz de consumir datos de múltiples fuentes (archivos de texto plano, bases de datos   PostgreSQL y archivos binarios de acceso aleatorio).

  * Objetivo: Optimizar la planificación de viajes en transporte público para el usuario.

---

## Diapositiva 2: Metodología y Herramientas

* **Título:** Metodología y Herramientas
* **Sección 1: Modalidad de Trabajo (Gestión):**
  * **Comunicación:** Discord (para interacción fluida y reuniones).
  * **Gestión de Tareas:** Trello (para organización de pendientes, en progreso y finalizadas).
  * **Control de Versiones:** Git y GitHub (para seguimiento de código y colaboración).
* **Sección 2: Herramientas de Desarrollo (Técnico):**
  * **IDEs:** Eclipse, IntelliJ IDEA, VSCode (reflejando las preferencias del equipo).
  * **Sistemas Operativos:** Windows y Linux.
* **Sección 3: División de Tareas (Post-Incremento 1):**
  * Se detalla la división de responsabilidades clave después de establecer la arquitectura base (inspirada en "Subte"):
  * **Miyén:** Arquitectura general, proyecto base Maven, gestión de Git, diseño de la interfaz desacoplada y la implementación del **DAO Secuencial**.
  * **Enzo:** Obtención y procesamiento de datos, implementación del **DAO PostgreSQL**, configuración de logs (Log4j) y mantenimiento de la BD.
  * **Agustín:** Optimización de algoritmos de cálculo de rutas, desarrollo de la interfaz **JavaFX**, formateo de datos y visualización de mapas.

---

## Sección 2: Arquitectura y Patrones (5 Diapositivas)

## Diapositiva 3: Arquitectura Inicial Inspirada en 'Subte'

* **Título:** Arquitectura Inicial Inspirada en 'Subte'
* **Subtítulo:** Replicación y Adaptación de Patrones Fundamentales
* **Contexto "Subte":**
  * Se explica que "Subte" fue el proyecto base de la cátedra, compuesto por 9-10 incrementos que añadían patrones de diseño progresivamente.
  * Se define el **desafío principal del equipo:** Implementar *todos* los patrones relevantes desde el inicio en una arquitectura unificada, en lugar de hacerlo incrementalmente.
* **4 Patrones Fundamentales Implementados (Base):**
  * **DAO (Data Access Object):** Para la abstracción del acceso a datos, separando la lógica de la persistencia.
  * **Factory Pattern:** Para la creación dinámica de instancias (especialmente DAOs e Interfaces).
  * **MVC (Model-View-Controller):** Para la separación de responsabilidades (Modelo, Vista, Controlador).
  * **Facade Pattern (Service Layer):** Para simplificar interfaces complejas, usando *Services* como punto de entrada a la lógica de negocio.
* **Principios de Diseño Respetados (Restricciones):**
  * No modificar el paquete **Modelo** (restricción explícita de la consigna).
  * Respetar nombres de paquetes originales del ejemplo "Subte".
  * Reutilizar clases utilitarias de "Subte" cuando fue posible.

---

## Diapositiva 4: Desafíos de Implementación: SecuencialDAO

* **Título:** Implementación de SecuencialDAO
* **Subtítulo:** Desafíos de Parsing y Carga de Datos desde `.txt`
* **Características de Archivos:**
  * Se describe el formato de los datos: archivos de texto plano (`.txt`) con delimitador punto y coma (`;`).
  * **Desafío de Parsing:** Se identifica un problema de localización: las coordenadas en los archivos usaban coma (`,`) como separador decimal, pero `Double.parseDouble()` de Java espera un punto (`.`).
  * **Solución:** Normalización mediante `String.replace(',', '.')` antes de parsear.
* **Modificación Necesaria del Modelo:**
  * Se destaca que el paquete Modelo (restringido) carecía de una forma de obtener los horarios de una línea.
  * **Solución:** Se agregó el método `public List<LocalTime> getHorasFrecuencia(int diaSemana)` a la clase `Linea.java` para poder calcular la espera del pasajero.
* **Desafíos de Carga Bidireccional:**
  * **Problema 1 (Línea ↔ Parada):** El método `linea.agregarParada()` también llamaba a `parada.agregarLinea()`, causando referencias circulares y duplicación de datos durante la carga inicial.
  * **Problema 2 (Tramos Caminando):** El constructor de `Tramo` (tipo 2) agregaba la conexión a pie en *ambas* paradas (`inicio.agregarParadaCaminado(fin)` y viceversa), generando también duplicados.
    * **Solución (Tramos):** Se modificó la carga para crear el tramo inverso manualmente, seteando el tipo *después* de llamar al constructor para evitar la doble inserción.

---

## Diapositiva 5: Patrones Clave de la Arquitectura (Flujo)

* **Título:** Patrones Clave de la Arquitectura
* **Subtítulo:** Flujo de Inicialización y Configuración Dinámica
* **Diseño:** Un flujo vertical que muestra la conexión e inicialización de los componentes.
* **Sección 1: 🏭 Factory + Registry (con Reflexión)**
  * **Contenido:** Muestra el archivo `factory.properties`.
  * **Explicación:** Detalla cómo, al comentar o descomentar líneas, se puede seleccionar dinámicamente qué implementación de DAO (Secuencial, Postgresql, Aleatorio) o de Interfaz (Consola, JavaFX) usar, **sin recompilar el código**.
* **Sección 2: 🚪 Service Layer (Facade)**
  * **Contenido:** Muestra el código de `InterfazServiceImpl`.
  * **Explicación:** El Service actúa como cliente del Factory. Pide la instancia "INTERFAZ" sin conocer la implementación concreta que el Factory le proveerá.
* **Sección 3: 💾 Ciudad (Repository + Singleton)**
  * **Contenido:** Muestra el método `getCiudad()` (acceso Singleton) y el `private Ciudad()` (constructor).
  * **Explicación:** Se establece `Ciudad` como el **Repositorio** centralizado y **Singleton**. Carga todos los datos (Paradas, Líneas, Tramos) una sola vez al inicio.
  * **Ventaja:** Optimización radical del rendimiento al evitar accesos repetidos a disco o BD para cada cálculo.
* **Sección 4: 🧠 Coordinador (Orquestador)**
  * **Contenido:** Muestra el método `inicializarAplicacion()`.
  * **Explicación:** Se enfatiza que el **Coordinador NO ES UN SINGLETON**. Su rol es orquestar e inicializar, conectando todos los componentes: `Configuracion`, `Ciudad` (el Singleton), `Calculo` y los `Services`.

---

## Diapositiva 6: Flujo de Ejecución y Arquitectura Final

* **Título:** Flujo de Ejecución y Arquitectura Final
* **Subtítulo:** Integración Completa de Patrones
* **Sección 1: Diagrama de Arquitectura Completo (Visual)**
  * **Contenido:** Un diagrama de arquitectura visual (basado en el ASCII del prompt) que muestra la estructura completa.
  * **Capas:**
    1. **Entry Point:** `AplicacionConsultas` / `AplicacionJavaFX`
    2. **Orchestrator:** `Coordinador` (No-Singleton)
    3. **View/Business:** `Interfaz` (View) y `Calculo` (Business)
    4. **Facades:** `InterfazService`, `EstrategiaBusqueda` (Strategy), `Ciudad` (Repository), `LineaService`, `ParadaService`, `TramoService`
    5. **Factory:** `Factory.java` (con Reflexión)
    6. **Interfaces DAO:** `LineaDAO`, `ParadaDAO`, `TramoDAO`
    7. **Implementaciones DAO:** `SecuencialDAO`, `AleatorioDAO`, `PostgresqlDAO`
    8. **Persistencia:** Archivos `.txt`, `.dat` y Base de Datos PostgreSQL.
* **Sección 2: Flujo de Consulta y Ventajas**
  * **Diagrama de Secuencia (Flujo):** Muestra la secuencia de una consulta típica:
    * Usuario → Interfaz → Coordinador → Calculo → EstrategiaBusqueda → Ciudad (Repository) → (Datos de retorno) → ... → Usuario.
  * **Ventajas Clave del Diseño:**
    * Desacoplamiento total entre capas.
    * Configuración dinámica (cambio de DAOs/UI) sin recompilar.
    * Alta extensibilidad y mantenibilidad.
    * Testabilidad independiente por capa.
    * Responsabilidades claras (SRP).

---

## Diapositiva 7: Funcionalidades Avanzadas (Profundización)

* **Título:** Funcionalidades Avanzadas
* **Subtítulo:** Inicialización Parametrizable, Interfaz Desacoplada y Strategy
* **Sección 1: Inicialización Parametrizable**
  * **Contenido:** Muestra el código completo de `Coordinador.inicializarAplicacion()`.
  * **Explicación:** Detalla el proceso de inyección de dependencias manual (ej. `setCoordinador()`) y el orden de inicialización: 1. Configuración, 2. Ciudad, 3. Calculo, 4. Interfaz.
* **Sección 2: Interfaz Desacoplada**
  * **Contenido:** Muestra el código de `InterfazServiceImpl` (obteniendo instancia del Factory).
  * **Explicación:** Reitera los principios de independencia entre lógica y UI.
  * **Desafío Clave (JavaFX):** Se destaca un problema técnico importante: `InterfazJavaFX` (que `extends Application`) pierde el contexto (el Coordinador inyectado) debido al ciclo de vida de JavaFX.
  * **Solución Implementada:** Se utilizó un `static Coordinador` para preservar el contexto entre la inicialización de la aplicación y el método `start()` de JavaFX.
* **Sección 3: Patrón Strategy**
  * **Contenido:** Muestra la interfaz `EstrategiaBusqueda`.
  * **Implementaciones:** Lista las estrategias creadas (Búsqueda Directa, Búsqueda Caminando, Búsqueda Con Transbordo).
    * **Uso Actual:** Se explica que el sistema ejecuta las estrategias *secuencialmente* y devuelve el primer resultado (una implementación simple).
    * **Ventajas a Futuro:** El patrón permitiría al usuario *elegir* la estrategia (ej. "más rápido" vs. "menos caminata") o ejecutarlas en paralelo.
    * **Clases Utilitarias:** Menciona `CalculosAuxiliares` (con métodos `static`) para evitar duplicación de código entre las diferentes 
- *Nota: Esta diapositiva sirve como puente hacia las siguientes secciones.*estrategias.

---

## Sección 3: Datos, DAOs Múltiples, Log4j e Internacionalización

### Diapositiva 8: Proceso de Datos

- Obtención de los datos originales.
- Proceso de limpieza, formato y creación de los archivos de texto finales.

### Diapositiva 9: Base de Datos PostgreSQL

- Diseño del esquema de la base de datos.
- Creación del script de carga de datos.
- Configuración de la conexión JDBC.
- Problemas y soluciones encontradas (ej. tipos de datos, constraints).

### Diapositiva 10: Implementación y Pruebas de DAOs

- Implementación del `PostgreSQLDAO`.
- **Pruebas realizadas:**
  - Manuales.
  - Tests unitarios (JUnit).
  - Validación con diferentes fuentes de datos (Secuencial vs. PostgreSQL) para asegurar consistencia.

### Diapositiva 11: Implementación de Log4j

- Configuración de `log4j.properties`.
- Uso de Log4j para registrar el historial de consultas y eventos importantes.
- Ejemplos de logs generados.
- Pruebas de funcionamiento del logging.

### Diapositiva 12: Internacionalización (I18N)

- Explicación del funcionamiento con `ResourceBundle`.
- Cómo se cargan los textos para diferentes idiomas (español/inglés).
- Impacto en la interfaz gráfica.

---

## Sección 4: Optimización e Interfaz JavaFX

### Diapositiva 13: Optimización y Patrón Strategy

- Optimización del algoritmo de cálculo de recorridos.
- **Diseño previsor con Patrón Strategy:**
  - Explicación de la implementación para las búsquedas (Directa, Caminando, Transbordo).
  - Justificación: Se diseñó una arquitectura flexible y escalable para el futuro, aunque la elección de la estrategia no sea dinámica en la UI actual. Esto demuestra una decisión de diseño inteligente.

### Diapositiva 14: Evolución de la Interfaz

- **Comparativa Visual:** Interfaz del 1er incremento vs. 2do incremento (usar imagen lado a lado).
- **Clase `Formateador`:** Su rol en la presentación de datos.
- **Desafíos de JavaFX:**
  - Necesidad de un doble controlador (`Coordinador` + `Controller` de FXML). Apoyar la explicación con el diagrama de arquitectura.
  - Problemas para mantener la interfaz desacoplada del resto de la lógica.

### Diapositiva 15: Hacia la Interfaz Final

- Investigación e intentos de integración de una API de mapas (ej. Google Maps, OpenStreetMap).
- Problemas encontrados (con y sin solución).

### Diapositiva 16: Integración Final en la Interfaz

- Unión de la UI con Log4j (mostrar logs en la interfaz).
- Integración de la funcionalidad de Internacionalización.

### Diapositiva 17: Demostración de la Interfaz Final

- Presentación en vivo de la aplicación funcional.
- Recorrido por las funcionalidades clave.

---

## Sección 5: Conclusiones

### Diapositiva 18: Documentación y Repositorio

- Muestra de la documentación generada para el proyecto (JavaDoc, manuales, etc.).
- Enlace al repositorio público en GitHub.

### Diapositiva 19: Conclusiones Finales

- Resumen de los logros del proyecto.
- Reflexión sobre el workflow y la dinámica de equipo.
- Aprendizajes específicos adquiridos (técnicos y no técnicos).

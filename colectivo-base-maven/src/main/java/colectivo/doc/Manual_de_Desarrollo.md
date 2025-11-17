# Manual de Desarrollo — Versión Maven

  Autores: Enzo Riera, Miyen Brandolino, Agustin Sepulveda

  Profesores: Gustavo Samec, Debora Pollicelli

  Fecha: 2025-11-05

  Licencia: MIT (ver licencia en el repositorio)

  ## Propósito

  Documento técnico que describe la arquitectura, las piezas clave y comportamientos relevantes para mantener y extender el sistema en la variante Maven. Incluye un resumen del mecanismo de integración con JavaFX y las reglas de uso de los archivos de configuración (`.properties`) que controlan las variantes de ejecución.

  ### Artefacto UML

  ![Diagrama UML](img/UML-Modelo-intellij.png)

  ## Visión general de la arquitectura

  El proyecto sigue una arquitectura en capas (Presentación / Lógica / Persistencia). Las piezas clave para desarrolladores son:

  - `Coordinador` — inicializa componentes (DAOs, Ciudad, servicios) y orquesta la aplicación.
  - `Calculo` — punto central para cálculo de recorridos; delega a distintas `EstrategiaBusqueda`.
  - `Ciudad` — almacena estructuras en memoria (paradas, líneas, índices).
  - `InterfazJavaFX` — controla la vista; usa un patrón estático / puente para que los controladores accedan al `Coordinador` inicializado por la aplicación.

  ## Archivos `.properties` (configuración y reglas de uso)

  El proyecto utiliza seis archivos de propiedades en `src/main/resources/` que controlan implementaciones, conexiones y comportamiento. Resumen y reglas de uso:

  1. `factory.properties` — Selección de Implementaciones
    - Define qué `INTERFAZ` usar (p.ej. `colectivo.interfaz.javafx.InterfazJavaFX` o `InterfazConsola`) y qué implementaciones de DAO utilizar (PostgreSQL / Secuencial / Aleatorio).
    - Regla: dejar exactamente UNA implementación de `INTERFAZ` sin comentar y un conjunto completo de DAOs (LINEA, PARADA, TRAMO) de la misma familia.

  2. `jdbc.properties` — Conexión a PostgreSQL y selección de schema (ciudad)
    - Contiene `url`, `usr`, `pwd` y una línea `schema=...` para seleccionar la ciudad cuando se usan DAOs PostgreSQL.
    - Regla: descomentar únicamente un `schema` por ejecución.

  3. `secuencial.properties` — Archivos `.txt` para DAOs Secuenciales
    - Lista las rutas/nombres de los archivos de texto (linea_XX.txt, parada_XX.txt, tramo_XX.txt, frecuencia_XX.txt).
    - Regla: descomentar las cuatro entradas que correspondan a la misma ciudad.

  4. `aleatorio.properties` — Rutas de archivos `.dat` para DAOs Aleatorios
    - Define ubicaciones de archivos binarios. La primera ejecución genera los `.dat` desde secuencial; usos posteriores los leen directamente.

  5. `config.properties` — Configuración general (idioma, rutas FXML/CSS)
    - Controla `language`, `country`, y las rutas `vista`, `estiloOscuro`, `estiloClaro` usadas por JavaFX.
    - Regla: descomentar solo el par `language/country` deseado (ej. `es/ES` o `en/US`).

  6. `log4j2.properties` — Logging
    - Controla niveles y appenders. Útil para diagnosticar (ajustar consola a DEBUG/INFO/WARN).

  Notas prácticas:
  - Cambios en `.properties` requieren reiniciar la aplicación.
  - Para ejecutar con JavaFX via Maven use `mvn javafx:run` (Maven se encarga de módulos). Para ejecución manual en Eclipse asegúrate de los VM args y módulos (ver `Manual_de_Usuario.md`).

  ## Clases y responsabilidades (resumen)

  ### `Coordinador`

  Responsabilidad: crear e inyectar dependencias, inicializar `Ciudad` y servicios, exponer APIs para la interfaz.

  Snippet (simplificado):

  ```java
  public class Coordinador {
    private final Ciudad ciudad;
    private final DAOFactory daoFactory;

    public Coordinador() {
     this.daoFactory = new DAOFactory();
     this.ciudad = new Ciudad(daoFactory);
     // inicializar caches y servicios
    }

    public void inicializarAplicacion() { /* ... */ }
  }
  ```

  ### `Calculo` y `EstrategiaBusqueda`

  `Calculo` orquesta las estrategias de búsqueda (directas, con transbordo, con caminata). Cada `EstrategiaBusqueda` implementa el contrato y puede probarse aisladamente.

  ## InterfazJavaFX — inyección de dependencias (implementación actual)

  JavaFX crea controladores por reflexión y los instancia internamente. En este proyecto la dependencia entre la aplicación y los controladores JavaFX se resuelve mediante una referencia publicada y una inyección explícita al cargar cada vista.

  Puntos clave (ver `InterfazJavaFX.java` y `Coordinador.java`):

  - `InterfazJavaFX` mantiene un campo estático `private static Coordinador coordinador;` y expone `setCoordinador(Coordinador)` usado por el servicio de interfaz para publicar la referencia antes de arrancar JavaFX.
  - Antes de llamar a `Application.launch()`, la aplicación (a través de `Coordinador` y `InterfazService`) establece la referencia estática en `InterfazJavaFX`.
  - Cuando JavaFX crea la instancia de `InterfazJavaFX` y ejecuta `start(Stage)`, el código carga el FXML, obtiene el controlador (`loader.getController()`) y llama `controlador.setCoordinador(coordinador);` con la referencia previamente publicada.

  Flujo resumido:

  1. Crear e inicializar `Coordinador` (configuración, `Ciudad`, `Calculo`).
  2. Crear `InterfazServiceImpl` y asignar el `Coordinador` con `interfazService.setCoordinador(this)`.
  3. `InterfazJavaFX.setCoordinador(...)` (o equivalente) publica la referencia en un campo estático.
  4. `InterfazJavaFX.iniciarInterfaz()` llama a `JavaFXLauncher.launchJavaFX(coordinador, this,configuracion, new String[] {})` que sera el encargado de tener el puente estatico de las dependencias y hacer el  `Application.launch()`. JavaFX crea la instancia de `Application` y ejecuta `start(...)`.
  5. `start(...)` inyecta la referencia en el controlador con `controlador.setCoordinador(coordinador)`.

  Consideraciones de diseño:

  - Implementado el patron Static Bridge propuesto por la catedra (Nuestra interfaz original creada por el factory ya no tiene `extends Application`
  - Es una solución explícita y práctica en entornos académicos; no introduce bibliotecas externas.
  - Si se requiere mayor testabilidad o escalabilidad, considerar `FXMLLoader.setControllerFactory(...)` o un contenedor DI para constructor-injection.

  ## Persistencia y DAO

  - El patrón DAO separa la fuente de datos. Existen implementaciones Secuencial (ficheros), Aleatorio (binario) y Postgresql.
  - En esta entrega, el modo Secuencial está configurado por defecto para Trelew (TW) en `secuencial.properties`; para Postgres se selecciona el `schema` correspondiente en `jdbc.properties`.

  ## Tests

  Los tests provistos por la cátedra están en `src/test/java` (paquete `colectivo.test`). Se ejecutan con Maven y están diseñados para correr con los datos de prueba configurados a Puerto Madryn en los recursos de test. Tests relevantes incluyen `TestCalcularRecorridoDAO` y `TestCalcularRecorrido`.


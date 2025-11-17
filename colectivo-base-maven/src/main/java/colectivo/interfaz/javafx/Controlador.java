package colectivo.interfaz.javafx;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Constantes;
import colectivo.controlador.Coordinador;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.Scene;
import colectivo.modelo.Parada;

/**
 * Controlador principal de la interfaz gráfica JavaFX de la aplicación.
 *
 * <p>Gestiona la interacción entre la vista FXML y la lógica de negocio, coordinando
 * múltiples gestores especializados para diferentes aspectos de la interfaz:</p>
 * <ul>
 *   <li>{@link GestorParadas}: gestión de ComboBox de paradas</li>
 *   <li>{@link GestorTiempos}: gestión de selectores de hora y minuto</li>
 *   <li>{@link GestorDias}: gestión del selector de día de la semana</li>
 *   <li>{@link GestorMapa}: visualización de rutas en mapa interactivo</li>
 *   <li>{@link GestorTemas}: cambio entre tema claro y oscuro</li>
 *   <li>{@link GestorTextos}: actualización de textos para internacionalización</li>
 *   <li>{@link ServicioBusqueda}: ejecución asíncrona de búsquedas de recorridos</li>
 * </ul>
 *
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Búsqueda de recorridos de transporte entre paradas</li>
 *   <li>Visualización de resultados en mapa interactivo o vista de texto</li>
 *   <li>Cambio de idioma (español/inglés)</li>
 *   <li>Cambio de tema visual (claro/oscuro)</li>
 *   <li>Autocompletado en selectores de paradas</li>
 *   <li>Intercambio rápido de origen y destino</li>
 *   <li>Selección de hora actual con un clic</li>
 * </ul>
 *
 * @see Coordinador
 * @see GestorParadas
 * @see GestorMapa
 * @see ServicioBusqueda
 */
public class Controlador {

    private static final Logger logger = LogManager.getLogger(Controlador.class);

    private Coordinador coordinador;
    private ResourceBundle resourceBundle;

    // Gestores
    private GestorParadas gestorParadas;
    private GestorTiempos gestorTiempos;
    private GestorDias gestorDias;
    private GestorMapa gestorMapa;
    private GestorTemas gestorTemas;
    private GestorTextos gestorTextos;
    private ServicioBusqueda servicioBusqueda;

    // FXML Components
    @FXML private Label mainTitle, secondaryTitle, labelOrigen, labelDestino, labelDia, labelLlegada, labelOpciones;
    @FXML private Button botonBuscar, botonIdioma, botonVista;
    @FXML private VBox mapVBox, textVBox;
    @FXML private Pane rootPane;
    @FXML private ComboBox<Integer> comboHora, comboMinuto;
    @FXML private ComboBox<Parada> comboOrigen, comboDestino;
    @FXML private ComboBox<String> comboDia;
    @FXML private ListView<String> optionsListView;
    @FXML private WebView webView;
    @FXML private TextArea resultsArea;
    @FXML private Label queryCompleted;

    /**
     * Establece el coordinador de la aplicación que gestiona la lógica de negocio.
     *
     * @param coordinador el {@link Coordinador} a utilizar
     */
    public void setCoordinador(Coordinador coordinador) {
        this.coordinador = coordinador;
    }

    /**
     * Obtiene el ResourceBundle para internacionalización.
     * Si no está cacheado, lo obtiene del coordinador.
     *
     * @return el {@link ResourceBundle} con las etiquetas traducidas
     */
    private ResourceBundle getResourceBundle() {
        if (resourceBundle == null && coordinador != null) {
            resourceBundle = coordinador.getConfiguracion().getResourceBundle();
        }
        return resourceBundle;
    }

    /**
     * Método de inicialización de JavaFX llamado automáticamente tras cargar el FXML.
     * Inicializa todos los gestores especializados.
     */
    @FXML
    public void initialize() {
        logger.debug("Iniciando controlador JavaFX");
        inicializarGestores();
    }

    /**
     * Inicializa todos los gestores especializados que manejan diferentes aspectos de la UI.
     * Crea instancias de gestores de paradas, tiempos, días, mapa, temas y textos.
     */
    private void inicializarGestores() {
        gestorParadas = new GestorParadas();
        gestorTiempos = new GestorTiempos();
        gestorDias = new GestorDias();
        gestorMapa = new GestorMapa(webView, Constantes.graphhopperApiKey);
        gestorMapa.setControlador(this);
        gestorTemas = new GestorTemas();
        gestorTextos = new GestorTextos();
    }

    /**
     * Inicializa los datos del controlador una vez que el coordinador está disponible.
     * Carga paradas, días, horas, temas y prepara el servicio de búsqueda.
     */
    public void initData() {
        if (coordinador != null) {
            logger.debug("Inicializando datos del controlador con coordinador");
            this.servicioBusqueda = new ServicioBusqueda(coordinador);

            inicializarComponentes();
            inicializarResultados();
            cargarTemas();

            logger.debug("Datos del controlador inicializados correctamente");
        } else {
            logger.warn("Coordinador es null, no se pueden inicializar datos");
        }
    }

    /**
     * Inicializa todos los componentes de la interfaz con datos desde el coordinador.
     * Configura ComboBox de paradas, días, horas y minutos, e inicializa el mapa.
     */
    private void inicializarComponentes() {

        Map<Integer, Parada> nombresParadas = coordinador.mapearParadas();
        gestorParadas.inicializar(comboOrigen, comboDestino, nombresParadas);

        ResourceBundle bundle = getResourceBundle();
        gestorDias.inicializar(comboDia, bundle);
        gestorTiempos.inicializarHoras(comboHora);
        gestorTiempos.inicializarMinutos(comboMinuto);
        gestorMapa.iniciar();
    }

    /**
     * Inicializa la configuración de visualización de resultados.
     * Establece la celda personalizada para mostrar opciones de recorrido.
     */
    private void inicializarResultados() {
        optionsListView.setCellFactory(listView -> new ResultadoListCell());
    }

    /**
     * Carga los archivos de estilos de temas (claro y oscuro) desde la configuración.
     * Prepara el gestor de temas con las rutas a los archivos CSS.
     */
    private void cargarTemas() {
        if (coordinador != null && coordinador.getConfiguracion() != null) {
            gestorTemas.inicializar(
                    coordinador.getConfiguracion().getArchivoEstiloOscuro(),
                    coordinador.getConfiguracion().getArchivoEstiloClaro()
            );
        }
    }

    /**
     * Maneja el evento de búsqueda de recorridos cuando el usuario presiona el botón buscar.
     *
     * <p>Valida que todos los campos estén completos (origen, destino, día, hora, minuto),
     * limpia resultados anteriores y ejecuta la búsqueda de forma asíncrona mediante
     * {@link ServicioBusqueda}. Actualiza la interfaz según el estado de la búsqueda
     * (en curso, exitosa o fallida).</p>
     */
    @FXML
    public void buscarRecorridos() {
        limpiarResultadosAnteriores();
        ResourceBundle bundle = getResourceBundle();

        Parada origen = comboOrigen.getValue();
        Parada destino = comboDestino.getValue();
        String diaStr = comboDia.getValue();
        Integer hora = comboHora.getValue();
        Integer minuto = comboMinuto.getValue();

        boolean inputsValidos = origen != null && destino != null && diaStr != null && hora != null && minuto != null;

        if (inputsValidos) {
            int numeroDia = Integer.parseInt(diaStr.split(" ")[0]);
            LocalTime time = LocalTime.of(hora, minuto);


            logger.debug("Iniciando búsqueda - Origen: {}, Destino: {}, Día: {}, Hora: {}:{}",
                    origen.getCodigo(), destino.getCodigo(), numeroDia, hora, minuto);

            Task<ResultadoFormateado> tarea = servicioBusqueda.crearTareaBusqueda(origen, destino, numeroDia, time);

            tarea.setOnSucceeded(event -> manejarBusquedaExitosa(tarea.getValue(), bundle));
            tarea.setOnFailed(event -> manejarBusquedaFallida(bundle));
            tarea.setOnRunning(event -> manejarBusquedaEnCurso(bundle));

            new Thread(tarea).start();
        } else {
            logger.warn("Búsqueda cancelada: campos incompletos");
            mostrarErrorValidacion(bundle);
        }

    }

    /**
     * Maneja el caso de una búsqueda exitosa.
     * Actualiza la interfaz con los resultados encontrados o muestra mensaje de ruta no encontrada.
     *
     * @param resultado el {@link ResultadoFormateado} con los datos de la búsqueda, o null si no hay rutas
     * @param bundle el {@link ResourceBundle} para mensajes internacionalizados
     */
    private void manejarBusquedaExitosa(ResultadoFormateado resultado, ResourceBundle bundle) {

        // CASO 1: La búsqueda funcionó, pero no se encontraron rutas.
        if (resultado == null) {
            logger.info("Búsqueda completada sin resultados");
            mostrarResultadosVacios(bundle);
            botonBuscar.setDisable(false);
        }
        else {
            logger.debug("Búsqueda completada exitosamente con resultados");
            if (optionsListView != null) {
                optionsListView.getItems().clear();
                optionsListView.getItems().addAll(resultado.resumen().split("\n"));
            }

            resultsArea.setText(resultado.logs());

            String queryCompletedMsg = (bundle != null) ? bundle.getString("search.queryCompleted")
                    : "Consulta completada";
            queryCompleted.setText(queryCompletedMsg);
            queryCompleted.getStyleClass().add("label-success");

            gestorMapa.dibujarRuta(resultado.jsonRuta());
        }
    }

    /**
     * Maneja el caso de una búsqueda fallida.
     * Muestra un mensaje de error al usuario y rehabilita el botón de búsqueda.
     *
     * @param bundle el {@link ResourceBundle} para mensajes internacionalizados
     */
    private void manejarBusquedaFallida(ResourceBundle bundle) {
        System.err.println("Error en búsqueda");
        mostrarErrorValidacion(bundle);
        botonBuscar.setDisable(false);
    }

    /**
     * Maneja el estado de búsqueda en curso.
     * Deshabilita el botón de búsqueda y muestra un mensaje de "Cargando..." al usuario.
     *
     * @param bundle el {@link ResourceBundle} para mensajes internacionalizados
     */
    private void manejarBusquedaEnCurso(ResourceBundle bundle) {
        botonBuscar.setDisable(true);
        queryCompleted.setText(bundle.getString("search.loading"));
    }

    /**
     * Notifica que el mapa terminó de cargar y dibujar la ruta.
     * Rehabilita el botón de búsqueda en el hilo de la UI de JavaFX.
     * Este metodo lo llama el JS del webview
     */
    public void notificarMapaTerminado() {
        Platform.runLater(() -> botonBuscar.setDisable(false));
    }

    /**
     * Limpia todos los resultados de búsquedas anteriores.
     * Borra el área de texto, la lista de opciones, el mensaje de estado y limpia la ruta del mapa.
     */
    private void limpiarResultadosAnteriores() {
        resultsArea.clear();
        optionsListView.getItems().clear();
        queryCompleted.setText("");
        gestorMapa.limpiarRuta();
    }

    /**
     * Muestra un mensaje de error de validación al usuario.
     * Se usa cuando faltan campos obligatorios para realizar la búsqueda.
     *
     * @param bundle el {@link ResourceBundle} para mensajes internacionalizados
     */
    private void mostrarErrorValidacion(ResourceBundle bundle) {
        String msg = bundle.getString("search.fillAllFields");
        optionsListView.getItems().add(msg);
    }

    /**
     * Muestra un mensaje indicando que no se encontraron rutas.
     * Se usa cuando la búsqueda fue exitosa pero no encontró recorridos válidos.
     *
     * @param bundle el {@link ResourceBundle} para mensajes internacionalizados
     */
    private void mostrarResultadosVacios(ResourceBundle bundle) {
        String msg = bundle.getString("search.noRoutesFound");
        optionsListView.getItems().add(msg);
    }

    /**
     * Cambia entre la vista de mapa y la vista de texto.
     * Alterna la visibilidad entre el contenedor del mapa interactivo y el área de texto.
     */
    @FXML private void cambiarVista() {
        boolean showingMap = textVBox.isVisible();
        textVBox.setVisible(!showingMap);
        mapVBox.setVisible(showingMap);
    }

    /**
     * Intercambia los valores de origen y destino en los selectores de paradas.
     * Solo realiza el intercambio si ambos valores están seleccionados.
     */
    @FXML private void swapParadas() {
        Parada origen = comboOrigen.getValue();
        Parada destino = comboDestino.getValue();
        if (origen != null && destino != null) {
            comboOrigen.setValue(destino);
            comboDestino.setValue(origen);
        }
    }

    /**
     * Establece la fecha y hora actual en los selectores.
     * Actualiza automáticamente los selectores de hora, minuto y día con los valores actuales del sistema.
     */
    @FXML private void tiempoActual() {
        LocalDateTime now = LocalDateTime.now();
        comboHora.setValue(now.getHour());
        comboMinuto.setValue(now.getMinute());
        int diaNumero = now.getDayOfWeek().getValue();
        comboDia.setValue(comboDia.getItems().get(diaNumero - 1));
    }

    /**
     * Cambia el tema visual de la aplicación entre claro y oscuro.
     * Delega al {@link GestorTemas} para alternar los archivos CSS aplicados.
     */
    @FXML private void cambiarTema() {
        Scene scene = rootPane.getScene();
        gestorTemas.cambiarTema(scene);
    }

    /**
     * Cambia el idioma de la interfaz entre español e inglés.
     * Detecta el idioma actual y solicita al coordinador el cambio al idioma alternativo.
     * Después actualiza todos los textos de la interfaz.
     */
    @FXML public void cambioIdioma() {
        if (coordinador != null) {
            ResourceBundle bundleActual = getResourceBundle();
            Locale localeActual = bundleActual != null ? bundleActual.getLocale() : Locale.getDefault();

            if (localeActual.getLanguage().equals("es")) {
                logger.debug("Cambiando idioma de español a inglés");
                coordinador.pedirCambioIdioma("en", "US");
            } else {
                logger.debug("Cambiando idioma de inglés a español");
                coordinador.pedirCambioIdioma("es", "ES");
            }
            actualizarTextos();
        } else {
            logger.warn("No se puede cambiar idioma: coordinador es null");
        }
    }

    /**
     * Actualiza todos los textos de la interfaz según el idioma actual.
     * Invalida el caché del ResourceBundle y recarga todas las etiquetas, botones y mensajes.
     * Preserva las selecciones de los controles donde es posible.
     */
    public void actualizarTextos() {
        logger.debug("Actualizando textos de la interfaz");
        this.resourceBundle = null;
        ResourceBundle bundle = getResourceBundle();

        if (bundle != null) {
            int diaIndex = comboDia.getSelectionModel().getSelectedIndex();
            gestorDias.inicializar(comboDia, bundle);
            if (diaIndex != -1 && diaIndex < comboDia.getItems().size()) {
                comboDia.setValue(comboDia.getItems().get(diaIndex));
            }

            gestorTextos.actualizar(bundle, rootPane.getScene(),
                    mainTitle, secondaryTitle, labelOrigen, labelDestino,
                    labelDia, labelLlegada, labelOpciones, botonVista,
                    botonIdioma, botonBuscar, comboOrigen, comboDestino,
                    comboDia, resultsArea);

            limpiarResultadosAnteriores();
            logger.debug("Textos actualizados correctamente");
        } else {
            logger.warn("No se puede actualizar textos: ResourceBundle es null");
        }
    }

    /**
     * Celda personalizada para mostrar resultados de búsqueda en el ListView.
     *
     * <p>Formatea cada opción de recorrido con un diseño de tarjeta (card) que incluye
     * un título y un subtítulo opcionales. El texto se divide por el separador " - ",
     * mostrando la primera parte como título principal y la segunda (si existe) como
     * subtítulo con formato diferenciado.</p>
     */
    private static class ResultadoListCell extends ListCell<String> {
        private final VBox card = new VBox();
        private final Label title = new Label();
        private final Label subtitle = new Label();

        /**
         * Construye una celda de resultado inicializando su estructura visual.
         * Configura las clases CSS y el layout de los componentes.
         */
        public ResultadoListCell() {
            card.getStyleClass().add("card");
            title.getStyleClass().add("card-title");
            subtitle.getStyleClass().add("card-subtitle");
            subtitle.managedProperty().bind(subtitle.visibleProperty());
            card.setSpacing(5);
            card.getChildren().addAll(title, subtitle);
        }

        /**
         * Actualiza el contenido de la celda según el elemento a mostrar.
         *
         * <p>Si el elemento no es null, lo divide por " - " y asigna las partes
         * al título y subtítulo de la tarjeta. El subtítulo solo se muestra si existe.</p>
         *
         * @param item el texto del elemento a mostrar
         * @param empty si la celda está vacía
         */
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                String[] partes = item.split(" - ", 2);
                title.setText(partes[0]);
                subtitle.setText(partes.length > 1 ? partes[1] : "");
                subtitle.setVisible(partes.length > 1 && !partes[1].isEmpty());
                setGraphic(card);
            }
        }
    }
}
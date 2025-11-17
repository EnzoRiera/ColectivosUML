package colectivo.interfaz.javafx;

import java.net.URL;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Gestor especializado para la visualización de mapas interactivos con rutas de transporte.
 *
 * <p>Administra un {@link WebView} que carga un archivo HTML con Leaflet.js y GraphHopper
 * para renderizar mapas interactivos. Se encarga de:</p>
 * <ul>
 *   <li>Cargar el HTML del mapa desde los recursos</li>
 *   <li>Inyectar la clave API de GraphHopper y la referencia al controlador Java</li>
 *   <li>Ejecutar scripts JavaScript para dibujar y limpiar rutas en el mapa</li>
 *   <li>Gestionar el ciclo de vida del WebView (carga, listo, error)</li>
 *   <li>Comunicación bidireccional entre Java y JavaScript mediante JSObject</li>
 * </ul>
 *
 * <p>El mapa utiliza la API de GraphHopper para calcular rutas y Leaflet.js para
 * la visualización interactiva. Las rutas se pasan en formato JSON desde Java
 * hacia JavaScript.</p>
 *
 * @see WebView
 * @see WebEngine
 * @see Controlador
 */
public class GestorMapa {
	private static final Logger logger = LogManager.getLogger(GestorMapa.class);

	/** WebView de JavaFX que contiene el mapa HTML/JavaScript. */
	private final WebView webView;

	/** Clave API de GraphHopper para cálculo de rutas. */
	private final String graphhopperApiKey;

	/** Indica si el WebView terminó de cargar y está listo para ejecutar scripts. */
	private boolean isWebViewReady;

	/** Referencia al controlador principal para comunicación JavaScript-Java. */
	private Controlador controlador;

	/**
	 * Construye un gestor de mapa con el WebView y la clave API especificados.
	 *
	 * @param webView el {@link WebView} donde se cargará el mapa
	 * @param apiKey la clave API de GraphHopper para routing
	 */
	public GestorMapa(WebView webView, String apiKey) {
		this.webView = webView;
		this.graphhopperApiKey = apiKey;
	}

	/**
	 * Establece el controlador para comunicación bidireccional con JavaScript.
	 * El controlador se expone como objeto 'javaApp' en el contexto JavaScript.
	 *
	 * @param controlador el {@link Controlador} a vincular
	 */
	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}

	/**
	 * Inicia el WebView cargando el HTML del mapa y configurando listeners.
	 *
	 * <p>Habilita JavaScript en el motor web, configura un listener para detectar
	 * cuando la página termina de cargar, e inicia la carga del archivo HTML.
	 * Una vez cargado exitosamente, inyecta la configuración JavaScript necesaria.</p>
	 */
	public void iniciar() {
		this.isWebViewReady = false;
		WebEngine webEngine = webView.getEngine();
		webEngine.setJavaScriptEnabled(true);

		webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED)
				inyectarJS(webEngine);

		});

		cargarHTML(webEngine);
	}

	/**
	 * Inyecta configuración JavaScript en la página cargada.
	 *
	 * <p>Realiza las siguientes operaciones:</p>
	 * <ul>
	 *   <li>Expone el controlador Java como 'window.javaApp' para llamadas desde JS</li>
	 *   <li>Establece la clave API de GraphHopper en 'window.GH_API_KEY'</li>
	 *   <li>Marca el WebView como listo para recibir comandos</li>
	 *   <li>Ejecuta la función inicial de dibujo de rutas</li>
	 * </ul>
	 *
	 * @param webEngine el {@link WebEngine} donde inyectar el código
	 */
	private void inyectarJS(WebEngine webEngine) {
		try {
			netscape.javascript.JSObject window = (netscape.javascript.JSObject) webEngine.executeScript("window");
			window.setMember("javaApp", controlador);

			webEngine.executeScript("window.GH_API_KEY = '" + graphhopperApiKey + "';");
			this.isWebViewReady = true;
			webEngine.executeScript("dibujarRutasDeColectivo();");
		} catch (Exception e) {
			this.isWebViewReady = false;
			logger.error("Error al inyectar JS" , e);
		}
	}

	/**
	 * Carga el archivo HTML del mapa desde los recursos de la aplicación.
	 *
	 * <p>Busca el archivo 'map.html' en la ubicación '/colectivo/interfaz/mapa/'.
	 * Si no se encuentra, muestra una página de error básica.</p>
	 *
	 * @param webEngine el {@link WebEngine} donde cargar el HTML
	 */
	private void cargarHTML(WebEngine webEngine) {
		URL url = getClass().getResource("/colectivo/interfaz/mapa/map.html");
		if (url != null) {
			webEngine.load(url.toExternalForm());
		} else {
			webEngine.loadContent("<html><body><h1>Error</h1><p>map.html no encontrado</p></body></html>");
		}
	}

	/**
	 * Dibuja una o más rutas en el mapa interactivo.
	 *
	 * <p>Recibe un JSON con las coordenadas de las rutas y ejecuta la función
	 * JavaScript 'dibujarRutasDeColectivo()' para renderizarlas en el mapa.
	 * El JSON se escapa adecuadamente antes de pasarlo a JavaScript.</p>
	 *
	 * <p>Solo ejecuta si el WebView está listo. Si no lo está, registra una advertencia.</p>
	 *
	 * @param jsonRutas cadena JSON con las coordenadas de las rutas a dibujar
	 */
	public void dibujarRuta(String jsonRutas) {
		if (isWebViewReady) {
            try {
                String jsonEscapado = jsonRutas.replace("\\", "\\\\").replace("'", "\\'");
                String script = String.format("dibujarRutasDeColectivo('%s');", jsonEscapado);
                webView.getEngine().executeScript(script);
            } catch (Exception e) {
                logger.error("Error al dibujar ruta", e);
            }

		} else {
            logger.warn("WebView no está listo para dibujar ruta");
		}
	}

	/**
	 * Limpia todas las rutas dibujadas en el mapa.
	 *
	 * <p>Ejecuta la función JavaScript 'limpiarRuta()' para remover todas las
	 * polilíneas y marcadores del mapa, dejándolo en su estado inicial.</p>
	 *
	 * <p>Solo ejecuta si el WebView está listo. Si no lo está, registra una advertencia.</p>
	 */
	public void limpiarRuta() {
		if (isWebViewReady) {
			try {
				webView.getEngine().executeScript("limpiarRuta()");
			} catch (Exception e) {
			    logger.error("Error al limpiar mapa", e);
			}
		} else {
            logger.warn("WebView no está listo");
        }


	}
}

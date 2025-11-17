package colectivo.interfaz.javafx;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.controlador.Coordinador;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXLauncher extends Application {

	private static final Logger logger = LogManager.getLogger(JavaFXLauncher.class);

	private static Coordinador coordinador;
	private static Configuracion configuracion;
	private static InterfazJavaFX uiInstancia;

	public JavaFXLauncher() {
		System.out.println("JavaFXLauncher-Constructor vacío llamado por JavaFX");
		System.out.println("Hash de esta instancia: " + this.hashCode());
	}

	public JavaFXLauncher(String parametro) {
		this(); // Llama al constructor vacío
		System.out.println(" Constructor con parámetro: " + parametro);
	}

	public static void launchJavaFX(Coordinador coordinador, InterfazJavaFX uiInstancia, Configuracion configuracion,
			String[] args) {
		System.out.println("CREANDO PUENTE entre instancias...");

		// 1. Almacenar referencias en estáticos TEMPORALES
		JavaFXLauncher.coordinador = coordinador;
		JavaFXLauncher.configuracion = configuracion;
		JavaFXLauncher.uiInstancia = uiInstancia;
		

		// 2. Lanzar JavaFX (creará nueva instancia)
		launch(args); // ← BLOQUEANTE
//			launch();
	}

	/**
	 * Inicia la aplicación JavaFX cargando la vista, estilos y configurando la
	 * ventana.
	 *
	 * <p>
	 * Proceso de inicialización:
	 * </p>
	 * <ol>
	 * <li>Obtiene la configuración singleton</li>
	 * <li>Carga el archivo FXML con su ResourceBundle para i18n</li>
	 * <li>Obtiene el controlador del FXML</li>
	 * <li>Inyecta el coordinador en el controlador</li>
	 * <li>Inicializa los datos del controlador</li>
	 * <li>Crea la escena y aplica la hoja de estilos</li>
	 * <li>Configura y muestra la ventana en pantalla completa</li>
	 * </ol>
	 *
	 * @param stage el {@link Stage} principal de la aplicación JavaFX
	 * @throws Exception si ocurre un error al cargar FXML, CSS o inicializar
	 *                   componentes
	 */
	@Override
	public void start(Stage stage) throws Exception {
		try {

//			configuracion = Configuracion.getConfiguracion();

			URL vistaUrl = getClass().getResource(configuracion.getArchivoVista());
			if (vistaUrl == null) {
				logger.error("No se encontró el archivo FXML: {}", configuracion.getArchivoVista());
				throw new IllegalStateException("No se encontró el archivo FXML: " + configuracion.getArchivoVista());
			}
			FXMLLoader loader = new FXMLLoader(vistaUrl, configuracion.getResourceBundle());
			Parent root = loader.load();

			Controlador controlador = loader.getController();

			controlador.setCoordinador(coordinador);
			controlador.initData();

			Scene scene = new Scene(root);
			URL estiloUrl = getClass().getResource(configuracion.getArchivoEstiloOscuro());
			if (estiloUrl == null) {
				logger.error("No se encontró el archivo de estilos: {}", configuracion.getArchivoEstiloOscuro());
				throw new IllegalStateException(
						"No se encontró el archivo de estilos: " + configuracion.getArchivoEstiloOscuro());
			}
			scene.getStylesheets().add(estiloUrl.toExternalForm());

			ResourceBundle bundle = configuracion.getResourceBundle();
			String tituloVentana = bundle != null && bundle.containsKey("title.window")
					? bundle.getString("title.window")
					: "Simulación Colectivo";

			stage.setTitle(tituloVentana);
			stage.setScene(scene);
			stage.setFullScreen(true);
			stage.show();
		} catch (Exception e) {
			logger.error("Error al iniciar la interfaz JavaFX.", e);
			throw e;
		}
	}

	/**
	 * Detiene la aplicación JavaFX y libera recursos.
	 *
	 * <p>
	 * Se llama automáticamente cuando se cierra la ventana principal. Registra
	 * cualquier error que ocurra durante el cierre.
	 * </p>
	 *
	 * @throws Exception si ocurre un error al detener la aplicación
	 */
	@Override
	public void stop() throws Exception {
		try {
			super.stop();
			JavaFXLauncher.coordinador = null;
			JavaFXLauncher.configuracion = null;
			JavaFXLauncher.uiInstancia = null;
		} catch (Exception e) {
			logger.error("Error al detener la aplicación JavaFX.", e);
			throw e;
		}
	}

}

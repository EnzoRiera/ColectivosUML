package colectivo.interfaz.javafx;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.stage.Stage;

import colectivo.aplicacion.Configuracion;
import colectivo.controlador.Coordinador;
import colectivo.interfaz.Interfaz;

/**
 * Implementación de la interfaz de usuario basada en JavaFX.
 *
 * <p>Esta clase extiende {@link Application} de JavaFX e implementa {@link Interfaz}
 * para proporcionar una interfaz gráfica completa y moderna. Se encarga de:</p>
 * <ul>
 *   <li>Cargar el archivo FXML de la vista principal</li>
 *   <li>Aplicar hojas de estilo CSS para temas visuales</li>
 *   <li>Inicializar el controlador con el coordinador de la aplicación</li>
 *   <li>Configurar la ventana principal en modo pantalla completa</li>
 *   <li>Gestionar el ciclo de vida de la aplicación JavaFX</li>
 * </ul>
 *
 * <p>La configuración se obtiene del singleton {@link Configuracion}, que proporciona
 * las rutas a los archivos FXML, CSS y los ResourceBundle para internacionalización.</p>
 *
 * <p>El coordinador se pasa de forma estática ya que JavaFX requiere un constructor
 * sin parámetros para la clase Application.</p>
 *
 * @see Application
 * @see Interfaz
 * @see Controlador
 * @see Configuracion
 */
public class InterfazJavaFX implements Interfaz {

	private static final Logger logger = LogManager.getLogger(InterfazJavaFX.class);

	private Configuracion configuracion;
	private Coordinador coordinador;

	/**
	 * Establece el coordinador de la aplicación de forma estática.
	 *
	 * <p>Necesario porque JavaFX requiere un constructor sin parámetros.
	 * El coordinador se almacena estáticamente para ser inyectado en el
	 * controlador durante el método {@link #start(Stage)}.</p>
	 *
	 * @param coordinador el {@link Coordinador} a utilizar
	 */
	@Override
	public void setCoordinador(Coordinador coordinador) {
		this.coordinador = coordinador;
	}

	/**
	 * Inicia la interfaz JavaFX lanzando la aplicación.
	 *
	 * <p>Llama al método launch() de JavaFX que iniciará el hilo
	 * de la aplicación y llamará a {@link #start(Stage)} cuando esté listo.</p>
	 *
	 * <p>Este método es bloqueante y retorna solo cuando la aplicación se cierra.</p>
	 *
	 * @throws RuntimeException si ocurre un error al lanzar la aplicación
	 */
	@Override
	public void iniciarInterfaz() {
		try {
			configuracion = Configuracion.getConfiguracion();
			JavaFXLauncher.launchJavaFX(coordinador, this,configuracion, new String[] {});
		} catch (Exception e) {
			logger.error("Error al lanzar la interfaz JavaFX.", e);
			throw e;
		}
	}
}

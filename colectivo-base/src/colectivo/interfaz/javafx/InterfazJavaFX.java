package colectivo.interfaz.javafx;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.aplicacion.Constantes;
import colectivo.aplicacion.Coordinador;
import colectivo.interfaz.Interfaz;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application class for the bus simulation interface. Uses
 * ApplicationContext for dependency injection to bridge the gap between
 * application initialization and JavaFX lifecycle.
 */
public class InterfazJavaFX extends Application implements Interfaz {

	private static final Logger logger = LogManager.getLogger(InterfazJavaFX.class);

	private Configuracion configuracion;
	private static Coordinador coordinador;

	@Override
	public void start(Stage stage) throws Exception {
		try {
			// Load singleton Configuracion -> to obtain view file, stylesheet and labels
			configuracion = Configuracion.getConfiguracion();

			// Load FXML and initialize controller
			URL vistaUrl = getClass().getResource(configuracion.getArchivoVista());
			if (vistaUrl == null) {
				logger.error("No se encontró el archivo FXML: " + configuracion.getArchivoVista());
				throw new IllegalStateException("No se encontró el archivo FXML: " + configuracion.getArchivoVista());
			}
			FXMLLoader loader = new FXMLLoader(vistaUrl, configuracion.getResourceBundle());
			Parent root = loader.load();

			Controlador controlador = loader.getController();

			// Initialize the system - inject coordinator and initialize data
			controlador.setCoordinador(coordinador);
			controlador.initData();

			// Create scene with stylesheet
			Scene scene = new Scene(root, Constantes.APP_WIDTH, Constantes.APP_HEIGHT);
			URL estiloUrl = getClass().getResource(configuracion.getArchivoEstilo());
			if (estiloUrl == null) {
				logger.error("No se encontró el archivo de estilos: " + configuracion.getArchivoEstilo());
				throw new IllegalStateException(
						"No se encontró el archivo de estilos: " + configuracion.getArchivoEstilo());
			}
			scene.getStylesheets().add(estiloUrl.toExternalForm());

			// Set window title from the ResourceBundle if present, fallback to a default
			ResourceBundle bundle = configuracion.getResourceBundle();
			String tituloVentana = bundle != null && bundle.containsKey("title.window")
					? bundle.getString("title.window")
					: "Simulación Colectivo";

			// Show the stage
			stage.setTitle(tituloVentana);
			stage.setScene(scene);
			stage.show();
		} catch (Exception e) {
			logger.error("Error al iniciar la interfaz JavaFX.", e);
			throw e;
		}
	}

	@Override
	public void stop() throws Exception {
        try {
            super.stop();
        } catch (Exception e) {
            logger.error("Error al detener la aplicación JavaFX.", e);
            throw e;
        }
	}

	@Override
	public void setCoordinador(Coordinador coordinador) {
		InterfazJavaFX.coordinador = coordinador;
	}

	@Override
	public void iniciarInterfaz() {
        try {
            launch();
        } catch (Exception e) {
            logger.error("Error al lanzar la aplicación JavaFX.", e);
            throw e;
        }
    }
}

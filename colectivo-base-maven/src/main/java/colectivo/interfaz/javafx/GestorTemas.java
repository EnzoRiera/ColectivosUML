package colectivo.interfaz.javafx;

import javafx.scene.Scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Gestor especializado para el cambio de temas visuales de la aplicación.
 *
 * <p>Permite alternar entre tema claro y tema oscuro modificando dinámicamente
 * las hojas de estilo CSS de la escena de JavaFX. Mantiene el estado del tema
 * actual y aplica los archivos CSS correspondientes según la preferencia del usuario.</p>
 *
 * <p>Los temas se definen mediante rutas a archivos CSS que deben ser inicializados
 * antes de poder cambiar entre ellos. Por defecto, la aplicación inicia en modo oscuro.</p>
 *
 * @see Scene
 */
public class GestorTemas {
	private static final Logger logger = LogManager.getLogger(GestorTemas.class);

    /** Ruta al archivo CSS del tema oscuro. */
    private String temaOscuro;

    /** Ruta al archivo CSS del tema claro. */
    private String temaClaro;

    /** Indica si actualmente está activo el modo oscuro. Por defecto true. */
    private boolean isDarkMode = true;

    /**
     * Inicializa el gestor con las rutas a los archivos CSS de los temas.
     *
     * <p>Este método debe ser llamado antes de usar {@link #cambiarTema(Scene)}
     * para establecer las rutas a los archivos de estilos.</p>
     *
     * @param temaOscuro ruta al archivo CSS del tema oscuro
     * @param temaClaro ruta al archivo CSS del tema claro
     */
    public void inicializar(String temaOscuro, String temaClaro) {
        this.temaOscuro = temaOscuro;
        this.temaClaro = temaClaro;
    }

    /**
     * Alterna entre el tema claro y el tema oscuro.
     *
     * <p>Si está en modo oscuro, cambia al tema claro removiendo el CSS oscuro
     * y añadiendo el CSS claro. Si está en modo claro, hace lo inverso.
     * El estado del modo actual se actualiza automáticamente tras cada cambio.</p>
     *
     * <p>Si la escena es null o los temas no han sido inicializados, registra un error
     * y no realiza ningún cambio.</p>
     *
     * @param scene la {@link Scene} de JavaFX a la que aplicar el cambio de tema
     */
    public void cambiarTema(Scene scene) {
        if (scene != null ) {
            if (isDarkMode) {
                scene.getStylesheets().remove(temaOscuro);
                scene.getStylesheets().add(temaClaro);
            } else {
                scene.getStylesheets().remove(temaClaro);
                scene.getStylesheets().add(temaOscuro);
            }

            isDarkMode = !isDarkMode;
        } else {
            logger.error("Configuración de temas no inicializada");
        }

    }
}

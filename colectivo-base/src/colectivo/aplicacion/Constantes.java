package colectivo.aplicacion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class that centralizes the application-wide constants.
 *
 * <p>
 * This class provides named constants used across the application for modes,
 * encoding and minimal expected CSV/line fields. It is not instantiable.
 * </p>
 *
 * @author Miyen
 * @author Enzo
 * @author Agustin
 * @version 1.0
 * @since 1.0
 */
public final class Constantes {

    private static final Logger logger = LogManager.getLogger(Configuracion.class);

    /**
     * Alto predeterminado de la ventana principal de la aplicación.
     */
    public static final double APP_HEIGHT = 800.0;
    
    /**
     * Ancho predeterminado de la ventana principal de la aplicación.
     */
    public static final double APP_WIDTH = 1000.0;
    
    /**
     * Mode constant representing travel by walking.
     */
    public static final int CAMINANDO = 2;
    
    /**
     * Minimum number of fields expected when parsing a frequency record.
     */
    public static final int CAMPOS_MINIMOS_FRECUENCIA = 3;
    
    /**
     * Minimum number of fields expected when parsing a line (`Linea`) record.
     */
    public static final int CAMPOS_MINIMOS_LINEA = 3;
    
    /**
     * Minimum number of fields expected when parsing a stop (`Parada`) record.
     */
    public static final int CAMPOS_MINIMOS_PARADA = 4;

    /**
     * Minimum number of fields expected when parsing a segment (`Tramo`) record.
     */
    public static final int CAMPOS_MINIMOS_TRAMO = 4;
    
    /**
     * Mode constant representing travel by bus/colectivo.
     */
    public static final int COLECTIVO = 1;

    /**
     * Character encoding used when reading/writing application files.
     * Value corresponds to ISO-8859-1 (Latin-1).
     */
    public static final String ENCODING = "ISO-8859-1";

    /**
     * Regular expression used to split input lines by semicolon with optional surrounding whitespace.
     * Example: "a ; b; c" -> split into \["a","b","c"\]
     */
    public static final String SEPARADOR = "\\s*;\\s*";

    /**
     * Private constructor to prevent instantiation.
     */
    private Constantes() {
        logger.error("Attempt to instantiate utility class Constantes");
        throw new AssertionError("Utility class - should not be instantiated");
    }
    
    public static final String graphhopperApiKey = "055d486d-ec25-4772-b1fa-5818b58714d7";
}

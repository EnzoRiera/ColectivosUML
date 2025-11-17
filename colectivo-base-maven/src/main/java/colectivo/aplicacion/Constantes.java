package colectivo.aplicacion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase de utilidad que centraliza las constantes de la aplicación.
 * <p>
 * Esta clase proporciona constantes nombradas utilizadas en toda la aplicación para
 * modos de transporte, codificación de caracteres, validación de datos CSV y
 * claves de API externas.
 * </p>
 * <p>
 * Esta es una clase de utilidad y no puede ser instanciada. El constructor privado
 * lanza un {@link AssertionError} si se intenta crear una instancia mediante reflexión.
 * </p>
 * <p>
 * <b>Categorías de constantes:</b>
 * <ul>
 *   <li><b>Modos de transporte:</b> Tipos de tramos (caminando)</li>
 *   <li><b>Codificación:</b> Charset para lectura/escritura de archivos</li>
 *   <li><b>Validación CSV:</b> Número mínimo de campos esperados en registros</li>
 *   <li><b>Parseo:</b> Expresiones regulares para separadores</li>
 *   <li><b>APIs externas:</b> Claves de autenticación</li>
 * </ul>
 * </p>
 *
 * @see java.nio.charset.Charset
 * @see java.util.regex.Pattern
 */
public final class Constantes {

    private static final Logger logger = LogManager.getLogger(Constantes.class);

    /**
     * Constante que representa el modo de viaje caminando.
     * <p>
     * Esta constante se utiliza para identificar tramos que se recorren a pie,
     * diferenciándolos de los tramos en colectivo (valor 1). Los tramos de tipo
     * CAMINANDO son típicamente bidireccionales.
     * </p>
     *
     * @see colectivo.modelo.Tramo
     */
    public static final int CAMINANDO = 2;
    
    /**
     * Codificación de caracteres utilizada al leer/escribir archivos de la aplicación.
     * <p>
     * El valor corresponde a ISO-8859-1 (Latin-1), que es compatible con caracteres
     * del español y otros idiomas europeos occidentales.
     * </p>
     *
     * @see java.nio.charset.Charset
     * @see java.nio.charset.StandardCharsets#ISO_8859_1
     */
    public static final String ENCODING = "ISO-8859-1";
    
    /**
     * Número mínimo de campos esperados al parsear un registro de parada.
     * <p>
     * Los registros de parada en archivos CSV/texto deben contener al menos 4 campos:
     * código, dirección, latitud y longitud.
     * </p>
     *
     * @see colectivo.modelo.Parada
     * @see colectivo.datos.CargarDatos
     */
    public static final int CAMPOS_MINIMOS_PARADA = 4;
    
    /**
     * Número mínimo de campos esperados al parsear un registro de tramo.
     * <p>
     * Los registros de tramo en archivos CSV/texto deben contener al menos 4 campos:
     * código de parada inicio, código de parada fin, tiempo y tipo.
     * </p>
     *
     * @see colectivo.modelo.Tramo
     * @see colectivo.datos.CargarDatos
     */
    public static final int CAMPOS_MINIMOS_TRAMO = 4;
    
    /**
     * Número mínimo de campos esperados al parsear un registro de línea.
     * <p>
     * Los registros de línea en archivos CSV/texto deben contener al menos 3 campos:
     * código, nombre y códigos de paradas separados.
     * </p>
     *
     * @see colectivo.modelo.Linea
     * @see colectivo.datos.CargarDatos
     */
    public static final int CAMPOS_MINIMOS_LINEA = 3;

    /**
     * Número mínimo de campos esperados al parsear un registro de frecuencia.
     * <p>
     * Los registros de frecuencia en archivos CSV/texto deben contener al menos 3 campos:
     * código de línea, día de la semana y hora.
     * </p>
     *
     * @see colectivo.modelo.Linea#agregarFrecuencia(int, java.time.LocalTime)
     * @see colectivo.datos.CargarDatos
     */
    public static final int CAMPOS_MINIMOS_FRECUENCIA = 3;
    
    /**
     * Expresión regular utilizada para separar campos de entrada por punto y coma.
     * <p>
     * El patrón {@code "\\s*;\\s*"} separa por punto y coma permitiendo espacios
     * en blanco opcionales antes y después del separador.
     * </p>
     * <p>
     * <b>Ejemplo:</b> La cadena {@code "a b ; c"} se divide en {@code ["a b", "c"]}
     * </p>
     *
     * @see String#split(String)
     * @see java.util.regex.Pattern
     */
    public static final String SEPARADOR = "\\s*;\\s*";

    /**
     * Clave de API para el servicio GraphHopper.
     * <p>
     * GraphHopper es un servicio de geocodificación y enrutamiento utilizado
     * para calcular distancias y rutas entre ubicaciones geográficas.
     * Esta clave permite autenticarse con el servicio externo.
     * @see <a href="https://www.graphhopper.com/">GraphHopper API</a>
     */
    public static final String graphhopperApiKey = "33b6368d-f00c-4ccc-8c55-56a2f50c0629";

    /**
     * Constructor privado para prevenir la instanciación.
     * <p>
     * Esta es una clase de utilidad que solo contiene constantes estáticas,
     * por lo que no debe ser instanciada. Si se intenta crear una instancia
     * mediante reflexión u otros medios, se registrará un error en el log
     * y se lanzará un {@link AssertionError}.
     * </p>
     *
     * @throws AssertionError siempre que se invoque este constructor
     */
    private Constantes() {
        logger.error("Attempt to instantiate utility class Constantes");
        throw new AssertionError("Utility class - should not be instantiated");
    }

}

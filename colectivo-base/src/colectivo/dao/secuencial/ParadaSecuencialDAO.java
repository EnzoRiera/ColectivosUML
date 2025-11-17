package colectivo.dao.secuencial;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Constantes;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

/**
 * Sequential DAO implementation that reads {@link Parada} data from a text
 * file.
 *
 * <p>
 * The expected file format is a separator-delimited record per line:
 * {@code codigo;direccion;latitud;longitud}. Example:
 * {@code 1;1 De Marzo, 405;-42.766285;-65.040768;}
 * </p>
 *
 * <p>
 * Loading is performed lazily on the first call to {@link #buscarTodos()} and
 * cached for subsequent calls. Parsing errors are reported to
 * {@code System.err} but do not stop processing of other entries.
 * </p>
 *
 * @author Miyo
 * @version 1.0
 * @since 1.0
 */
public class ParadaSecuencialDAO implements ParadaDAO {

    private static final Logger logger = LogManager.getLogger(ParadaSecuencialDAO.class);

    private String rutaArchivo;
    private boolean actualizar;
    Map<Integer, Parada> paradas = new TreeMap<Integer, Parada>();

    /**
     * Constructs a new {@code ParadaSecuencialDAO}.
     *
     * <p>
     * The constructor reads the resource bundle {@code secuencial} to obtain the
     * path to the stops file and sets the internal flag so data is loaded on
     * demand.
     * </p>
     */
    public ParadaSecuencialDAO() {
        ResourceBundle rb = ResourceBundle.getBundle("secuencial");
        rutaArchivo = rb.getString("parada");
        actualizar = true;
    }

    /**
     * Returns all loaded {@link Parada} instances.
     *
     * <p>
     * The first invocation triggers a one-time load of the configured file;
     * subsequent calls return the cached map. Any exception thrown during loading
     * is printed to {@code System.err} and propagated as appropriate by the
     * internal loader.
     * </p>
     *
     * @return a map from stop code to {@link Parada} instances
     */
    @Override
    public Map<Integer, Parada> buscarTodos() {
        if (actualizar) {
            try {
                paradas = leerDeArchivo(rutaArchivo);
                actualizar = false;
            } catch (Exception e) {
                logger.error("Error al cargar paradas desde el archivo secuencial: " + rutaArchivo, e);
            }
        }
        return paradas;
    }

    /**
     * Reads stops from the specified file and returns a map of parsed {@link Parada} objects.
     *
     * <p>
     * Each non-empty line is split using {@link Constantes#SEPARADOR}. Lines with
     * insufficient fields are ignored. Numeric parsing errors for code, latitude,
     * or longitude are reported to {@code System.err} and the offending line is skipped.
     * </p>
     *
     * @param nombreArchivo path to the stops file
     * @return a map from stop code to {@link Parada} instances (may be empty)
     * @throws FileNotFoundException if the file cannot be found
     * @throws NoSuchElementException if an unexpected scanner error occurs
     * @throws IllegalStateException if the scanner is closed unexpectedly
     * @throws Exception for other IO or parsing errors
     */
    private Map<Integer, Parada> leerDeArchivo(String nombreArchivo) throws Exception {

        Parada paradaActual;

        try (Scanner read = new Scanner(new File(nombreArchivo), Constantes.ENCODING)) {
            while (read.hasNextLine()) {
                String linea = read.nextLine().trim();
                if (!linea.isEmpty()) {

                    String[] partes = linea.split(Constantes.SEPARADOR);
                    if (partes.length >= Constantes.CAMPOS_MINIMOS_PARADA) {
                        try {
                            int codigo = Integer.parseInt(partes[0].strip());
                            String nombre = partes[1].strip();

                            // Normalizar separador decimal: reemplazar coma por punto
                            String latStr = partes[2].strip().replace(',', '.');
                            String lonStr = partes[3].strip().replace(',', '.');

                            // Ahora parsear como double
                            double latitud = Double.parseDouble(latStr);
                            double longitud = Double.parseDouble(lonStr);

                            // Crear y agregar la parada al mapa
                            paradaActual = new Parada(codigo, nombre, latitud, longitud);
                            paradas.put(codigo, paradaActual);
                        } catch (NumberFormatException nfe) {
                            logger.warn("Advertencia: formato numérico inválido en línea: " + linea + " - " + nfe.getMessage(), nfe);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + nombreArchivo, e);
            throw e;
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Error leyendo archivo: " + nombreArchivo, e);
            throw e;
        }

        return paradas;
    }
}

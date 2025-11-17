package colectivo.dao.secuencial;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.aplicacion.Constantes;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

/**
 * Sequential DAO implementation that reads {@link Tramo} (segment) data from a
 * text file and builds an in-memory map of tramos.
 *
 * <p>
 * The expected file format for each line is:
 * {@code codigoParadaOrigen;codigoParadaDestino;tiempo;tipo} Example:
 * {@code 88;97;60;1;} Where {@code tipo} values: {@code 1 = COLECTIVO},
 * {@code 2 = CAMINANDO}.
 * </p>
 *
 * <p>
 * Loading is performed lazily on the first call to {@link #buscarTodos()} and
 * the result is cached for subsequent calls. This DAO reuses {@link Parada}
 * instances provided by an injected {@link ParadaDAO} to avoid duplicating stop
 * objects.
 * </p>
 *
 * @author Miyo
 * @version 1.0
 * @since 1.0
 * @see colectivo.dao.ParadaDAO
 * @see colectivo.modelo.Tramo
 */
public class TramoSecuencialDAO implements TramoDAO {

    private static final Logger logger = LogManager.getLogger(TramoSecuencialDAO.class);

    /**
     * Path to the tramos input file obtained from the {@code secuencial} resource
     * bundle.
     */
    private String rutaArchivo;

    /**
     * Shared ParadaDAO used to obtain {@link Parada} instances referenced by
     * tramos.
     */
    private final ParadaDAO paradaDAO;

    /**
     * Cached map of paradas loaded from the injected {@link ParadaDAO}. Keyed by
     * parada code.
     */
    private Map<Integer, Parada> paradas;

    /**
     * In-memory map of tramos loaded from file. Keys are generated as
     * {@code codigoInicio-codigoFin-tipo}.
     */
    Map<String, Tramo> tramos = new HashMap<>();

    /**
     * Flag that controls lazy one-time loading. When {@code true} the next call to
     * {@link #buscarTodos()} will reload from file.
     */
    private boolean actualizar;

    /**
     * Default constructor that obtains a {@link ParadaDAO} from the {@link Factory}
     * using the key {@code "PARADA"} and delegates to the parameterized
     * constructor.
     */
    public TramoSecuencialDAO() {
        // Obtener ParadaDAO desde Factory (singleton compartido)
        this(Factory.getInstancia("PARADA", ParadaDAO.class));
    }

    /**
     * Constructor that accepts a shared {@link ParadaDAO}.
     *
     * <p>
     * Injecting a shared {@code ParadaDAO} allows multiple DAOs to reuse the same
     * {@link Parada} instances so stops are not duplicated in memory.
     * </p>
     *
     * @param paradaDAO shared {@code ParadaDAO} used to load and reuse stop
     *                  instances
     */
    public TramoSecuencialDAO(ParadaDAO paradaDAO) {
        this.paradaDAO = paradaDAO;
        this.paradas = cargarParadas();
        ResourceBundle rb = ResourceBundle.getBundle("secuencial");
        rutaArchivo = rb.getString("tramo");
        actualizar = true;
    }

    /**
     * Loads and returns the map of {@link Parada} instances from the injected DAO.
     *
     * <p>
     * Any exception thrown by the underlying {@code paradaDAO} is caught and
     * printed to {@code System.err}; an empty map is returned in case of failure.
     * </p>
     *
     * @return a map from parada code to {@link Parada} instances (never
     *         {@code null})
     */
    private Map<Integer, Parada> cargarParadas() {
        Map<Integer, Parada> paradas = new TreeMap<Integer, Parada>();
        try {
            paradas = this.paradaDAO.buscarTodos();
        } catch (Exception e) {
            logger.error("Error al cargar paradas desde ParadaDAO.", e);
        }
        return paradas;
    }

    /**
     * Returns all loaded {@link Tramo} objects.
     *
     * <p>
     * The first invocation triggers a one-time load of the tramos file; subsequent
     * calls return the cached map. Any exceptions during loading are printed to
     * {@code System.err}.
     * </p>
     *
     * @return a map from generated tramo key to {@link Tramo} instances
     */
    @Override
    public Map<String, Tramo> buscarTodos() {
        if (actualizar) {
            try {
                tramos = leerDeArchivo(rutaArchivo);
                actualizar = false;
            } catch (Exception e) {
                logger.error("Error al cargar tramos desde el archivo: " + rutaArchivo, e);
            }
        }
        return tramos;
    }

    /**
     * Reads tramos from the specified file and constructs {@link Tramo} instances.
     *
     * <p>
     * Each non-empty line is split using {@link Constantes#SEPARADOR}. Lines with
     * insufficient fields are ignored. The method looks up origin and destination
     * {@link Parada} instances by code; tramos referencing missing paradas are
     * skipped. For walking tramos (tipo == {@code Constantes.CAMINANDO}) an inverse
     * tramo is also created to allow bidirectional walking routes.
     * </p>
     *
     * @param nombreArchivo path to the tramos file
     * @return a map from tramo key to {@link Tramo} instances
     * @throws FileNotFoundException  if the file cannot be found
     * @throws NoSuchElementException if an unexpected scanner error occurs
     * @throws IllegalStateException  if the scanner is closed unexpectedly
     * @throws Exception              for other IO or parsing errors
     */
    private Map<String, Tramo> leerDeArchivo(String nombreArchivo) throws Exception {
        tramos.clear();

        try (Scanner read = new Scanner(new File(nombreArchivo), Constantes.ENCODING)) {
            while (read.hasNextLine()) {

                String linea = read.nextLine().trim();

                if (!linea.isEmpty()) {

                    String[] partes = linea.split(Constantes.SEPARADOR);
                    if (partes.length >= Constantes.CAMPOS_MINIMOS_TRAMO) {

                        // Leer datos del tramo
                        int codigoInicio = Integer.parseInt(partes[0].trim());
                        int codigoFin = Integer.parseInt(partes[1].trim());

                        // Obtener las paradas desde el mapa
                        Parada paradaInicio = paradas.get(codigoInicio);
                        Parada paradaFin = paradas.get(codigoFin);

                        int tiempo = Integer.parseInt(partes[2].trim());
                        int tipo = Integer.parseInt(partes[3].trim());

                        if (paradaInicio != null && paradaFin != null) {
                            // Crear y agregar el tramo al mapa
                            // Los tramos tipo 1 son unidireccionales (colectivo)
                            Tramo tramo = new Tramo(paradaInicio, paradaFin, tiempo, tipo);
                            String claveTramo = codigoInicio + "-" + codigoFin + "-" + tipo;
                            tramos.put(claveTramo, tramo);
                            if (tipo == Constantes.CAMINANDO) {
                                // Agregar tramo inverso para caminando
                                Tramo tramoInverso = new Tramo(paradaFin, paradaInicio, tiempo, 0);
                                // Para evitar duplicacion de carga de paradas caminando, seteamos el tipo
                                // despues
                                tramoInverso.setTipo(Constantes.CAMINANDO);
                                String claveTramoInverso = codigoFin + "-" + codigoInicio + "-" + Constantes.CAMINANDO;
                                tramos.put(claveTramoInverso, tramoInverso);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Archivo no encontrado: " + nombreArchivo, e);
            throw e;
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Error leyendo el archivo: " + nombreArchivo, e);
            throw e;
        }
        return tramos;
        }
}

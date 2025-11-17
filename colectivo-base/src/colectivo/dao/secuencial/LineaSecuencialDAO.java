package colectivo.dao.secuencial;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Constantes;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.util.Factory;

/**
 * DAO implementation that reads {@link Linea} data from sequential text files.
 *
 * <p>
 * This class parses two files configured via the resource bundle `secuencial`:
 * the lines file (format: {@code codigoLinea;nombreLinea;codigoParada1;...})
 * and the frequencies file (format: {@code codigoLinea;diaSemana;HH:MM}). It
 * links loaded {@link Linea} instances to existing {@link Parada} objects
 * obtained from a {@link ParadaDAO} so the same stop instances are reused.
 * </p>
 *
 * <p>
 * Construction:
 * <ul>
 *   <li>The no-arg constructor obtains a {@link ParadaDAO} from the {@code Factory}
 *       and delegates to the constructor that accepts a {@code ParadaDAO}.</li>
 *   <li>The alternative constructor allows injecting a shared {@code ParadaDAO}
 *       so multiple DAOs reuse the same {@code Parada} instances.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Loading semantics:
 * <ul>
 *   <li>{@link #buscarTodos()} triggers a one-time load controlled by the {@code actualizar}
 *       flag; parsing errors and missing files are reported to {@code System.err}
 *       and exceptions are propagated to the caller.</li>
 *   <li>Private helper {@code leerDeArchivo(...)} reads and constructs lines and
 *       delegates frequency parsing to {@code cargarFrecuencias(...)}.</li>
 *   <li>Input parsing validates numeric stop IDs and time formats, logging warnings
 *       for malformed lines while attempting to continue processing.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Notes:
 * <ul>
 *   <li>Character encoding and separators are taken from {@link colectivo.aplicacion.Constantes}.</li>
 *   <li>The DAO is intended for educational use following the original course data-loading logic,
 *       with the improvement of shared stop instances via dependency injection.</li>
 * </ul>
 * </p>
 *
 * @author Miyen
 * @version 1.0
 * @since 1.0
 * @see colectivo.dao.ParadaDAO
 * @see colectivo.modelo.Linea
 * @see colectivo.aplicacion.Constantes
 */
public class LineaSecuencialDAO implements LineaDAO {

    private static final Logger logger = LogManager.getLogger(LineaSecuencialDAO.class);

	private String rutaArchivo;
	private String rutaArchivoFrecuencia;
	private final ParadaDAO paradaDAO;
	private Map<Integer, Parada> paradas;
	private Map<String, Linea> lineas;
	private boolean actualizar;

    /**
     * Default constructor.
     *
     * <p>
     * Obtains a {@link ParadaDAO} from the {@link Factory} using the key {@code "PARADA"}
     * and delegates to the parameterized constructor. This preserves previous behaviour
     * where a local sequential {@code Parada} DAO was used.
     * </p>
     */
    public LineaSecuencialDAO() {
        this(Factory.getInstancia("PARADA", ParadaDAO.class));
    }
    
	/**
	 * Constructor that accepts an injected {@link ParadaDAO}.
	 *
	 * <p>
	 * Using an injected {@code ParadaDAO} allows multiple DAOs to share the same
	 * {@link Parada} instances so stops are not duplicated in memory.
	 * </p>
	 *
	 * @param paradaDAO shared {@code ParadaDAO} used to load and reuse stop instances
	 */
	public LineaSecuencialDAO(ParadaDAO paradaDAO) {
		this.paradaDAO = paradaDAO;
		this.paradas = cargarParadas();
		ResourceBundle rb = ResourceBundle.getBundle("secuencial");
		this.rutaArchivo = rb.getString("linea");
		this.rutaArchivoFrecuencia = rb.getString("frecuencia");
		actualizar = true;
	}

	/**
	 * Loads stops from the injected {@link ParadaDAO}.
	 *
	 * <p>
	 * This method attempts to obtain all stops from {@code paradaDAO.buscarTodos()}.
	 * Any exception thrown by the underlying DAO is caught and printed to the error
	 * stream; an empty map is returned in case of failure.
	 * </p>
	 *
	 * @return a map from stop code to {@link Parada} instances (never {@code null})
	 */
	private Map<Integer, Parada> cargarParadas() {
        Map<Integer, Parada> paradas = new TreeMap<Integer, Parada>();
		try {
            paradas = this.paradaDAO.buscarTodos();
		} catch (RuntimeException re) {
            logger.error("Error de ejecución al cargar paradas", re);
            throw re;
        } catch (Exception e) {
			logger.error("No se pudieron cargar las paradas", e);
		}
		return paradas;
	}

	/**
	 * Returns all loaded {@link Linea} objects.
	 *
	 * <p>
	 * The first invocation triggers a one-time load of line and frequency files;
	 * subsequent calls return the cached map. If loading fails, the exception is
	 * printed to {@code System.err} and propagated.
	 * </p>
	 *
	 * @return a map from line code to {@link Linea} instances
	 * @throws Exception if an underlying IO or parsing error occurs during initial load
	 */
	@Override
	public Map<String, Linea> buscarTodos() throws Exception {
		if (actualizar) {
			lineas = new TreeMap<String, Linea>();
			try {
				lineas = leerDeArchivo(rutaArchivo, rutaArchivoFrecuencia);
				actualizar = false;
			} catch (Exception e) {
				logger.error("Error al cargar líneas desde archivos secuenciales", e);
				throw e;
			}
		}
		return lineas;
	}

	/**
	 * Reads line definitions and links them to existing stops, then loads frequencies.
	 *
	 * <p>
	 * The file specified by {@code nombreArchivo} is expected to contain lines in the
	 * format: {@code codigoLinea;nombreLinea;codigoParada1;...}. Stops referenced by code
	 * are looked up in the {@code paradas} map and linked bi-directionally with the
	 * created {@link Linea} instances. Lines without stops are ignored and a warning
	 * is printed.
	 * </p>
	 *
	 * @param nombreArchivo path to the lines file
	 * @param nombreArchivoFrecuencia path to the frequencies file
	 * @return a map from line code to populated {@link Linea} instances
	 * @throws FileNotFoundException if either input file cannot be found
	 * @throws NoSuchElementException if an unexpected scanner error occurs
	 * @throws IllegalStateException if scanner is closed unexpectedly
	 * @throws Exception for other parsing or IO errors
	 */
	private Map<String, Linea> leerDeArchivo(String nombreArchivo, String nombreArchivoFrecuencia) throws Exception {
		lineas = new TreeMap<String, Linea>();

		// PASO 1: Cargar las líneas y sus paradas primero
		try (Scanner read = new Scanner(new File(nombreArchivo), Constantes.ENCODING)) {
			String codigo;
			String nombre;
			Linea lineaObj;
			int codigoParada;
			Parada parada;

			while (read.hasNextLine()) {
				String linea = read.nextLine().trim();

				if (!linea.isEmpty()) {
					String[] partes = linea.split(Constantes.SEPARADOR);

					if (partes.length >= Constantes.CAMPOS_MINIMOS_LINEA) {
						codigo = partes[0].trim();
						nombre = partes[1].trim();
						lineaObj = new Linea(codigo, nombre);
						for (int i = 2; i < partes.length; i++) {
                            try {
							    codigoParada = Integer.parseInt(partes[i].trim());
							    parada = paradas.get(codigoParada);
							    if (parada != null) {
								    // Agregar la parada a la línea y viceversa (dentro de modelo Linea)
								    lineaObj.agregarParada(parada);
							    } else {
								    logger.warn("Advertencia: código de parada " + codigoParada
										    + " no encontrado para la línea " + codigo);
							    }
                            } catch (NumberFormatException nfe) {
                                logger.warn("Advertencia: código de parada inválido '" + partes[i].trim()
                                        + "' para la línea " + codigo, nfe);
                            }
						}

						if (!lineaObj.getParadas().isEmpty()) {
							lineas.put(codigo, lineaObj);
						} else {
							logger.warn("Advertencia: línea " + codigo + " sin paradas, ignorada.");
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
		} // fin carga de lineas

		// PASO 2: Cargar las frecuencias UNA SOLA VEZ y agregarlas a las líneas correspondientes
		cargarFrecuencias(nombreArchivoFrecuencia, lineas);
		return lineas;
	}

	/**
	 * Reads frequency definitions from the given file and attaches them to lines.
	 *
	 * <p>
	 * The frequencies file must contain entries in the format:
	 * {@code codigoLinea;diaSemana;HH:MM}. For each valid entry, the corresponding
	 * {@link Linea} (if present) will receive the parsed frequency. Malformed time
	 * values or references to non-existing lines are reported to {@code System.err}
	 * and processing continues.
	 * </p>
	 *
	 * @param nombreArchivoFrecuencia path to the frequencies file
	 * @param lineasCargadas map of lines previously loaded by {@link #leerDeArchivo(String, String)}
	 * @throws FileNotFoundException if the frequencies file cannot be found
	 * @throws NoSuchElementException if an unexpected scanner error occurs
	 * @throws IllegalStateException if scanner is closed unexpectedly
	 */
	private static void cargarFrecuencias(String nombreArchivoFrecuencia, Map<String, Linea> lineasCargadas)
			throws FileNotFoundException {

		try (Scanner freqRead = new Scanner(new File(nombreArchivoFrecuencia), Constantes.ENCODING)) {

			// Leer frecuencias
			while (freqRead.hasNextLine()) {
				String lineaFreq = freqRead.nextLine().trim();

				if (!lineaFreq.isEmpty()) {
					String[] partesFreq = lineaFreq.split(Constantes.SEPARADOR);
					if (partesFreq.length >= Constantes.CAMPOS_MINIMOS_FRECUENCIA) {
						String codigoLinea = partesFreq[0].trim();
						int diaSemana = Integer.parseInt(partesFreq[1].trim());
						String horaString = partesFreq[2].trim();

						// Buscar la línea en el mapa
						Linea lineaActual = lineasCargadas.get(codigoLinea);
						if (lineaActual != null) {
							try {
								LocalTime hora = LocalTime.parse(horaString);
								lineaActual.agregarFrecuencia(diaSemana, hora);
							} catch (DateTimeParseException dtpe) {
								logger.warn("Advertencia: formato de hora inválido '" + horaString
										+ "' para la línea " + codigoLinea + ": " + dtpe.getMessage(), dtpe);
							}
						} else {
							logger.warn("Advertencia: frecuencia para línea inexistente " + codigoLinea);
						}
					}
				}
			} // fin while frecuencias
		} catch (FileNotFoundException e) {
			logger.error("Archivo no encontrado: " + nombreArchivoFrecuencia, e);
			throw e;
		} catch (NoSuchElementException | IllegalStateException e) {
			logger.error("Error leyendo archivo: " + nombreArchivoFrecuencia, e);
			throw e;
		} // fin try
	}
}

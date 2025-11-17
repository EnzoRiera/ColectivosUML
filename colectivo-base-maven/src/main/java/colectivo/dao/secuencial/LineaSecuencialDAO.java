package colectivo.dao.secuencial;

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
 * Implementación de {@link LineaDAO} que lee líneas de transporte desde archivos de texto secuenciales.
 *
 * <p>Esta implementación del patrón DAO carga las líneas y sus frecuencias desde archivos planos
 * ubicados en el classpath. Utiliza un mecanismo de caché para evitar recargar los datos en cada
 * consulta, marcándolos como actualizables solo cuando es necesario.</p>
 *
 * <p>Proceso de carga en dos fases:</p>
 * <ol>
 *   <li>Carga líneas con sus paradas desde el archivo principal</li>
 *   <li>Carga frecuencias (horarios) desde archivo secundario y las asocia a las líneas</li>
 * </ol>
 *
 * <p>Características:</p>
 * <ul>
 *   <li>Validación de paradas: ignora paradas inexistentes con logging de advertencia</li>
 *   <li>Control de duplicados para líneas circulares</li>
 *   <li>Relación bidireccional entre líneas y paradas</li>
 *   <li>Caché de datos con flag de actualización</li>
 *   <li>Manejo robusto de errores con logging detallado</li>
 * </ul>
 *
 * <p>Las rutas de archivos se obtienen del ResourceBundle "secuencial.properties".</p>
 *
 * @see LineaDAO
 * @see Linea
 * @see ParadaDAO
 * @see Factory
 */
public class LineaSecuencialDAO implements LineaDAO {

	private static final Logger logger = LogManager.getLogger(LineaSecuencialDAO.class);

	private final String rutaArchivo;
	private final String rutaArchivoFrecuencia;
	private final ParadaDAO paradaDAO;
	private final Map<Integer, Parada> paradas;
	private Map<String, Linea> lineas;

	/** Indica si se debe recargar los datos en la próxima consulta. */
	private boolean actualizar;

	/**
	 * Constructor por defecto que obtiene el ParadaDAO desde la Factory.
	 *
	 * <p>Utiliza {@link Factory} para obtener dinámicamente la implementación
	 * de {@link ParadaDAO} configurada en el sistema.</p>
	 */
	public LineaSecuencialDAO() {
		this(Factory.getInstancia("PARADA", ParadaDAO.class));
	}

	/**
	 * Constructor con inyección de dependencia del ParadaDAO.
	 *
	 * <p>Inicializa el DAO cargando las paradas, leyendo las rutas de archivos
	 * desde el ResourceBundle "secuencial" y marcando los datos como pendientes
	 * de actualización.</p>
	 *
	 * @param paradaDAO el {@link ParadaDAO} a utilizar para cargar paradas
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
	 * Carga todas las paradas desde el ParadaDAO configurado.
	 *
	 * <p>Las paradas son necesarias para validar las referencias al construir
	 * las líneas. En caso de error, registra el problema y puede lanzar
	 * RuntimeException o retornar un mapa vacío.</p>
	 *
	 * @return mapa de {@link Parada} indexadas por código, o mapa vacío si falla la carga
	 * @throws RuntimeException si ocurre un error de ejecución al cargar paradas
	 */
	private Map<Integer, Parada> cargarParadas() {
		Map<Integer, Parada> paradas = new TreeMap<>();

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
	 * Busca y retorna todas las líneas de transporte disponibles.
	 *
	 * <p>Implementa el método de {@link LineaDAO}. Utiliza caché: solo recarga
	 * los datos desde los archivos si el flag {@code actualizar} está activo.
	 * En consultas sucesivas, retorna el caché sin acceder a los archivos.</p>
	 *
	 * @return mapa con las {@link Linea} indexadas por su código identificador
	 * @throws Exception si ocurre un error al leer los archivos o parsear los datos
	 */
	@Override
	public Map<String, Linea> buscarTodos() throws Exception {
		if (actualizar) {
			lineas = new TreeMap<>();
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
	 * Lee y parsea las líneas y frecuencias desde archivos de texto.
	 *
	 * <p>Proceso completo en dos pasos:</p>
	 * <ol>
	 *   <li>Lee líneas con sus paradas desde {@code nombreArchivo}</li>
	 *   <li>Lee frecuencias desde {@code nombreArchivoFrecuencia} y las asocia</li>
	 * </ol>
	 *
	 * <p>Formato esperado del archivo de líneas:
	 * código;nombre;códigoParada1;códigoParada2;...</p>
	 *
	 * <p>Validaciones realizadas:</p>
	 * <ul>
	 *   <li>Verifica que las paradas referenciadas existan</li>
	 *   <li>Controla duplicados para líneas circulares</li>
	 *   <li>Ignora líneas sin paradas válidas</li>
	 *   <li>Registra advertencias para datos inconsistentes</li>
	 * </ul>
	 *
	 * @param nombreArchivo ruta relativa del archivo de líneas en el classpath
	 * @param nombreArchivoFrecuencia ruta relativa del archivo de frecuencias en el classpath
	 * @return mapa de {@link Linea} indexadas por código
	 * @throws FileNotFoundException si alguno de los archivos no existe en el classpath
	 * @throws NoSuchElementException si hay problemas al leer el Scanner
	 * @throws IllegalStateException si el Scanner está cerrado
	 * @throws Exception si ocurre cualquier otro error durante la lectura
	 */
	private Map<String, Linea> leerDeArchivo(String nombreArchivo, String nombreArchivoFrecuencia) throws Exception {
		lineas = new TreeMap<>();

		// PASO 1: Cargar las líneas y sus paradas primero
		var inputStream = getClass().getClassLoader().getResourceAsStream(nombreArchivo);
		if (inputStream == null) {
			throw new FileNotFoundException("Resource not found in classpath: " + nombreArchivo);
		}

		try (Scanner read = new Scanner(inputStream, Constantes.ENCODING)) {
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
									// Control de duplicados para líneas circulares:
									// Solo agregamos la parada si no está ya en la línea
									if (!lineaObj.getParadas().contains(parada)) {
										lineaObj.agregarParada(parada);
										logger.debug("Parada {} agregada a línea {}", codigoParada, codigo);
									} else {
										logger.debug("Parada {} ya existe en línea {}, evitando duplicado", codigoParada, codigo);
									}
									// Control adicional: verificar que la línea esté en la parada
									// (necesario porque agregarParada llama a parada.agregarLinea)
									if (!parada.getLineas().contains(lineaObj)) {
										parada.agregarLinea(lineaObj);
									}
								} else {
									logger.warn("Código de parada {} no encontrado para la línea {}", codigoParada, codigo);

								}
							} catch (NumberFormatException nfe) {
								logger.warn("Advertencia: código de parada inválido '{}' para la línea {}", partes[i].trim(), codigo, nfe);
							}
						}

						if (!lineaObj.getParadas().isEmpty()) {
							lineas.put(codigo, lineaObj);
						} else {
							logger.warn("Advertencia: línea {} sin paradas, ignorada" , codigo);
						}
					}
				}
			}
		} catch (NoSuchElementException | IllegalStateException e) {
			logger.error("Error leyendo archivo: {}",nombreArchivo, e);
			throw e;
		}

		// PASO 2: Cargar las frecuencias UNA SOLA VEZ y agregarlas a las líneas
		// correspondientes
		cargarFrecuencias(nombreArchivoFrecuencia, lineas);
		return lineas;
	}

	/**
	 * Carga las frecuencias (horarios de salida) desde un archivo y las asocia a las líneas.
	 *
	 * <p>Formato esperado del archivo de frecuencias:
	 * códigoLínea;díaSemana;hora (formato HH:mm:ss)</p>
	 *
	 * <p>Características:</p>
	 * <ul>
	 *   <li>Valida que la línea referenciada exista en el mapa proporcionado</li>
	 *   <li>Parsea horas usando {@link LocalTime#parse(CharSequence)}</li>
	 *   <li>Registra advertencias para líneas inexistentes o formato de hora inválido</li>
	 *   <li>Ignora líneas vacías</li>
	 *   <li>Asocia cada frecuencia a su línea correspondiente</li>
	 * </ul>
	 *
	 * @param nombreArchivoFrecuencia ruta relativa del archivo de frecuencias en el classpath
	 * @param lineasCargadas mapa de {@link Linea} previamente cargadas donde asociar las frecuencias
	 * @throws FileNotFoundException si el archivo no existe en el classpath
	 * @throws NoSuchElementException si hay problemas al leer el Scanner
	 * @throws IllegalStateException si el Scanner está cerrado
	 */
	private static void cargarFrecuencias(String nombreArchivoFrecuencia, Map<String, Linea> lineasCargadas)
			throws FileNotFoundException {

		var inputStream = LineaSecuencialDAO.class.getClassLoader().getResourceAsStream(nombreArchivoFrecuencia);
		if (inputStream == null) {
			throw new FileNotFoundException("Resource not found in classpath: " + nombreArchivoFrecuencia);
		}

		try (Scanner freqRead = new Scanner(inputStream, Constantes.ENCODING)) {

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
								logger.warn("Formato de hora inválido '{}' para la línea {}", horaString, codigoLinea, dtpe);
							}
						} else {
							logger.warn("Frecuencia para línea inexistente {}", codigoLinea);
						}
					}
				}
			}
		} catch (NoSuchElementException | IllegalStateException e) {
			logger.error("Error reading resource: {}", nombreArchivoFrecuencia, e);
			throw e;
		}
	}
}
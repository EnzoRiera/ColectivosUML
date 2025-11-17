package colectivo.dao.secuencial;

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
 * Implementación de {@link ParadaDAO} que lee paradas de transporte desde archivos de texto secuenciales.
 *
 * <p>Esta implementación del patrón DAO carga las paradas desde archivos planos ubicados en el classpath.
 * Utiliza un mecanismo de caché para evitar recargar los datos en cada consulta, marcándolos como
 * actualizables solo cuando es necesario.</p>
 *
 * <p>Formato esperado del archivo de paradas:
 * código;nombre;latitud;longitud</p>
 *
 * <p>Características:</p>
 * <ul>
 *   <li>Soporta separador decimal tanto punto como coma (normaliza automáticamente)</li>
 *   <li>Validación de formato numérico con logging de advertencias</li>
 *   <li>Ignora líneas vacías y con formato incorrecto</li>
 *   <li>Caché de datos con flag de actualización</li>
 *   <li>Retorna TreeMap ordenado por código de parada</li>
 *   <li>Manejo robusto de errores con logging detallado</li>
 * </ul>
 *
 * <p>La ruta del archivo se obtiene del ResourceBundle "secuencial.properties".</p>
 *
 * @see ParadaDAO
 * @see Parada
 * @see Constantes
 */
public class ParadaSecuencialDAO implements ParadaDAO {

	private static final Logger logger = LogManager.getLogger(ParadaSecuencialDAO.class);

	/** Ruta del archivo de paradas obtenida del ResourceBundle. */
	private final String rutaArchivo;

	/** Indica si se debe recargar los datos en la próxima consulta. */
	private boolean actualizar;

	/** Caché de paradas cargadas. */
	Map<Integer, Parada> paradas = new TreeMap<>();

	/**
	 * Constructor por defecto que inicializa el DAO.
	 *
	 * <p>Lee la ruta del archivo desde el ResourceBundle "secuencial" y marca
	 * los datos como pendientes de actualización para la primera consulta.</p>
	 */
	public ParadaSecuencialDAO() {
		ResourceBundle rb = ResourceBundle.getBundle("secuencial");
		rutaArchivo = rb.getString("parada");
		actualizar = true;
	}

	/**
	 * Busca y retorna todas las paradas de transporte disponibles.
	 *
	 * <p>Implementa el método de {@link ParadaDAO}. Utiliza caché: solo recarga
	 * los datos desde el archivo si el flag {@code actualizar} está activo.
	 * En consultas sucesivas, retorna el caché sin acceder al archivo.</p>
	 *
	 * <p>Si ocurre un error al leer el archivo, registra el error y retorna
	 * el caché existente (que puede estar vacío si es la primera carga).</p>
	 *
	 * @return mapa con las {@link Parada} indexadas por su código identificador numérico
	 */
	@Override
	public Map<Integer, Parada> buscarTodos() {
		if (actualizar) {
			try {
				paradas = leerDeArchivo(rutaArchivo);
				actualizar = false;
			} catch (Exception e) {
				logger.error("Error al cargar paradas desde el archivo secuencial: {}", rutaArchivo, e);
			}
		}
		return paradas;
	}

	/**
	 * Lee y parsea las paradas desde un archivo de texto.
	 *
	 * <p>Formato esperado del archivo (una parada por línea):
	 * código;nombre;latitud;longitud</p>
	 *
	 * <p>Proceso de lectura:</p>
	 * <ol>
	 *   <li>Carga el archivo desde el classpath</li>
	 *   <li>Lee línea por línea ignorando líneas vacías</li>
	 *   <li>Divide cada línea usando el separador configurado en {@link Constantes#SEPARADOR}</li>
	 *   <li>Normaliza coordenadas reemplazando comas por puntos</li>
	 *   <li>Crea objetos {@link Parada} y los agrega al mapa</li>
	 * </ol>
	 *
	 * <p>Validaciones y manejo de errores:</p>
	 * <ul>
	 *   <li>Verifica que existan al menos {@link Constantes#CAMPOS_MINIMOS_PARADA} campos</li>
	 *   <li>Registra advertencias para líneas con formato numérico inválido</li>
	 *   <li>Continúa procesando el resto del archivo si una línea falla</li>
	 *   <li>Lanza excepción si el archivo no existe o hay errores de lectura</li>
	 * </ul>
	 *
	 * @param nombreArchivo ruta relativa del archivo en el classpath
	 * @return mapa de {@link Parada} indexadas por código, ordenado (TreeMap)
	 * @throws FileNotFoundException si el archivo no existe en el classpath
	 * @throws NoSuchElementException si hay problemas al leer el Scanner
	 * @throws IllegalStateException si el Scanner está cerrado
	 * @throws Exception si ocurre cualquier otro error durante la lectura
	 */
	private Map<Integer, Parada> leerDeArchivo(String nombreArchivo) throws Exception {

		Parada paradaActual;

		var inputStream = getClass().getClassLoader().getResourceAsStream(nombreArchivo);
		if (inputStream == null) {
			throw new FileNotFoundException("Resource not found in classpath: " + nombreArchivo);
		}

		try (Scanner read = new Scanner(inputStream, Constantes.ENCODING)) {
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
							logger.warn("Advertencia: formato numérico inválido en línea: {} ", linea, nfe);
						}
					}
				}
			}
		} catch (NoSuchElementException | IllegalStateException e) {
			logger.error("Error leyendo archivo: {}", nombreArchivo, e);
			throw e;
		}

		return paradas;
	}
}

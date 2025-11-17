package colectivo.dao.secuencial;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Constantes;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

/**
 * Implementación de {@link TramoDAO} que lee tramos de conexión desde archivos de texto secuenciales.
 *
 * <p>Esta implementación del patrón DAO carga los tramos de conexión entre paradas desde archivos planos
 * ubicados en el classpath. Utiliza un mecanismo de caché para evitar recargar los datos en cada consulta,
 * marcándolos como actualizables solo cuando es necesario.</p>
 *
 * <p>Formato esperado del archivo de tramos:
 * códigoInicio;códigoFin;tiempo;tipo</p>
 *
 * <p>Características:</p>
 * <ul>
 *   <li>Valida que las paradas de inicio y fin existan en el sistema</li>
 *   <li>Para tramos tipo {@link Constantes#CAMINANDO}, crea automáticamente el tramo inverso bidireccional</li>
 *   <li>Los tramos de colectivo (tipo 1) son unidireccionales</li>
 *   <li>Control de duplicados para evitar relaciones bidireccionales múltiples</li>
 *   <li>Registra advertencias para paradas inexistentes</li>
 *   <li>Caché de datos con flag de actualización</li>
 *   <li>Manejo robusto de errores con logging detallado</li>
 * </ul>
 *
 * <p>La ruta del archivo se obtiene del ResourceBundle "secuencial.properties".</p>
 *
 * @see TramoDAO
 * @see Tramo
 * @see ParadaDAO
 * @see Constantes
 * @see Factory
 */
public class TramoSecuencialDAO implements TramoDAO {

    private static final Logger logger = LogManager.getLogger(TramoSecuencialDAO.class);

    /** Ruta del archivo de tramos obtenida del ResourceBundle. */
    private final String rutaArchivo;

    /** DAO de paradas utilizado para cargar paradas necesarias. */
    private final ParadaDAO paradaDAO;

    /** Mapa de paradas cargadas para validación de referencias. */
    private final Map<Integer, Parada> paradas;

    /** Caché de tramos cargados. */
    Map<String, Tramo> tramos = new HashMap<>();

    /** Indica si se debe recargar los datos en la próxima consulta. */
    private boolean actualizar;

    /**
     * Constructor por defecto que obtiene el ParadaDAO desde la Factory.
     *
     * <p>Utiliza {@link Factory} para obtener dinámicamente la implementación
     * de {@link ParadaDAO} configurada en el sistema (singleton compartido).</p>
     */
    public TramoSecuencialDAO() {
        // Obtener ParadaDAO desde Factory (singleton compartido)
        this(Factory.getInstancia("PARADA", ParadaDAO.class));
    }

    /**
     * Constructor con inyección de dependencia del ParadaDAO.
     *
     * <p>Inicializa el DAO cargando las paradas, leyendo la ruta del archivo
     * desde el ResourceBundle "secuencial" y marcando los datos como pendientes
     * de actualización.</p>
     *
     * @param paradaDAO el {@link ParadaDAO} a utilizar para cargar paradas
     */
    public TramoSecuencialDAO(ParadaDAO paradaDAO) {
        this.paradaDAO = paradaDAO;
        this.paradas = cargarParadas();
        ResourceBundle rb = ResourceBundle.getBundle("secuencial");
        rutaArchivo = rb.getString("tramo");
        actualizar = true;
    }

	/**
	 * Carga todas las paradas desde el ParadaDAO configurado.
	 *
	 * <p>Las paradas son necesarias para validar las referencias al construir
	 * los tramos. En caso de error, registra el problema y retorna un TreeMap vacío.</p>
	 *
	 * @return mapa de {@link Parada} indexadas por código, o mapa vacío si falla la carga
	 */
	private Map<Integer, Parada> cargarParadas() {
        Map<Integer, Parada> paradas = new TreeMap<>();
        try {
            paradas = this.paradaDAO.buscarTodos();
        } catch (Exception e) {
            logger.error("Error al cargar paradas desde ParadaDAO.", e);
        }
        return paradas;
    }

	/**
	 * Busca y retorna todos los tramos de conexión disponibles.
	 *
	 * <p>Implementa el método de {@link TramoDAO}. Utiliza caché: solo recarga
	 * los datos desde el archivo si el flag {@code actualizar} está activo.
	 * En consultas sucesivas, retorna el caché sin acceder al archivo.</p>
	 *
	 * <p>Si ocurre un error al leer el archivo, registra el error y retorna
	 * el caché existente (que puede estar vacío si es la primera carga).</p>
	 *
	 * @return mapa con los {@link Tramo} indexados por su identificador único "códigoInicio-códigoFin-tipo"
	 */
	@Override
	public Map<String, Tramo> buscarTodos() {
        if (actualizar) {
            try {
                tramos = leerDeArchivo(rutaArchivo);
                actualizar = false;
            } catch (Exception e) {
                logger.error("Error al cargar tramos desde el archivo: {}",rutaArchivo, e);
            }
        }
        return tramos;
    }

	/**
	 * Lee y parsea los tramos desde un archivo de texto.
	 *
	 * <p>Formato esperado del archivo (un tramo por línea):
	 * códigoInicio;códigoFin;tiempo;tipo</p>
	 *
	 * <p>Proceso de lectura:</p>
	 * <ol>
	 *   <li>Limpia el caché de tramos existente</li>
	 *   <li>Carga el archivo desde el classpath</li>
	 *   <li>Lee línea por línea ignorando líneas vacías</li>
	 *   <li>Divide cada línea usando el separador configurado en {@link Constantes#SEPARADOR}</li>
	 *   <li>Valida que las paradas de inicio y fin existan</li>
	 *   <li>Crea objetos {@link Tramo} y los agrega al mapa</li>
	 *   <li>Para tramos tipo {@link Constantes#CAMINANDO}, crea el tramo inverso bidireccional</li>
	 * </ol>
	 *
	 * <p>Lógica especial para tramos caminando:</p>
	 * <ul>
	 *   <li>Controla duplicados en relaciones bidireccionales</li>
	 *   <li>Agrega la parada de destino a la lista de paradas caminando de la parada de origen</li>
	 *   <li>Crea un tramo inverso automáticamente (bidireccional)</li>
	 *   <li>Establece el tipo después para evitar duplicación de carga</li>
	 * </ul>
	 *
	 * <p>Validaciones y manejo de errores:</p>
	 * <ul>
	 *   <li>Verifica que existan al menos {@link Constantes#CAMPOS_MINIMOS_TRAMO} campos</li>
	 *   <li>Valida que ambas paradas existan antes de crear el tramo</li>
	 *   <li>Ignora silenciosamente tramos con paradas inexistentes</li>
	 *   <li>Continúa procesando el resto del archivo si una línea falla</li>
	 *   <li>Lanza excepción si el archivo no existe o hay errores de lectura</li>
	 * </ul>
	 *
	 * @param nombreArchivo ruta relativa del archivo en el classpath
	 * @return mapa de {@link Tramo} indexados por clave "códigoInicio-códigoFin-tipo"
	 * @throws FileNotFoundException si el archivo no existe en el classpath
	 * @throws NoSuchElementException si hay problemas al leer el Scanner
	 * @throws IllegalStateException si el Scanner está cerrado
	 * @throws Exception si ocurre cualquier otro error durante la lectura
	 */
	private Map<String, Tramo> leerDeArchivo(String nombreArchivo) throws Exception {
		tramos.clear();

		var inputStream = getClass().getClassLoader().getResourceAsStream(nombreArchivo);
		if (inputStream == null) {
			throw new FileNotFoundException("Resource not found in classpath: " + nombreArchivo);
		}

		try (Scanner read = new Scanner(inputStream, Constantes.ENCODING)) {
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
								// Control de duplicados: verificar si la relación bidireccional ya existe
								if (!paradaInicio.getParadaCaminando().contains(paradaFin)) {
									paradaInicio.agregarParadaCaminado(paradaFin);
									logger.debug("Relación caminando agregada: parada {} -> {}", codigoInicio, codigoFin);
								} else {
									logger.debug("Relación caminando {} -> {} ya existe, evitando duplicado", codigoInicio, codigoFin);
								}
								if (!paradaFin.getParadaCaminando().contains(paradaInicio)) {
									paradaFin.agregarParadaCaminado(paradaInicio);
								}
								
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
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Error leyendo el archivo: {}",nombreArchivo, e);
            throw e;
        }
		return tramos;
	}
}
package colectivo.logica;

import java.util.Map;
import java.util.TreeMap;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.servicio.LineaService;
import colectivo.servicio.LineaServiceImpl;
import colectivo.servicio.ParadaService;
import colectivo.servicio.ParadaServiceImpl;
import colectivo.servicio.TramoService;
import colectivo.servicio.TramoServiceImpl;

/**
 * Clase Singleton que representa el modelo de la ciudad con su red de transporte.
 * <p>
 * Esta clase centraliza toda la información de la red de transporte público de la ciudad,
 * incluyendo paradas, tramos (conexiones entre paradas) y líneas de colectivo.
 * Implementa el patrón Singleton para garantizar una única instancia del modelo de la ciudad.
 * </p>
 * <p>
 * <b>Componentes del modelo:</b>
 * <ul>
 *   <li><b>Paradas:</b> Puntos físicos donde los colectivos recogen y dejan pasajeros</li>
 *   <li><b>Tramos:</b> Conexiones entre paradas, pueden ser en colectivo o caminando</li>
 *   <li><b>Líneas:</b> Rutas de colectivo que conectan múltiples paradas en secuencia</li>
 * </ul>
 * </p>
 * <p>
 * <b>Estructura de datos:</b> Todos los elementos se almacenan en {@link TreeMap} para
 * mantener un orden consistente y permitir búsquedas eficientes:
 * <ul>
 *   <li>Paradas indexadas por código (Integer)</li>
 *   <li>Líneas indexadas por código (String)</li>
 *   <li>Tramos indexados por clave compuesta "codigoInicio-codigoFin-tipo" (String)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Servicios asociados:</b> La clase utiliza servicios (capa de negocio) para cargar
 * los datos desde los DAOs correspondientes:
 * <ul>
 *   <li>{@link ParadaService} - Gestión de paradas</li>
 *   <li>{@link LineaService} - Gestión de líneas</li>
 *   <li>{@link TramoService} - Gestión de tramos</li>
 * </ul>
 * </p>
 * <p>
 * <b>Orden de inicialización crítico:</b><br>
 * Es fundamental cargar las paradas PRIMERO, ya que tanto las líneas como los tramos
 * necesitan referenciar objetos Parada existentes para establecer relaciones.
 * </p>
 *
 * @see Parada
 * @see Linea
 * @see Tramo
 * @see Coordinador
 * @see ParadaService
 * @see LineaService
 * @see TramoService
 */
public class Ciudad {

	// Instancia Singleton -> Bandera
	/** Instancia única de Ciudad (patrón Singleton). */
	private static Ciudad ciudad = null;

	/** Nombre de la ciudad. */
	private String nombre;

	/** Mapa de líneas de colectivo indexadas por código. */
	private final Map<String, Linea> lineas;

	/** Mapa de paradas indexadas por código. */
	private final Map<Integer, Parada> paradas;

	/** Mapa de tramos indexados por clave compuesta (inicio-fin-tipo). */
	private final Map<String, Tramo> tramos;

	/** Servicio para gestión de líneas. */
	private final LineaService lineaService;

	/** Servicio para gestión de paradas. */
	private final ParadaService paradaService;

	/** Servicio para gestión de tramos. */
	private final TramoService tramoService;

	/**
	 * Obtiene la instancia única de Ciudad (patrón Singleton).
	 * <p>
	 * Si la instancia no existe, la crea invocando el constructor privado, que
	 * a su vez carga todos los datos de paradas, líneas y tramos desde los servicios.
	 * Este método implementa lazy initialization (inicialización perezosa).
	 * </p>
	 * <p>
	 * <b>Nota:</b> Este método no es thread-safe. En aplicaciones multi-hilo se debe
	 * sincronizar externamente si hay posibilidad de acceso concurrente durante la
	 * primera inicialización.
	 * </p>
	 *
	 * @return la instancia única de {@link Ciudad} con todos los datos cargados
	 * @throws Exception si ocurre un error al cargar los datos desde los servicios
	 * @see #Ciudad()
	 */
	public static Ciudad getCiudad() throws Exception {
		if (ciudad == null)
			ciudad = new Ciudad();

		return ciudad;
	}

	/**
	 * Constructor privado para implementar el patrón Singleton.
	 * <p>
	 * Este constructor realiza la inicialización completa del modelo de la ciudad
	 * siguiendo un orden específico y crítico. La secuencia de carga es:
	 * </p>
	 * <ol>
	 *   <li><b>Crear ParadaService:</b> Se instancia {@link ParadaServiceImpl} como servicio único</li>
	 *   <li><b>Cargar paradas PRIMERO:</b> Se cargan todas las paradas desde el servicio y se
	 *       almacenan en un {@link TreeMap} ordenado por código. Este paso es crítico porque
	 *       las líneas y tramos necesitan referenciar objetos Parada existentes.</li>
	 *   <li><b>Crear servicios de Línea y Tramo:</b> Se instancian {@link LineaServiceImpl}
	 *       y {@link TramoServiceImpl}. Estos servicios utilizarán las paradas ya cargadas
	 *       para establecer relaciones.</li>
	 *   <li><b>Cargar líneas:</b> Se cargan todas las líneas con sus paradas asociadas y
	 *       frecuencias desde el servicio correspondiente.</li>
	 *   <li><b>Cargar tramos:</b> Se cargan todos los tramos (conexiones entre paradas)
	 *       desde el servicio correspondiente.</li>
	 * </ol>
	 * <p>
	 * <b>Orden de carga crítico:</b> El orden de las operaciones es fundamental.
	 * Las paradas DEBEN cargarse antes que las líneas y tramos porque:
	 * <ul>
	 *   <li>Las líneas necesitan agregar referencias a objetos Parada existentes</li>
	 *   <li>Los tramos necesitan referenciar paradas de inicio y fin existentes</li>
	 *   <li>Se establecen relaciones bidireccionales entre paradas, líneas y tramos</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>Dependencias compartidas:</b> Se crea una única instancia de {@link ParadaService}
	 * que es compartida implícitamente a través de la Factory por los demás servicios,
	 * garantizando que todos trabajen con el mismo conjunto de paradas.
	 * </p>
	 *
	 * @throws Exception si ocurre un error al cargar datos desde cualquiera de los servicios
	 * @see ParadaService#buscarTodos()
	 * @see LineaService#buscarTodos()
	 * @see TramoService#buscarTodos()
	 */
	private Ciudad() throws Exception {
        super();
        
        // 1. Crear UNA SOLA instancia de ParadaService
        paradaService = new ParadaServiceImpl();
        
        // 2. Cargar paradas PRIMERO
        paradas = new TreeMap<>(paradaService.buscarTodos());
        
        // 3. Crear servicios de Linea y Tramo que usen las mismas paradas
        lineaService = new LineaServiceImpl();
        tramoService = new TramoServiceImpl();
        
        // 4. Cargar lineas y tramos
        lineas = new TreeMap<>(lineaService.buscarTodos());
        tramos = new TreeMap<>(tramoService.buscarTodos());
       
	}

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	// Getters de las estructuras de datos
	public Map<String, Linea> getLineas() {
		return lineas;
	}
	public Map<Integer, Parada> getParadas() {
		return paradas;
	}
	public Map<String, Tramo> getTramos() {
		return tramos;
	}

}

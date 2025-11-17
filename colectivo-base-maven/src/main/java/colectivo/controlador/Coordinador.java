package colectivo.controlador;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.interfaz.Formateador;
import colectivo.logica.Calculo;
import colectivo.logica.Ciudad;
import colectivo.logica.Recorrido;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.servicio.InterfazService;
import colectivo.servicio.InterfazServiceImpl;
import colectivo.servicio.LineaService;
import colectivo.servicio.ParadaService;
import colectivo.servicio.ParadaServiceImpl;
import colectivo.servicio.TramoService;

/**
 * Coordinador central de la aplicación que gestiona la comunicación entre
 * componentes.
 * <p>
 * Esta clase actúa como el controlador principal siguiendo el patrón de diseño
 * Mediator/Coordinator. Coordina la interacción entre los diferentes módulos de
 * la aplicación: {@link Ciudad}, {@link Configuracion}, {@link Calculo} y
 * {@link InterfazService}.
 * </p>
 * <p>
 * <b>Responsabilidades principales:</b>
 * <ul>
 * <li>Inicialización y configuración de todos los componentes de la
 * aplicación</li>
 * <li>Coordinación de búsquedas de recorridos entre paradas</li>
 * <li>Gestión de la internacionalización (cambio de idioma)</li>
 * <li>Provisión de acceso centralizado a datos de paradas y tramos</li>
 * <li>Inicio y gestión de la interfaz de usuario</li>
 * </ul>
 * </p>
 * <p>
 * <b>Flujo de inicialización:</b>
 * <ol>
 * <li>Crear instancia del Coordinador</li>
 * <li>Llamar a {@link #inicializarAplicacion()} para configurar todos los
 * componentes</li>
 * <li>Llamar a {@link #iniciarConsulta()} para lanzar la interfaz de
 * usuario</li>
 * </ol>
 * </p>
 *
 * @see Ciudad
 * @see Configuracion
 * @see Calculo
 * @see InterfazService
 * @see Recorrido
 */
public class Coordinador {

	private static final Logger logger = LogManager.getLogger(Coordinador.class);

	/** Módulo de cálculo de recorridos óptimos entre paradas. */
	private Calculo calculo;

	/** Modelo de la ciudad que contiene paradas, tramos y líneas. */
	private Ciudad ciudad;

	/** Configuración de la aplicación incluyendo internacionalización. */
	private Configuracion configuracion;

	/** Servicio de interfaz de usuario para interacción con el usuario. */
	private InterfazService interfazService;

	/** Lista de soluciones de recorridos encontrados en la última búsqueda. */
	List<List<Recorrido>> recorridosSolucion;

	/**
	 * Busca recorridos óptimos entre una parada de origen y una de destino.
	 * <p>
	 * Este método coordina el proceso de búsqueda de recorridos utilizando el
	 * módulo {@link Calculo}. Realiza validaciones exhaustivas de los parámetros de
	 * entrada y delega el cálculo al algoritmo de búsqueda.
	 * </p>
	 * <p>
	 * <b>Validaciones realizadas:</b>
	 * <ul>
	 * <li>paradaOrigen no puede ser null</li>
	 * <li>paradaDestino no puede ser null</li>
	 * <li>horaLlegaParada no puede ser null</li>
	 * <li>ciudad debe estar inicializada</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Los recorridos retornados están ordenados por optimalidad según los criterios
	 * del algoritmo de cálculo (tiempo, número de transbordos, etc.).
	 * </p>
	 *
	 * @param paradaOrigen    la parada desde donde se inicia el recorrido
	 * @param paradaDestino   la parada a la que se desea llegar
	 * @param diaSemana       el día de la semana (1-7) para considerar frecuencias
	 *                        de líneas
	 * @param horaLlegaParada la hora a la que se llega a la parada de origen
	 * @return una lista de listas de {@link Recorrido}, donde cada lista interna
	 *         representa una solución completa (puede incluir transbordos)
	 * @throws NullPointerException  si paradaOrigen, paradaDestino o
	 *                               horaLlegaParada son null
	 * @throws IllegalStateException si ciudad no está inicializada
	 * @see Calculo#calcularRecorrido(Parada, Parada, int, LocalTime, Map)
	 * @see #mapearTramos()
	 */
	public List<List<Recorrido>> buscarRecorridos(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada) {
		// Validaciones con log
		if (paradaOrigen == null) {
			logger.error("buscarRecorridos: paradaOrigen es null");
			throw new NullPointerException("paradaOrigen no puede ser null");
		}
		if (paradaDestino == null) {
			logger.error("buscarRecorridos: paradaDestino es null");
			throw new NullPointerException("paradaDestino no puede ser null");
		}
		if (horaLlegaParada == null) {
			logger.error("buscarRecorridos: horaLlegaParada es null");
			throw new NullPointerException("horaLlegaParada no puede ser null");
		}
		if (ciudad == null) {
			logger.error("buscarRecorridos: Ciudad no inicializada");
			throw new IllegalStateException("Ciudad no está inicializada");
		}

		recorridosSolucion = Calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada,
				mapearTramos());
		return recorridosSolucion;
	}

	/**
	 * Obtiene el módulo de cálculo de recorridos.
	 *
	 * @return la instancia de {@link Calculo} utilizada por el coordinador
	 */
	public Calculo getCalculo() {
		return calculo;
	}

	/**
	 * Obtiene el modelo de la ciudad.
	 *
	 * @return la instancia de {@link Ciudad} que contiene paradas, tramos y líneas
	 */
	public Ciudad getCiudad() {
		return ciudad;
	}

	/**
	 * Obtiene la configuración de la aplicación.
	 *
	 * @return la instancia de {@link Configuracion} con los parámetros de
	 *         configuración
	 */
	public Configuracion getConfiguracion() {
		return configuracion;
	}

	/**
	 * Obtiene el servicio de interfaz de usuario.
	 *
	 * @return la instancia de {@link InterfazService} para gestionar la UI
	 */
	public InterfazService getInterfaz() {
		return interfazService;
	}

	/**
	 * Inicia la interfaz de usuario para consultas de recorridos.
	 * <p>
	 * Este método debe ser llamado después de {@link #inicializarAplicacion()} para
	 * lanzar la interfaz gráfica o de consola que permite al usuario realizar
	 * consultas.
	 * </p>
	 *
	 * @throws NullPointerException si la interfaz no ha sido inicializada
	 *                              previamente
	 * @see InterfazService#iniciarInterfaz()
	 * @see #inicializarAplicacion()
	 */
	public void iniciarConsulta() {
		if (interfazService == null) {
			logger.error("iniciarConsulta: Interfaz no inicializada");
			throw new NullPointerException("Interfaz not initialized");
		}
		interfazService.iniciarInterfaz();
	}

	/**
	 * Obtiene el mapa de todas las paradas de la ciudad.
	 * <p>
	 * Proporciona acceso centralizado al mapa de paradas desde el modelo
	 * {@link Ciudad}. Las paradas están indexadas por su código para permitir
	 * búsquedas eficientes.
	 * </p>
	 *
	 * @return un {@link Map} con todas las paradas indexadas por código
	 * @throws NullPointerException si ciudad no ha sido inicializada
	 * @see Ciudad#getParadas()
	 */
	public Map<Integer, Parada> mapearParadas() {
		if (ciudad == null) {
			logger.error("mapearParadas: Ciudad no inicializada");
			throw new NullPointerException("Ciudad not initialized");
		}
		return ciudad.getParadas();
	}

	/**
	 * Obtiene el mapa de todos los tramos de la ciudad.
	 * <p>
	 * Proporciona acceso centralizado al mapa de tramos desde el modelo
	 * {@link Ciudad}. Los tramos están indexados por una clave compuesta
	 * "codigoInicio-codigoFin-tipo" para permitir búsquedas eficientes.
	 * </p>
	 *
	 * @return un {@link Map} con todos los tramos indexados por clave compuesta
	 * @throws NullPointerException si ciudad no ha sido inicializada
	 * @see Ciudad#getTramos()
	 */
	public Map<String, Tramo> mapearTramos() {
		if (ciudad == null) {
			logger.error("mapearTramos: Ciudad no inicializada");
			throw new NullPointerException("Ciudad not initialized");
		}
		return ciudad.getTramos();
	}

	/**
	 * Establece el servicio de interfaz de usuario.
	 * <p>
	 * Este método permite inyectar la implementación de {@link InterfazService} que
	 * se utilizará para la interacción con el usuario.
	 * </p>
	 *
	 * @param interfazService la instancia de {@link InterfazService} a utilizar
	 * @see InterfazService
	 * @see InterfazServiceImpl
	 */
	public void setInterfaz(InterfazService interfazService) {
		this.interfazService = interfazService;
	}

	/**
	 * Solicita un cambio de idioma en la aplicación.
	 * <p>
	 * Este método coordina el cambio de idioma delegando la operación a la clase
	 * {@link Configuracion}. El cambio afecta a todos los mensajes y etiquetas de
	 * la interfaz de usuario mediante {@link java.util.ResourceBundle}.
	 * </p>
	 *
	 * @param idioma el código de idioma ISO 639 (ej: "es", "en")
	 * @param pais   el código de país ISO 3166 (ej: "ES", "US")
	 * @see Configuracion#cambiarIdioma(String, String)
	 * @see java.util.Locale
	 */
	public void pedirCambioIdioma(String idioma, String pais) {
		configuracion.cambiarIdioma(idioma, pais);
	}

	/**
	 * Constructor por defecto.
	 * <p>
	 * Crea una instancia del Coordinador sin inicializar los componentes. Es
	 * necesario llamar a {@link #inicializarAplicacion()} después de crear la
	 * instancia para configurar todos los módulos de la aplicación.
	 * </p>
	 *
	 * @see #inicializarAplicacion()
	 */
	public Coordinador() {
	}

	/**
	 * Inicializa todos los componentes de la aplicación.
	 * <p>
	 * Este método debe ser llamado después de crear la instancia del Coordinador y
	 * antes de usar cualquier otro método. Realiza la inicialización en el
	 * siguiente orden:
	 * </p>
	 * <ol>
	 * <li><b>Configuración:</b> Carga {@link Configuracion} (Singleton) y establece
	 * la referencia bidireccional con el coordinador</li>
	 * <li><b>Ciudad:</b> Carga {@link Ciudad} (Singleton) con todos los datos de
	 * paradas, tramos y líneas desde los DAOs</li>
	 * <li><b>Cálculo:</b> Crea la instancia de {@link Calculo} para algoritmos de
	 * búsqueda</li>
	 * <li><b>Interfaz:</b> Crea el servicio de interfaz
	 * ({@link InterfazServiceImpl}) para interacción con el usuario</li>
	 * <li><b>Internacionalización:</b> Configura {@link Formateador} con el
	 * {@link java.util.ResourceBundle} para soporte multi-idioma</li>
	 * </ol>
	 * <p>
	 * <b>Relaciones establecidas:</b> Este método establece las referencias
	 * bidireccionales entre el coordinador y los componentes principales
	 * (Configuracion, Ciudad, Calculo, InterfazService), permitiendo la
	 * comunicación entre módulos.
	 * </p>
	 *
	 * @throws RuntimeException si ocurre un error al inicializar la ciudad o cargar
	 *                          datos
	 * @see Configuracion#getConfiguracion()
	 * @see Ciudad#getCiudad()
	 * @see InterfazServiceImpl
	 * @see Formateador#setResourceBundle(java.util.ResourceBundle)
	 */
	public void inicializarAplicacion() {
	    logger.debug("Iniciando aplicación...");
	    
	    // Configuración
	    logger.debug("Cargando configuración");
		// Inject configuration into the coordinator and vice versa
		configuracion = Configuracion.getConfiguracion();
		configuracion.setCoordinador(this);
		try {
			ciudad = Ciudad.getCiudad();
			// eliminado para evitar dependencia circular
//			ciudad.setCoordinador(this);
		} catch (Exception e) {
	        logger.error("Error al inicializar ciudad", e);
	        throw new RuntimeException("No se pudo inicializar la ciudad", e);
		} // Singleton

	    // Cálculo
	    logger.debug("Inicializando módulo de cálculo");
		calculo = new Calculo();
		// Bidirectional reference - eliminada
//		calculo.setCoordinador(this);

	    // Interfaz
	    logger.debug("Inicializando servicio de interfaz");
		// Creo la Interfaz via Service->(Factory)
		interfazService = new InterfazServiceImpl();
		this.setInterfaz(interfazService);
		interfazService.setCoordinador(this);

	    // ResourceBundle
	    logger.debug("Configurando internacionalización");
		// Inject ResourceBundle into Formateador for internationalization
		Formateador.setResourceBundle(configuracion.getResourceBundle());
		
	    logger.debug("Aplicación inicializada correctamente");
	}

}

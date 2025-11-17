package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.BDConexion;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.util.Factory;

/**
 * Implementación de {@link LineaDAO} para PostgreSQL.
 * <p>
 * Esta clase gestiona la persistencia y recuperación de líneas de colectivo desde una base de datos PostgreSQL.
 * Maneja la lectura de líneas, sus paradas asociadas y las frecuencias de paso por día de la semana.
 * </p>
 * <p>
 * La clase realiza consultas a tres tablas principales:
 * <ul>
 *   <li><b>linea</b> - Contiene la información básica de cada línea (código y nombre)</li>
 *   <li><b>linea_parada</b> - Define la relación entre líneas y paradas en secuencia</li>
 *   <li><b>linea_frecuencia</b> - Almacena los horarios de frecuencia por día de la semana</li>
 * </ul>
 * </p>
 *
 * @see LineaDAO
 * @see Linea
 * @see Parada
 * @see ParadaDAO
 */
public class LineaPostgresqlDAO implements LineaDAO {

	private static final Logger logger = LogManager.getLogger(LineaPostgresqlDAO.class);

	private final ParadaDAO paradaDAO;
	private final Map<Integer, Parada> paradas;
	private Map<String, Linea> lineas;
	private final Connection con = BDConexion.getConnection();
    private boolean actualizar;

	/**
	 * Constructor por defecto.
	 * <p>
	 * Obtiene una instancia de {@link ParadaDAO} desde {@link Factory} y delega
	 * la inicialización al constructor con parámetros.
	 * </p>
	 *
	 * @see Factory#getInstancia(String, Class)
	 * @see #LineaPostgresqlDAO(ParadaDAO)
	 */
	public LineaPostgresqlDAO() {
		this(Factory.getInstancia("PARADA", ParadaDAO.class));
	}

	/**
	 * Constructor con dependencia de {@link ParadaDAO}.
	 * <p>
	 * Inicializa el DAO de líneas con un {@link ParadaDAO} específico,
	 * carga todas las paradas disponibles y marca que es necesario actualizar
	 * las líneas en la próxima consulta.
	 * </p>
	 *
	 * @param paradaDAO el DAO de paradas a utilizar para obtener las paradas relacionadas con las líneas
	 * @see #cargarParadas()
	 */
	public LineaPostgresqlDAO(ParadaDAO paradaDAO) {
		this.paradaDAO = paradaDAO;
		this.paradas = cargarParadas();

		actualizar = true;
	}

	/**
	 * Carga todas las paradas desde el {@link ParadaDAO}.
	 * <p>
	 * Si ocurre un error de ejecución ({@link RuntimeException}), lo propaga.
	 * Si ocurre cualquier otra excepción, registra una advertencia y retorna un mapa vacío.
	 * </p>
	 *
	 * @return un {@link Map} con las paradas indexadas por su código
	 * @throws RuntimeException si ocurre un error de ejecución al cargar las paradas
	 * @see ParadaDAO#buscarTodos()
	 */
	private Map<Integer, Parada> cargarParadas() {
		Map<Integer, Parada> paradas;
		try {
			paradas = this.paradaDAO.buscarTodos();
		} catch (RuntimeException re) {
			logger.error("Error de ejecución cargando paradas", re);
			throw re;
		} catch (Exception e) {
			logger.warn("No se pudieron cargar las paradas", e);
			paradas = new TreeMap<>();
		}
		return paradas;
	}

	/**
	 * Busca y retorna todas las líneas almacenadas.
	 * <p>
	 * Si es necesario actualizar los datos (primera consulta o después de una actualización),
	 * carga todas las líneas desde la base de datos junto con sus paradas y frecuencias.
	 * Las líneas están indexadas por su código.
	 * </p>
	 *
	 * @return un {@link Map} con todas las líneas indexadas por su código
	 * @throws Exception si ocurre un error al consultar la base de datos
	 * @see #leerBDDLinea()
	 */
	public Map<String, Linea> buscarTodos() throws Exception {
		if (actualizar) {
			lineas = new TreeMap<>();
			try {
				lineas = leerBDDLinea();
				actualizar = false;
			} catch (Exception e) {
				logger.error("Error consultando líneas desde la base de datos", e);
				throw e;
			}
		}
		return lineas;
	}

	/**
	 * Lee todas las líneas desde la base de datos PostgreSQL.
	 * <p>
	 * Ejecuta una consulta SQL sobre la tabla 'linea' para obtener el código y nombre
	 * de cada línea. Crea objetos {@link Linea} y los almacena en el mapa.
	 * Luego invoca {@link #leerBDDLineaParada(Map)} para cargar las paradas asociadas.
	 * </p>
	 *
	 * @return un {@link Map} con todas las líneas leídas desde la base de datos
	 * @throws Exception si ocurre un error al leer desde la tabla 'linea' o 'linea_parada'
	 * @see #leerBDDLineaParada(Map)
	 */
	private Map<String, Linea> leerBDDLinea() throws Exception {

		String sql = "SELECT codigo, nombre FROM linea";
		try (PreparedStatement ps = this.con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				String codigo = rs.getString("codigo");
				String nombre = rs.getString("nombre");

				Linea linea = new Linea(codigo, nombre);

				lineas.put(codigo, linea);
			}

		} catch (Exception e) {
			logger.error("Error leyendo tabla 'linea'", e);
			throw e;
		}
		try {
			leerBDDLineaParada(lineas);
		} catch (Exception e) {
			logger.error("Error leyendo tabla 'linea_parada'", e);
			throw e;
		}

		return lineas;
	}

	/**
	 * Lee la relación entre líneas y paradas desde la base de datos.
	 * <p>
	 * Ejecuta una consulta SQL sobre la tabla 'linea_parada' ordenada por línea y secuencia
	 * para obtener las paradas en el orden correcto. Para cada registro, busca la parada
	 * y la línea correspondientes y establece la relación bidireccional entre ambas.
	 * </p>
	 * <p>
	 * Incluye control de duplicados para líneas circulares, evitando agregar la misma
	 * parada múltiples veces a una línea. También verifica y mantiene la consistencia
	 * bidireccional entre líneas y paradas.
	 * </p>
	 * <p>
	 * Después de cargar las paradas, invoca {@link #leerBDDLineaFrecuencia(Map)} para
	 * cargar las frecuencias de cada línea.
	 * </p>
	 *
	 * @param lineas el {@link Map} de líneas donde se agregarán las paradas
	 * @throws Exception si ocurre un error al leer desde la tabla 'linea_parada' o 'linea_frecuencia'
	 * @see Linea#agregarParada(Parada)
	 * @see Parada#agregarLinea(Linea)
	 * @see #leerBDDLineaFrecuencia(Map)
	 */
	private void leerBDDLineaParada(Map<String, Linea> lineas) throws Exception {

		String sql = "SELECT linea, parada FROM linea_parada ORDER BY linea, secuencia";
		try (PreparedStatement ps = this.con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				String codigo = rs.getString("linea");
				int codigoParada = rs.getInt("parada");

				Parada parada = paradas.get(codigoParada);
				Linea linea = lineas.get(codigo);

				if (parada != null && linea != null) {
					// Control de duplicados para líneas circulares:
					// Solo agregamos la parada si no está ya en la línea
					if (!linea.getParadas().contains(parada)) {
						linea.agregarParada(parada);
						logger.debug("Parada {} agregada a línea {}", codigoParada, codigo);
					} else {
						logger.debug("Parada {} ya existe en línea {}, evitando duplicado", codigoParada, codigo);
					}
					// Control adicional: verificar que la línea esté en la parada
					// (necesario porque agregarParada llama a parada.agregarLinea)
					if (!parada.getLineas().contains(linea)) {
						parada.agregarLinea(linea);
					}
				} else if (parada == null) {
				    logger.warn("Código de parada {} no encontrado para la línea {}", codigoParada, codigo);
				} else {
				    logger.warn("Línea {} inexistente al cargar paradas", codigo);
				}
			}

		} catch (Exception e) {
			logger.error("Error al leer 'linea_parada'", e);
			throw e;
		}

		try {
			leerBDDLineaFrecuencia(lineas);
		} catch (Exception e) {
			logger.error("Error leyendo 'linea_frecuencia'", e);
			throw e;
		}

    }

	/**
	 * Lee las frecuencias de paso de cada línea desde la base de datos.
	 * <p>
	 * Ejecuta una consulta SQL sobre la tabla 'linea_frecuencia' ordenada por línea,
	 * día de semana y hora. Para cada registro, agrega la frecuencia correspondiente
	 * a la línea especificada.
	 * </p>
	 * <p>
	 * Si encuentra un formato de hora inválido, registra una advertencia pero continúa
	 * procesando los demás registros. Si encuentra una frecuencia para una línea
	 * inexistente, también registra una advertencia.
	 * </p>
	 *
	 * @param lineas el {@link Map} de líneas donde se agregarán las frecuencias
	 * @throws Exception si ocurre un error al leer desde la tabla 'linea_frecuencia'
	 * @see Linea#agregarFrecuencia(int, LocalTime)
	 * @see LocalTime
	 * @see DateTimeParseException
	 */
	private void leerBDDLineaFrecuencia(Map<String, Linea> lineas) throws Exception {

		String sql = "SELECT linea, diasemana, hora FROM linea_frecuencia ORDER BY linea, diasemana, hora";
		try (PreparedStatement ps = this.con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				String codigo = rs.getString("linea");
				int diaSemana = rs.getInt("diasemana");
				LocalTime hora = rs.getTime("hora").toLocalTime();

				Linea linea = lineas.get(codigo);

				if (linea != null) {
					try {
					    linea.agregarFrecuencia(diaSemana, hora);
					} catch (DateTimeParseException dtpe) {
					    logger.warn("Formato de hora inválido '{}' para la línea {}", hora, codigo, dtpe);
					}
                } else {
                	logger.warn("Frecuencia para línea inexistente {}", codigo);
                }
			}
        } catch (Exception e) {
            logger.error("Error al leer 'linea_frecuencia'", e);
            throw e;
        }

    }

}

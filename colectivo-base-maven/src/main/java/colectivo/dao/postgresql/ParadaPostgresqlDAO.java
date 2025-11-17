package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.BDConexion;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

/**
 * Implementación de {@link ParadaDAO} que lee paradas desde una base de datos PostgreSQL.
 *
 * <p>Esta implementación del patrón DAO obtiene las paradas desde tablas de base de datos
 * mediante JDBC. Utiliza un mecanismo de caché para evitar recargar los datos en cada consulta,
 * marcándolos como actualizables solo cuando es necesario.</p>
 *
 * <p>Estructura de la tabla esperada:</p>
 * <pre>
 * CREATE TABLE parada (
 *   codigo INTEGER PRIMARY KEY,
 *   direccion VARCHAR,
 *   latitud DOUBLE PRECISION,
 *   longitud DOUBLE PRECISION
 * );
 * </pre>
 *
 * <p>Características:</p>
 * <ul>
 *   <li>Conexión a PostgreSQL mediante {@link BDConexion}</li>
 *   <li>Caché estático compartido entre instancias</li>
 *   <li>Uso de PreparedStatement para prevenir SQL injection</li>
 *   <li>Try-with-resources para gestión automática de recursos JDBC</li>
 *   <li>Manejo robusto de errores con logging detallado</li>
 *   <li>Retorna TreeMap ordenado por código de parada</li>
 * </ul>
 *
 * @see ParadaDAO
 * @see Parada
 * @see BDConexion
 */
public class ParadaPostgresqlDAO implements ParadaDAO {

	private static final Logger logger = LogManager.getLogger(ParadaPostgresqlDAO.class);

	/** Indica si se debe recargar los datos en la próxima consulta. */
	private boolean actualizar;

	/** Caché estático compartido de paradas cargadas. */
	static Map<Integer, Parada> paradas = new TreeMap<Integer, Parada>();

	/** Conexión a la base de datos PostgreSQL. */
	private final Connection con = BDConexion.getConnection();

	/**
	 * Constructor por defecto que inicializa el DAO.
	 *
	 * <p>Marca los datos como pendientes de actualización para la primera consulta.</p>
	 */
	public ParadaPostgresqlDAO() {
		actualizar = true;
	}

	/**
	 * Busca y retorna todas las paradas de transporte disponibles desde la base de datos.
	 *
	 * <p>Implementa el método de {@link ParadaDAO}. Utiliza caché: solo recarga
	 * los datos desde la base de datos si el flag {@code actualizar} está activo.
	 * En consultas sucesivas, retorna el caché sin acceder a la base de datos.</p>
	 *
	 * @return mapa con las {@link Parada} indexadas por su código identificador numérico
	 * @throws Exception si ocurre un error al acceder a la base de datos
	 */
	@Override
	public Map<Integer, Parada> buscarTodos() throws Exception {

		if (actualizar) {
			try {
				paradas = leerBDDParadas();
				actualizar = false;
			} catch (Exception e) {
				logger.error("Error cargando paradas desde la base de datos", e);
				throw e;
			}
		}

		return paradas;
	}

	/**
	 * Lee las paradas desde la tabla 'parada' de la base de datos PostgreSQL.
	 *
	 * <p>Ejecuta una consulta SQL para obtener todas las paradas con sus datos:
	 * código, dirección y coordenadas geográficas. Utiliza PreparedStatement y
	 * ResultSet con try-with-resources para gestión automática de recursos.</p>
	 *
	 * <p>Query ejecutada:</p>
	 * <pre>SELECT codigo, direccion, latitud, longitud FROM parada</pre>
	 *
	 * @return mapa de {@link Parada} indexadas por código, ordenado (TreeMap)
	 * @throws Exception si ocurre un error al ejecutar la consulta SQL o procesar los resultados
	 */
	private Map<Integer, Parada> leerBDDParadas() throws Exception {

		String sql = "SELECT codigo, direccion, latitud, longitud FROM parada";
		try (PreparedStatement ps = this.con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				int codigo = rs.getInt("codigo");
				String direccion = rs.getString("direccion");
				double latitud = rs.getDouble("latitud");
				double longitud = rs.getDouble("longitud");
				Parada parada = new Parada(codigo, direccion, latitud, longitud);
				paradas.put(codigo, parada);
			}
		} catch (Exception e) {
			logger.error("Error leyendo tabla 'parada'", e);
			throw e;
		}

		return paradas;
	}

}

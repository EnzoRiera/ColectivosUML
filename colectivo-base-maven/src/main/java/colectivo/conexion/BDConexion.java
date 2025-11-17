package colectivo.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase de gestión de conexión a base de datos PostgreSQL mediante patrón Singleton.
 * <p>
 * Esta clase proporciona una única instancia de conexión a la base de datos compartida
 * por toda la aplicación. La configuración de la conexión se obtiene desde el archivo
 * de recursos {@code jdbc.properties}.
 * </p>
 * <p>
 * <b>Características principales:</b>
 * <ul>
 *   <li>Implementa el patrón Singleton para garantizar una única conexión</li>
 *   <li>Configura automáticamente el schema de PostgreSQL</li>
 *   <li>Registra un shutdown hook para cerrar la conexión al finalizar la aplicación</li>
 *   <li>Carga la configuración desde {@link ResourceBundle} (archivo jdbc.properties)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Configuración requerida en jdbc.properties:</b>
 * <ul>
 *   <li><b>driver</b> - Nombre completo de la clase del driver JDBC (ej: org.postgresql.Driver)</li>
 *   <li><b>url</b> - URL de conexión a la base de datos</li>
 *   <li><b>usr</b> - Nombre de usuario para la conexión</li>
 *   <li><b>pwd</b> - Contraseña del usuario</li>
 *   <li><b>schema</b> - Esquema de PostgreSQL a utilizar</li>
 * </ul>
 * </p>
 *
 * @see Connection
 * @see DriverManager
 * @see ResourceBundle
 */
public class BDConexion {
	
    private static final Logger logger = LogManager.getLogger(BDConexion.class);

	/** Instancia única de conexión a la base de datos (patrón Singleton). */
	private static Connection con = null;

	/**
	 * Obtiene la instancia única de conexión a la base de datos.
	 * <p>
	 * Si la conexión no existe, la crea utilizando la configuración del archivo
	 * {@code jdbc.properties}. Este método implementa el patrón Singleton con
	 * inicialización lazy (se crea solo cuando se necesita por primera vez).
	 * </p>
	 * <p>
	 * <b>Proceso de inicialización:</b>
	 * <ol>
	 *   <li>Registra un {@link MiShDwnHook} para cerrar la conexión al finalizar la aplicación</li>
	 *   <li>Carga la configuración desde el {@link ResourceBundle} "jdbc"</li>
	 *   <li>Carga dinámicamente el driver JDBC mediante {@link Class#forName(String)}</li>
	 *   <li>Establece la conexión usando {@link DriverManager#getConnection(String, String, String)}</li>
	 *   <li>Configura el schema de PostgreSQL mediante {@code SET search_path}</li>
	 * </ol>
	 * </p>
	 * <p>
	 * <b>Nota:</b> Este método no es thread-safe. En aplicaciones multi-hilo se debe
	 * sincronizar externamente si hay posibilidad de acceso concurrente durante la
	 * primera inicialización.
	 * </p>
	 *
	 * @return la instancia única de {@link Connection} a la base de datos
	 * @throws RuntimeException si ocurre un error al cargar el driver, establecer la conexión,
	 *         o configurar el schema
	 * @see ResourceBundle#getBundle(String)
	 * @see DriverManager#getConnection(String, String, String)
	 * @see Statement#execute(String)
	 */
	public static Connection getConnection() {
		try {
			if (con == null) {
				// con esto determinamos cuando finalize el programa
				Runtime.getRuntime().addShutdownHook(new MiShDwnHook());
				ResourceBundle rb = ResourceBundle.getBundle("jdbc");
				String driver = rb.getString("driver");
				String url = rb.getString("url");
				String usr = rb.getString("usr");
				String pwd = rb.getString("pwd");
				String schema = rb.getString("schema");
				Class.forName(driver);
				con = DriverManager.getConnection(url, usr, pwd);
                logger.debug("Conexión establecida. URL: {}, usuario: {}", url, usr);
				
				Statement statement = con.createStatement();				
				try {
					statement.execute("set search_path to '" + schema + "'");
                    logger.debug("Schema configurado: {}", schema);
				} finally {
					try {
						statement.close();
					} catch (Exception e) {
						logger.error("Error al cerrar el Statement", e);
					}
				}
			}
			return con;
		} catch (Exception ex) {
			logger.error("Error al crear la conexión", ex);
			throw new RuntimeException("Error al crear la conexion", ex);
		}
	}

	/**
	 * Clase interna que implementa un shutdown hook para cerrar la conexión a la base de datos.
	 * <p>
	 * Esta clase extiende {@link Thread} y se registra automáticamente en el {@link Runtime}
	 * durante la inicialización de la conexión. La JVM invocará el método {@link #run()}
	 * justo antes de finalizar el programa, permitiendo cerrar ordenadamente la conexión
	 * a la base de datos y liberar recursos.
	 * </p>
	 * <p>
	 * <b>Comportamiento:</b> Si ocurre un error al cerrar la conexión, se registra en el
	 * log y se lanza una {@link RuntimeException}, aunque esto rara vez tiene efecto
	 * práctico dado que la JVM está en proceso de finalización.
	 * </p>
	 *
	 * @see Thread
	 * @see Runtime#addShutdownHook(Thread)
	 * @see Connection#close()
	 */
	public static class MiShDwnHook extends Thread {
		/**
		 * Método ejecutado por la JVM al finalizar el programa.
		 * <p>
		 * Obtiene la conexión actual y la cierra de forma ordenada. Este método
		 * es invocado automáticamente por la JVM como parte del proceso de shutdown
		 * cuando se ha registrado este thread como shutdown hook.
		 * </p>
		 * <p>
		 * <b>Secuencia de ejecución:</b>
		 * <ol>
		 *   <li>Obtiene la instancia de conexión mediante {@link BDConexion#getConnection()}</li>
		 *   <li>Registra en el log que se está cerrando la conexión</li>
		 *   <li>Cierra la conexión mediante {@link Connection#close()}</li>
		 * </ol>
		 * </p>
		 *
		 * @throws RuntimeException si ocurre un error al cerrar la conexión
		 * @see Connection#close()
		 */
		public void run() {
			try {
				Connection con = BDConexion.getConnection();
				logger.debug("Cerrando conexión a la base de datos");
				con.close();
			} catch (Exception ex) {
				logger.error("Error al cerrar la conexión en shutdown hook", ex);
				throw new RuntimeException(ex);
			}
		}
	}
}

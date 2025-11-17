package colectivo.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BDConexion {
	
    private static final Logger logger = LogManager.getLogger(BDConexion.class);
	private static Connection con = null;

	// Nos conectamos a la base de datos (con los datos de conexión del archivo
	// jdbc.properties)
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
				logger.debug("Conexión establecida. URL: " + url + ", usuario: " + usr);

				Statement statement = con.createStatement();
				try {
					statement.execute("set search_path to '" + schema + "'");
					logger.debug("Schema configurado: " + schema);
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

	public static class MiShDwnHook extends Thread {
		// justo antes de finalizar el programa la JVM invocara
		// a este metodo donde podemos cerrar la conexion
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

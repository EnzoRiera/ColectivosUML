package colectivo.datos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads application file parameters from a configuration file.
 *
 * <p>
 * This utility class reads the properties from `config.properties` and exposes
 * the configured filenames used by the data layer: the files for lines
 * ({@code archivoLinea}), stops ({@code archivoParada}), segments
 * ({@code archivoTramo}) and frequencies ({@code archivoFrecuencia}).
 * The static method {@link #parametros()} performs the loading and may throw
 * {@link IOException} if the configuration file cannot be read.</p>
 *
 * <p>
 * The class provides simple static getters to access the loaded values after
 * initialization. This class is part of the original course-provided code and
 * has not been otherwise modified.</p>
 *
 * @author POO-2025
 * @version 1.0
 * @since 1.0
 */
public class CargarParametros {

	private static String archivoLinea;
	private static String archivoParada;
	private static String archivoTramo;
	private static String archivoFrecuencia;

	/**
	 * Carga los parametros del archivo "config.properties"
	 * 
	 * @throws IOException
	 */
	public static void parametros() throws IOException {

		Properties prop = new Properties();
		InputStream input = new FileInputStream("config.properties");
		prop.load(input);
		archivoLinea = prop.getProperty("linea");
		archivoParada = prop.getProperty("parada");
		archivoTramo = prop.getProperty("tramo");
		archivoFrecuencia = prop.getProperty("frecuencia");

	}

	public static String getArchivoLinea() {
		return archivoLinea;
	}

	public static String getArchivoParada() {
		return archivoParada;
	}

	public static String getArchivoTramo() {
		return archivoTramo;
	}

	public static String getArchivoFrecuencia() {
		return archivoFrecuencia;
	}

}

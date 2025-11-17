package colectivo.datos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class CargarParametros {

	private static String archivoLinea;
	private static String archivoParada;
	private static String archivoTramo;
	private static String archivoFrecuencia;


	public static void parametros() throws IOException {

		Properties prop = new Properties();
		
		// Cargar desde classpath (compatible con Maven)
		InputStream input = CargarParametros.class.getClassLoader()
				.getResourceAsStream("config.properties");

		if (input == null) {
			throw new IOException("No se encontró el archivo 'config.properties' en el classpath");
		}

		try {
			prop.load(input);
			archivoLinea = prop.getProperty("linea");
			archivoParada = prop.getProperty("parada");
			archivoTramo = prop.getProperty("tramo");
			archivoFrecuencia = prop.getProperty("frecuencia");
		} finally {
			input.close();
		}

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

package colectivo.test.cargadatosincremento1;

import java.io.IOException;
import java.util.Map;

import colectivo.datos.CargarDatos;
import colectivo.datos.CargarParametros;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Test application that loads data from text files and prints to the console the
 * number of records loaded for each type.
 *
 * <p>
 * This class is used manually to verify the behavior of the loading process
 * when the files contain invalid entries, empty lines, or incorrect numbers
 * of fields. Internally it invokes methods from {@link CargarParametros} and
 * {@link CargarDatos} and presents a summary on standard output.
 * </p>
 *
 * @since 1.0
 */
public class DemoCargaDatos {

	/**
	 * Executes the loading sequence and displays the counts of loaded records.
	 *
	 * <p>
	 * The method:
	 * <ol>
	 *   <li>loads parameters via {@link CargarParametros#parametros()}</li>
	 *   <li>loads stops using {@link CargarDatos#cargarParadas(String)}</li>
	 *   <li>loads lines using {@link CargarDatos#cargarLineas(String, String, Map)}</li>
	 *   <li>loads segments using {@link CargarDatos#cargarTramos(String, Map)}</li>
	 * </ol>
	 * </p>
	 *
	 * <p>
	 * Any {@link IOException} produced while reading files is caught here;
	 * an error message is printed to the error output and the process exits
	 * with code {@code -1} to facilitate manual testing.
	 * </p>
	 */
	public void ejecutar() {
		try {
			CargarParametros.parametros();

			Map<Integer, Parada> paradas = CargarDatos.cargarParadas(CargarParametros.getArchivoParada());

			Map<String, Linea> lineas = CargarDatos.cargarLineas(CargarParametros.getArchivoLinea(),
					CargarParametros.getArchivoFrecuencia(), paradas);

			Map<String, Tramo> tramos = CargarDatos.cargarTramos(CargarParametros.getArchivoTramo(), paradas);

			int cantParadas = (paradas != null) ? paradas.size() : 0;
			int cantLineas = (lineas != null) ? lineas.size() : 0;
			int cantTramos = (tramos != null) ? tramos.size() : 0;

			System.out.println("=== Resultado de la carga de datos ===");
			System.out.println("Paradas cargadas : " + cantParadas);
			System.out.println("Líneas cargadas  : " + cantLineas);
			System.out.println("Tramos cargados  : " + cantTramos);

		} catch (IOException e) {
			System.err.println("Error al cargar parametros o datos: " + e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Entry point to run this test application from the command line.
	 *
	 * @param args command line arguments (not used)
	 */
	public static void main(String[] args) {
		new DemoCargaDatos().ejecutar();
	}
}
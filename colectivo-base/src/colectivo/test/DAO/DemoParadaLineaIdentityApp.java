package colectivo.test.DAO;

import java.util.Map;

import colectivo.aplicacion.Ciudad;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

/**
 * Demo class to verify referential identity of domain objects after data load.
 *
 * <p>
 * This small application inspects the in-memory model loaded by the system and
 * prints simple checks that demonstrate:
 * <ul>
 * <li>That {@code Parada} instances returned by {@code Ciudad} and the shared
 * {@code ParadaDAO} are the same Java objects (no duplicates).</li>
 * <li>That a {@code Linea} references the shared {@code Parada} instances in
 * its recorrido (route).</li>
 * <li>That another {@code Linea} referencing the same parada code uses the same
 * object reference (example: code 88 appears in multiple lines).</li>
 * </ul>
 * </p>
 *
 * <p>
 * This demo is intended for manual execution during development. It prints
 * readable messages to standard output and uses simple identity (==) checks.
 * </p>
 *
 * @see colectivo.aplicacion.Ciudad
 * @see colectivo.dao.ParadaDAO
 * @see colectivo.modelo.Linea
 * @since 1.0
 */
public class DemoParadaLineaIdentityApp {

	/**
	 * Main entry point for the demo.
	 *
	 * <p>
	 * Steps performed:
	 * <ol>
	 * <li>Obtain the singleton {@code Ciudad} instance and its loaded
	 * collections.</li>
	 * <li>Pick the first loaded {@code Linea} and its first {@code Parada}.</li>
	 * <li>Compare the {@code Parada} instance from the {@code Linea} with the
	 * instance returned by the shared {@code ParadaDAO} (identity check).</li>
	 * <li>Search for another {@code Linea} that references the same parada code and
	 * compare references to demonstrate reuse across lineas.</li>
	 * <li>Optionally, search {@code Tramo} objects for a tramo that starts at the
	 * same parada and verify identity.</li>
	 * </ol>
	 * </p>
	 *
	 * @param args Command line arguments (ignored)
	 */
	public static void main(String[] args) {
		try {

			/* Obtain the singleton Ciudad and the maps of paradas and lineas. */
			Ciudad ciudad = Ciudad.getCiudad();
			Map<Integer, Parada> ciudadParadas = ciudad.getParadas();
			Map<String, Linea> lineas = ciudad.getLineas();

			/* Guard: if there are no lineas loaded, print a message and exit early. */
			if (lineas == null || lineas.isEmpty()) {
				System.out.println("No lines loaded.");
				return;
			}

			/* Select the first Linea available and obtain its first parada (if any). */
			Linea firstLinea = lineas.values().iterator().next();
			Parada primeraParada = firstLinea.getParadas().isEmpty() ? null : firstLinea.getParadas().get(0);

			/* Print basic information: first linea code and its first parada code. */
			System.out.println("First Line loaded: " + firstLinea.getCodigo());

			if (primeraParada == null) {
				System.out.println("First line has no paradas.");
				return;
			}

			System.out.println("First parada code in that line: " + primeraParada.getCodigo());

			/* Retrieve the shared ParadaDAO from the Factory and get the Parada by code. */
			/*
			 * Print the result of the reference equality check between the Linea's parada
			 * and the DAO's parada.
			 */
			ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
			Parada pFromDAO = paradaDAO.buscarTodos().get(primeraParada.getCodigo());

			System.out.println("Ciudad vs DAO same instance: " + (primeraParada == pFromDAO));

			/*
			 * Search for another Linea (different object) that contains the same parada
			 * code.
			 */
			/*
			 * If found, obtain that Parada instance and print whether both references are
			 * identical.
			 */
			/* If not found, print that no other line references the same parada code. */
			Linea other = null;
			for (Linea l : lineas.values()) {
				if (l == firstLinea)
					continue;
				if (l.getParadas().stream().anyMatch(p -> p.getCodigo() == primeraParada.getCodigo())) {
					other = l;
					break;
				}
			}

			if (other != null) {
				Parada pInOther = other.getParadas().stream().filter(p -> p.getCodigo() == primeraParada.getCodigo())
						.findFirst().orElse(null);
				System.out.println("Found another line that references same parada: " + other.getCodigo());
				System.out.println("Reference equality between first line's parada and other line's parada: "
						+ (primeraParada == pInOther));
			} else {
				System.out.println("No other line references the same parada code.");
			}

			/*
			 * Optional: obtain the TramoDAO and iterate tramos to find one whose 'inicio'
			 * has the same codigo.
			 */
			/*
			 * For the first match, print whether the tramo's inicio reference equals the
			 * Ciudad's Parada instance.
			 */
			/* If no matching tramo is found, print an informative message. */
			TramoDAO tramoDAO = Factory.getInstancia("TRAMO", TramoDAO.class);
			Map<String, Tramo> tramos = tramoDAO.buscarTodos();
			boolean tramoMatch = false;
			for (Tramo t : tramos.values()) {
				if (t.getInicio().getCodigo() == primeraParada.getCodigo()) {
					System.out.println("Tramo found with same inicio parada. Identity: "
							+ (t.getInicio() == ciudadParadas.get(primeraParada.getCodigo())));
					tramoMatch = true;
					break;
				}
			}
			if (!tramoMatch) {
				System.out.println("No tramo starts with the sample parada code.");
			}

		} catch (Exception e) {
			/*
			 * General catch: on any exception, print stack trace and an error message for
			 * debugging.
			 */
			e.printStackTrace();
			System.err.println("Demo failed: " + e.getMessage());
		}
	}
}
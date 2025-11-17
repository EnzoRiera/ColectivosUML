package colectivo.test.DAO;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Map;

import org.junit.jupiter.api.Test;

import colectivo.aplicacion.Ciudad;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

/**
 * Unit tests that verify instance identity and referential integrity between
 * {@link Parada}, {@link Linea} and {@link Tramo} objects in the in-memory
 * model.
 *
 * <p>
 * These tests ensure that when the application loads data all references to a
 * given parada use the same Java object instance (no duplicates), and that
 * Linea and Tramo objects hold references to the shared Parada instances.
 * </p>
 *
 * <p>
 * Rationale:
 * <ul>
 * <li>Ensures a single canonical set of {@code Parada} objects is reused across
 * the system.</li>
 * <li>Detects improper re-creation or cloning of {@code Parada} instances
 * during loading.</li>
 * </ul>
 * </p>
 *
 * @since 1.0
 */
public class TestParadaLineaIdentity {

	/**
	 * Verifies that a {@code Parada} instance referenced by a {@code Linea} is the
	 * same instance returned by the shared {@code ParadaDAO} and that other Linea
	 * objects referencing the same parada code use the identical object.
	 *
	 * <p>
	 * Condition: Data is loaded via {@code Ciudad.getCiudad()} and the DAO factory.
	 * Expected: The {@code Parada} from the first loaded {@code Linea}, the
	 * {@code ParadaDAO} and any other {@code Linea} referencing the same code are
	 * the same object instance.
	 * </p>
	 *
	 * @throws Exception if loading the city or DAOs fails
	 */
	@Test
	public void testParadaAndLineaReferencesAreShared() throws Exception {
		Ciudad ciudad = Ciudad.getCiudad();
		Map<Integer, Parada> ciudadParadas = ciudad.getParadas();
		Map<String, Linea> lineas = ciudad.getLineas();

		assertNotNull(ciudadParadas);
		assertNotNull(lineas);
		assertNotNull(lineas.values().iterator().next(), "At least one Linea must be loaded");

		Linea firstLinea = lineas.values().iterator().next();
		Parada primeraParada = firstLinea.getParadas().get(0);
		assertNotNull(primeraParada);

		ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
		Parada pFromDAO = paradaDAO.buscarTodos().get(primeraParada.getCodigo());
		// Ciudad's parada and DAO's parada must be the same instance
		assertSame(primeraParada, pFromDAO);

		// Find another line that contains same parada code and assert same instance
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
			assertNotNull(pInOther);
			assertSame(primeraParada, pInOther);
		}

		// If there is any Tramo that uses this parada as inicio, it must reference the
		// same instance
		TramoDAO tramoDAO = Factory.getInstancia("TRAMO", TramoDAO.class);
		Map<String, Tramo> tramos = tramoDAO.buscarTodos();
		for (Tramo t : tramos.values()) {
			if (t.getInicio().getCodigo() == primeraParada.getCodigo()) {
				assertSame(t.getInicio(), ciudadParadas.get(primeraParada.getCodigo()));
			}
		}
	}

	/**
	 * Verifies that the {@code Parada} instances returned by {@code Ciudad} and by
	 * the {@code ParadaDAO} are identical and that a {@code Tramo}'s start parada
	 * references the same shared instance.
	 *
	 * <p>
	 * Condition: City and DAOs have loaded data. Expected: identity equality (==)
	 * between instances retrieved from different access points.
	 * </p>
	 *
	 * @throws Exception if loading the city or DAOs fails
	 */
	@Test
	public void testParadaInstancesAreShared() throws Exception {
		Ciudad ciudad = Ciudad.getCiudad();
		Map<Integer, Parada> ciudadParadas = ciudad.getParadas();
		assertNotNull(ciudadParadas);
		assertNotNull(ciudadParadas.values().iterator().next());

		ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
		Map<Integer, Parada> daoParadas = paradaDAO.buscarTodos();
		assertNotNull(daoParadas);

		Integer sampleCode = ciudadParadas.keySet().iterator().next();
		Parada pFromCiudad = ciudadParadas.get(sampleCode);
		Parada pFromDAO = daoParadas.get(sampleCode);

		// They must be the same object instance
		assertSame(pFromCiudad, pFromDAO, "Parada from Ciudad and ParadaDAO should be the same instance");

		TramoDAO tramoDAO = Factory.getInstancia("TRAMO", TramoDAO.class);
		Map<String, Tramo> tramos = tramoDAO.buscarTodos();
		if (!tramos.isEmpty()) {
			Tramo any = tramos.values().iterator().next();
			Parada inicioFromTramo = any.getInicio();
			assertSame(inicioFromTramo, ciudadParadas.get(inicioFromTramo.getCodigo()),
					"Tramo.start Parada should be the same instance as Ciudad's Parada");
		}
	}
}

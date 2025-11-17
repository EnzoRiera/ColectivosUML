package colectivo.dao;

import java.util.Map;

import colectivo.modelo.Parada;

/**
 * DAO interface for accessing bus stop (Parada) data.
 *
 * <p>Implementations provide access to the available bus stops in the system.
 * Different persistence mechanisms may be used (files, databases, remote APIs, etc.).</p>
 *
 * <p>Contract and guarantees:
 * <ul>
 *   <li>The {@link #buscarTodos()} method returns a {@code Map} keyed by the stop id
 *       (an {@code Integer}) with values of type {@link Parada}.</li>
 *   <li>The returned map <b>must not</b> be {@code null}. If no stops are available,
 *       an empty map should be returned.</li>
 *   <li>Mutability and concurrency semantics of the returned map depend on the implementation
 *       and should be documented by the implementor. The interface does not mandate thread-safety.</li>
 * </ul>
 * </p>
 *
 * @author POO-2025
 * @version 1.0
 * @since 1.0
 * @see colectivo.modelo.Parada
 */
public interface ParadaDAO {
	
	/**
	 * Retrieves all available bus stops.
	 *
	 * <p>The map keys are stop identifiers (Integer) and the values are {@link Parada}
	 * instances. Implementations must not return {@code null}; return an empty map if
	 * there are no stops. Implementations should document their concurrency and
	 * transactional behavior.</p>
	 *
	 * @return a non-null {@code Map<Integer, Parada>} mapping stop ids to {@link Parada} objects;
	 *         the map may be empty if no stops are available.
	 * @throws Exception if an error occurs while accessing or reading the data (I/O, DB, parsing, etc.).
	 */
	Map<Integer, Parada> buscarTodos() throws Exception;
}

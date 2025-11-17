package colectivo.dao;

import java.util.Map;

import colectivo.modelo.Linea;

/**
 * DAO interface for accessing bus line data.
 *
 * <p>Implementations provide access to the available bus lines in the system.
 * Different persistence mechanisms may be used (files, databases, remote APIs, etc.).</p>
 *
 * <p>Contract and guarantees:
 * <ul>
 *   <li>The {@link #buscarTodos()} method returns a {@code Map} keyed by the line code
 *       (for example {@code "22"}, {@code "101A"}) and with values of type {@link Linea}.</li>
 *   <li>The returned map <b>must not</b> be {@code null}. If no lines are available, an
 *       empty map should be returned.</li>
 *   <li>Mutability of the returned map depends on the implementation and should be documented
 *       by the implementor. The interface does not mandate thread-safety.</li>
 * </ul>
 * </p>
 *
 * @author POO-2025
 * @version 1.0
 * @since 1.0
 * @see colectivo.modelo.Linea
 */
public interface LineaDAO {
	
    /**
     * Retrieves all available bus lines.
     *
     * <p>The map keys are line codes and the values are {@link Linea} instances.
     * Implementations must not return {@code null}; return an empty map if there are
     * no lines. Implementations should document their concurrency and transactional
     * behavior.</p>
     *
     * @return a non-null {@code Map<String, Linea>} mapping line codes to {@link Linea} objects.
     *         The map may be empty if no lines are available.
     * @throws Exception if an error occurs while accessing or reading the data (I/O, DB, parsing, etc.).
     */
    Map<String, Linea> buscarTodos() throws Exception;
}

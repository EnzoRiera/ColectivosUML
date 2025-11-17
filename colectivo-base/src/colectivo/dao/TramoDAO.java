package colectivo.dao;

import java.util.Map;

import colectivo.modelo.Tramo;

/**
 * DAO interface for accessing route segment (Tramo) data.
 *
 * <p>Implementations provide access to the available route segments in the system.
 * Different persistence mechanisms may be used (files, databases, remote APIs, etc.).</p>
 *
 * <p>Contract and guarantees:
 * <ul>
 *   <li>Each map key follows the format {@code "<origin>-<destination>-<type>"} where:
 *       <ul>
 *         <li>{@code origin} and {@code destination} are stop codes (for example: {@code 88}, {@code 97}).</li>
 *         <li>{@code type} is a numeric segment type where:
 *             <ul>
 *               <li>{@code 1} = bus combination (example key: {@code "88-97-1"})</li>
 *               <li>{@code 2} = walking combination (example key: {@code "88-97-2"})</li>
 *             </ul>
 *         </li>
 *       </ul>
 *   </li>
 *   <li>The {@link #buscarTodos()} method returns a {@code Map} keyed by that string and with
 *       values of type {@link Tramo}.</li>
 *   <li>Each {@link Tramo} instance is expected to include the segment duration and references
 *       to the origin and destination {@code Parada} objects (implementation-dependent).</li>
 *   <li>The returned map <b>must not</b> be {@code null}. If no segments are available, an
 *       empty map should be returned.</li>
 *   <li>Mutability and concurrency semantics of the returned map depend on the implementation
 *       and should be documented by the implementor. The interface does not mandate thread-safety.</li>
 * </ul>
 * </p>
 *
 * @author POO-2025
 * @version 1.0
 * @since 1.0
 * @see colectivo.modelo.Tramo
 */
public interface TramoDAO {
	
    /**
     * Retrieves all available route segments (tramos).
     *
     * <p>The map keys are strings in the form {@code "<origin>-<destination>-<type>"} (for example:
     * {@code "88-97-1"}). Values are {@link Tramo} instances which should contain duration and
     * the origin/destination {@code Parada} references. Implementations must not return {@code null};
     * return an empty map if there are no segments. Implementations should document their concurrency
     * and transactional behavior.</p>
     *
     * @return a non-null {@code Map<String, Tramo>} mapping tramo keys (including type suffix)
     *         to {@link Tramo} objects; the map may be empty if no segments are available.
     * @throws Exception if an error occurs while accessing or reading the data (I/O, DB, parsing, etc.).
     */
    Map<String, Tramo> buscarTodos() throws Exception;
}

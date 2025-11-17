package colectivo.servicio;

import java.util.Map;

import colectivo.modelo.Tramo;

/**
 * Service interface for retrieving route segment (\`Tramo\`) data.
 *
 * <p>
 * Implementations provide access to the collection of available {@link Tramo}
 * objects in the system. This interface belongs to the service layer and
 * typically delegates to DAO/facade classes for persistence. Obtain concrete
 * implementations via dependency injection or the system initializer
 * (for example, {@code InicializadorSistema.iniciar()}).
 * </p>
 *
 * @since 1.0
 */
public interface TramoService {

    /**
     * Retrieves all route segments available in the system.
     *
     * @return a {@link Map} where the key is the tramo code (origin-destination)
     *         and the value is the corresponding {@link Tramo}
     * @throws Exception if an error occurs while accessing the data source
     */
    Map<String, Tramo> buscarTodos() throws Exception;

}

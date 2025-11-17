package colectivo.servicio;

import java.util.Map;

import colectivo.modelo.Parada;

/**
 * Service interface for retrieving stop (\`Parada\`) data.
 *
 * <p>
 * Implementations provide access to the collection of available {@link Parada}
 * objects in the system. This interface belongs to the service layer and usually
 * delegates to DAO/facade classes for persistence. Obtain concrete implementations
 * via dependency injection or the system initializer (for example,
 * {@code InicializadorSistema.iniciar()}).
 * </p>
 *
 * @since 1.0
 */
public interface ParadaService {

    /**
     * Retrieves all stops available in the system.
     *
     * @return a {@link Map} where the key is the stop code (Integer) and the value is the corresponding {@link Parada}
     * @throws Exception if an error occurs while accessing the data source
     */
    Map<Integer, Parada> buscarTodos() throws Exception;
}

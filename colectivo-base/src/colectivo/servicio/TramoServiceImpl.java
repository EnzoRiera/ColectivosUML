package colectivo.servicio;

import java.util.Map;

import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

/**
 * Default service implementation for {@link TramoService}.
 *
 * <p>
 * This class delegates to a {@link TramoDAO} obtained from the application's
 * {@link Factory}. It provides a thin service layer over DAO operations and is
 * suitable for use by the system initializer or dependency injection.
 * </p>
 *
 * @since 1.0
 * @see TramoService
 */
public class TramoServiceImpl implements TramoService {

    /**
     * DAO used to access persisted {@link Tramo} data.
     *
     * <p>
     * The concrete DAO implementation is provided by {@link Factory} at
     * construction time. Implementations obtained from the factory must
     * implement {@link TramoDAO}.
     * </p>
     */
    private TramoDAO tramoDAO;

    /**
     * Constructs a new {@code TramoServiceImpl}.
     *
     * <p>
     * The constructor obtains a {@link TramoDAO} instance from the {@link Factory}
     * using the key {@code "TRAMO"}. The factory is expected to return a fully
     * initialized DAO compatible with the {@link TramoDAO} interface.
     * </p>
     */
    public TramoServiceImpl() {
        // Factory automáticamente usará el constructor correcto
        this.tramoDAO = Factory.getInstancia("TRAMO", TramoDAO.class);
    }

    /**
     * Retrieves all route segments by delegating to the configured {@link TramoDAO}.
     *
     * @return a map of tramo identifiers to {@link Tramo} instances
     * @throws Exception if an error occurs while accessing the underlying data source
     */
    @Override
    public Map<String, Tramo> buscarTodos() throws Exception {
        return tramoDAO.buscarTodos();
    }
}

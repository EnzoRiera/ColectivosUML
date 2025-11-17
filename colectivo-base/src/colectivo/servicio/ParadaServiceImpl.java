package colectivo.servicio;

import java.util.Map;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import colectivo.util.Factory;

/**
 * Default service implementation for {@link ParadaService}.
 *
 * <p>
 * This class delegates to a {@link ParadaDAO} obtained from the application's
 * {@link Factory}. It provides a thin service layer over DAO operations and is
 * suitable for use by the system initializer or dependency injection.
 * </p>
 *
 * @since 1.0
 * @see ParadaService
 */
public class ParadaServiceImpl implements ParadaService {

	/**
	 * DAO used to access persisted {@link Parada} data.
	 *
	 * <p>
	 * The concrete DAO implementation is provided by {@link Factory} at
	 * construction time. Implementations obtained from the factory must implement
	 * {@link ParadaDAO}.
	 * </p>
	 */
	private ParadaDAO paradaDAO;

	/**
	 * Constructs a new {@code ParadaServiceImpl}.
	 *
	 * <p>
	 * The constructor obtains a {@link ParadaDAO} instance from the {@link Factory}
	 * using the key {@code "PARADA"}. The factory is expected to return a fully
	 * initialized DAO compatible with the {@link ParadaDAO} interface.
	 * </p>
	 */
	public ParadaServiceImpl() {
		paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
	}

	/**
	 * Retrieves all stops by delegating to the configured {@link ParadaDAO}.
	 *
	 * @return a map of stop codes to {@link Parada} instances
	 * @throws Exception if an error occurs while accessing the underlying data
	 *                   source
	 */
	@Override
	public Map<Integer, Parada> buscarTodos() throws Exception {
		return paradaDAO.buscarTodos();
	}

}

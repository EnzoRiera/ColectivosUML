package colectivo.servicio;

import java.util.Map;

import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.util.Factory;

/**
 * Default service implementation for {@link LineaService}.
 *
 * <p>
 * This class delegates to a {@link LineaDAO} obtained from the application's
 * {@link Factory}. It provides a thin service layer over DAO operations and is
 * suitable for use by the system initializer or dependency injection.
 * </p>
 *
 * @since 1.0
 * @see LineaService
 */
public class LineaServiceImpl implements LineaService {

	/**
	 * DAO used to access persisted {@link Linea} data.
	 *
	 * <p>
	 * The concrete DAO implementation is provided by {@link Factory} at
	 * construction time. Implementations obtained from the factory must implement
	 * {@link LineaDAO}.
	 * </p>
	 */
	private LineaDAO lineaDAO;

	/**
	 * Constructs a new {@code LineaServiceImpl}.
	 *
	 * <p>
	 * The constructor obtains a {@link LineaDAO} instance from the {@link Factory}
	 * using the key \"LINEA\". The factory is expected to return a fully
	 * initialized DAO compatible with the {@link LineaDAO} interface.
	 * </p>
	 */
	public LineaServiceImpl() {
		// Factory automatically provides the correct implementation
		this.lineaDAO = Factory.getInstancia("LINEA", LineaDAO.class);
	}

	/**
	 * Retrieves all bus lines by delegating to the configured {@link LineaDAO}.
	 *
	 * @return a map of line codes to {@link Linea} instances
	 * @throws Exception if an error occurs while accessing the underlying data
	 *                   source
	 */
	@Override
	public Map<String, Linea> buscarTodos() throws Exception {
		return lineaDAO.buscarTodos();
	}

}

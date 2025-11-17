package colectivo.servicio;

import java.util.Map;

import colectivo.modelo.Linea;

/**
 * Service interface for retrieving bus line data.
 *
 * <p>
 * Implementations of this interface provide access to the collection of
 * available {@link Linea} objects in the system. This interface is part of
 * the service layer and typically delegates to DAO/facade classes to obtain
 * persistence data. Use the system initializer or dependency injection to
 * obtain a concrete implementation at runtime.
 * </p>
 *
 * @since 1.0
 */
public interface LineaService {

	/**
	 * Retrieves all bus lines available in the system.
	 *
	 * @return a {@link Map} where the key is the line code and the value is the corresponding {@link Linea}
	 * @throws Exception if an error occurs while accessing the data source
	 */
	Map<String, Linea> buscarTodos() throws Exception;

}

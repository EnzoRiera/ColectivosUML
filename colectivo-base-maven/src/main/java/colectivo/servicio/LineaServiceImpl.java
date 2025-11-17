package colectivo.servicio;

import java.util.Map;

import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.util.Factory;

/**
 * Implementación del servicio de gestión de líneas de transporte.
 * Delega las operaciones de acceso a datos al {@link LineaDAO} correspondiente,
 * obtenido dinámicamente mediante {@link Factory} según la configuración del sistema.
 * Proporciona una capa de abstracción entre la lógica de negocio y el acceso a datos.
 *
 * @see LineaService
 * @see LineaDAO
 * @see Linea
 * @see Factory
 */
public class LineaServiceImpl implements LineaService {

	private final LineaDAO lineaDAO;

	/**
	 * Constructor que inicializa el servicio obteniendo la implementación
	 * de DAO de líneas configurada a través de la fábrica.
	 */
	public LineaServiceImpl() {
		// Factory automatically provides the correct implementation
		this.lineaDAO = Factory.getInstancia("LINEA", LineaDAO.class);
	}

	/**
	 * Busca y retorna todas las líneas de transporte disponibles.
	 *
	 * @return mapa con las {@link Linea} indexadas por su código identificador
	 * @throws Exception si ocurre un error al acceder a los datos
	 */
	@Override
	public Map<String, Linea> buscarTodos() throws Exception {
		return lineaDAO.buscarTodos();
	}

}

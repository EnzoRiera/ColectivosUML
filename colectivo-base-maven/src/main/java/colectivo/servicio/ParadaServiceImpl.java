package colectivo.servicio;

import java.util.Map;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import colectivo.util.Factory;

/**
 * Implementación del servicio de gestión de paradas de transporte.
 * Delega las operaciones de acceso a datos al {@link ParadaDAO} correspondiente,
 * obtenido dinámicamente mediante {@link Factory} según la configuración del sistema.
 * Proporciona una capa de abstracción entre la lógica de negocio y el acceso a datos.
 *
 * @see ParadaService
 * @see ParadaDAO
 * @see Parada
 * @see Factory
 */
public class ParadaServiceImpl implements ParadaService {

	private final ParadaDAO paradaDAO;

	/**
	 * Constructor que inicializa el servicio obteniendo la implementación
	 * de DAO de paradas configurada a través de la fábrica.
	 */
	public ParadaServiceImpl() {
		paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
	}

	/**
	 * Busca y retorna todas las paradas de transporte disponibles.
	 *
	 * @return mapa con las {@link Parada} indexadas por su código identificador numérico
	 * @throws Exception si ocurre un error al acceder a los datos
	 */
	@Override
	public Map<Integer, Parada> buscarTodos() throws Exception {
		return paradaDAO.buscarTodos();
	}

}

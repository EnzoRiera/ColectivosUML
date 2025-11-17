package colectivo.servicio;

import java.util.Map;

import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

/**
 * Implementación del servicio de gestión de tramos de transporte.
 * Delega las operaciones de acceso a datos al {@link TramoDAO} correspondiente,
 * obtenido dinámicamente mediante {@link Factory} según la configuración del sistema.
 * Proporciona una capa de abstracción entre la lógica de negocio y el acceso a datos.
 *
 * @see TramoService
 * @see TramoDAO
 * @see Tramo
 * @see Factory
 */
public class TramoServiceImpl implements TramoService {

    private final TramoDAO tramoDAO;

    /**
     * Constructor que inicializa el servicio obteniendo la implementación
     * de DAO de tramos configurada a través de la fábrica.
     */
    public TramoServiceImpl() {
        // Factory automáticamente usará el constructor correcto
        this.tramoDAO = Factory.getInstancia("TRAMO", TramoDAO.class);
    }

    /**
     * Busca y retorna todos los tramos de conexión disponibles.
     *
     * @return mapa con los {@link Tramo} indexados por su identificador único
     * @throws Exception si ocurre un error al acceder a los datos
     */
    @Override
    public Map<String, Tramo> buscarTodos() throws Exception {
        return tramoDAO.buscarTodos();
    }
}

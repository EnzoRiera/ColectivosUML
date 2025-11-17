package colectivo.dao;

import java.util.Map;

import colectivo.modelo.Tramo;

/**
 * Interfaz DAO (Data Access Object) para acceso a datos de tramos de transporte.
 *
 * <p>Define el contrato para las operaciones de acceso a datos relacionadas con
 * {@link Tramo}. Las implementaciones concretas pueden obtener los datos desde
 * diferentes fuentes: archivos de texto, bases de datos, servicios web, etc.</p>
 *
 * <p>Patrón DAO que separa la lógica de acceso a datos de la lógica de negocio,
 * permitiendo cambiar la fuente de datos sin afectar al resto de la aplicación.</p>
 *
 * @see Tramo
 */
public interface TramoDAO {
	
    /**
     * Busca y retorna todos los tramos de conexión disponibles en el sistema.
     *
     * <p>Los tramos representan conexiones entre paradas, ya sea mediante transporte
     * público o caminando, e incluyen el tiempo de recorrido en segundos.</p>
     *
     * @return mapa con los {@link Tramo} indexados por su identificador único
     * @throws Exception si ocurre un error al acceder a la fuente de datos
     */
    Map<String, Tramo> buscarTodos() throws Exception;
}

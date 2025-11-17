package colectivo.dao;

import java.util.Map;

import colectivo.modelo.Parada;

/**
 * Interfaz DAO (Data Access Object) para acceso a datos de paradas de transporte.
 *
 * <p>Define el contrato para las operaciones de acceso a datos relacionadas con
 * {@link Parada}. Las implementaciones concretas pueden obtener los datos desde
 * diferentes fuentes: archivos de texto, bases de datos, servicios web, etc.</p>
 *
 * <p>Patrón DAO que separa la lógica de acceso a datos de la lógica de negocio,
 * permitiendo cambiar la fuente de datos sin afectar al resto de la aplicación.</p>
 *
 * @see Parada
 */
public interface ParadaDAO {
	
	/**
	 * Busca y retorna todas las paradas de transporte disponibles en el sistema.
	 *
	 * <p>Las paradas incluyen su código, dirección, coordenadas geográficas
	 * y las líneas que pasan por ellas.</p>
	 *
	 * @return mapa con las {@link Parada} indexadas por su código identificador numérico
	 * @throws Exception si ocurre un error al acceder a la fuente de datos
	 */
	Map<Integer, Parada> buscarTodos() throws Exception;
}

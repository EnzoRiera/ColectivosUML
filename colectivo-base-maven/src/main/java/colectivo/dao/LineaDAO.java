package colectivo.dao;

import java.util.Map;

import colectivo.modelo.Linea;

/**
 * Interfaz DAO (Data Access Object) para acceso a datos de líneas de transporte.
 *
 * <p>Define el contrato para las operaciones de acceso a datos relacionadas con
 * {@link Linea}. Las implementaciones concretas pueden obtener los datos desde
 * diferentes fuentes: archivos de texto, bases de datos, servicios web, etc.</p>
 *
 * <p>Patrón DAO que separa la lógica de acceso a datos de la lógica de negocio,
 * permitiendo cambiar la fuente de datos sin afectar al resto de la aplicación.</p>
 *
 * @see Linea
 */
public interface LineaDAO {
	
    /**
     * Busca y retorna todas las líneas de transporte disponibles en el sistema.
     *
     * <p>Las líneas incluyen su código, nombre, lista de paradas en orden
     * y frecuencias de salida por día de la semana.</p>
     *
     * @return mapa con las {@link Linea} indexadas por su código identificador
     * @throws Exception si ocurre un error al acceder a la fuente de datos
     */
    Map<String, Linea> buscarTodos() throws Exception;
}

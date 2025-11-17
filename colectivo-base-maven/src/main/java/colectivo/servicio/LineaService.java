package colectivo.servicio;

import java.util.Map;

import colectivo.modelo.Linea;

/**
 * Interfaz que define el contrato para el servicio de gestión de líneas de transporte.
 * Proporciona operaciones para acceder a la información de las líneas disponibles en el sistema.
 */
public interface LineaService {

	/**
	 * Busca y retorna todas las líneas de transporte disponibles en el sistema.
	 *
	 * @return mapa con las líneas indexadas por su código identificador
	 * @throws Exception si ocurre un error al acceder a los datos de las líneas
	 */
	Map<String, Linea> buscarTodos() throws Exception;

}

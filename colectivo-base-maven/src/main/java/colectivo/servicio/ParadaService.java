package colectivo.servicio;

import java.util.Map;

import colectivo.modelo.Parada;

/**
 * Interfaz que define el contrato para el servicio de gestión de paradas de transporte.
 * Proporciona operaciones para acceder a la información de las paradas disponibles en el sistema.
 */
public interface ParadaService {

    /**
     * Busca y retorna todas las paradas de transporte disponibles en el sistema.
     *
     * @return mapa con las paradas indexadas por su código identificador numérico
     * @throws Exception si ocurre un error al acceder a los datos de las paradas
     */
    Map<Integer, Parada> buscarTodos() throws Exception;
}

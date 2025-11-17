package colectivo.servicio;

import java.util.Map;

import colectivo.modelo.Tramo;

/**
 * Interfaz que define el contrato para el servicio de gestión de tramos de transporte.
 * Proporciona operaciones para acceder a la información de los tramos que conectan paradas.
 */
public interface TramoService {

    /**
     * Busca y retorna todos los tramos de conexión disponibles en el sistema.
     *
     * @return mapa con los tramos indexados por su identificador único
     * @throws Exception si ocurre un error al acceder a los datos de los tramos
     */
    Map<String, Tramo> buscarTodos() throws Exception;

}

package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.CalculosAuxiliares;

/**
 * Clase encargada de calcular recorridos óptimos entre paradas.
 * Aplica diferentes estrategias de búsqueda en orden de preferencia:
 * primero busca recorridos directos, luego con transbordo y finalmente
 * considera opciones caminando. Retorna la primera solución encontrada.
 */
public class Calculo {

	private static final Logger logger = LogManager.getLogger(Calculo.class);

	/**
	 * Calcula el recorrido óptimo entre dos paradas aplicando estrategias de búsqueda.
	 * Intenta primero con búsqueda directa, luego con transbordo y finalmente considera
	 * tramos caminando. Retorna el primer conjunto de soluciones encontrado.
	 *
	 * @param paradaOrigen la parada de inicio del recorrido
	 * @param paradaDestino la parada de destino del recorrido
	 * @param diaSemana el día de la semana (1=lunes, 7=domingo)
	 * @param horaLlegaParada la hora de llegada deseada a la parada de origen
	 * @param tramos mapa de todos los tramos disponibles por identificador
	 * @return lista de listas de recorridos posibles, vacía si no hay solución
	 */
	public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		logger.info("Iniciando cálculo de recorrido desde {} hasta {} para el día {} llegando a las {}",
				paradaOrigen.getDireccion(), paradaDestino.getDireccion(), diaSemana, horaLlegaParada);

        try {

			Map<Parada, List<Tramo>> conexionesParadas = CalculosAuxiliares.conexionesParadas(tramos);

			List<EstrategiaBusqueda> estrategias = List.of(new BusquedaDirecta(), new BusquedaConTransbordo(),
					new BusquedaCaminando());

			for (EstrategiaBusqueda estrategia : estrategias) {
				List<List<Recorrido>> soluciones = estrategia.buscar(paradaOrigen, paradaDestino, diaSemana,
						horaLlegaParada, conexionesParadas, tramos);

				if (!soluciones.isEmpty())
					return soluciones;
			}
		} catch (Exception e) {
			logger.error("Error inesperado durante el cálculo de recorridos", e);
		}

		return new ArrayList<>();
	}

}
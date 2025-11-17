package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.CalculosAuxiliares;

/**
 * Estrategia de búsqueda que encuentra recorridos con un transbordo entre dos líneas.
 * Busca combinaciones donde el usuario toma una primera línea desde el origen hasta una
 * parada de transbordo, y luego cambia a otra línea para llegar al destino final.
 * La parada de transbordo debe ser común a ambas líneas y estar en la ruta correcta.
 */
public class BusquedaConTransbordo implements EstrategiaBusqueda {

	private static final Logger logger = LogManager.getLogger(BusquedaConTransbordo.class);

	public BusquedaConTransbordo() {
	}

	/**
	 * Busca recorridos que requieren un transbordo entre dos líneas diferentes.
	 * Itera sobre las líneas del origen y destino, busca paradas comunes de transbordo
	 * y construye soluciones de dos tramos que conectan origen-transbordo-destino.
	 *
	 * @param paradaOrigen la parada de origen del recorrido
	 * @param paradaDestino la parada de destino del recorrido
	 * @param diaSemana el día de la semana (1=lunes, 7=domingo)
	 * @param hora la hora de salida deseada desde el origen
	 * @param conexionesParadas mapa de paradas con sus tramos de conexión
	 * @param todosLosTramos mapa de todos los tramos disponibles por identificador
	 * @return lista de listas de recorridos, cada lista contiene dos recorridos (tramo1 y tramo2)
	 * @throws IllegalArgumentException si algún parámetro obligatorio es null o diaSemana está fuera de rango
	 */
	@Override
	public List<List<Recorrido>> buscar(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime hora,
			Map<Parada, List<Tramo>> conexionesParadas, Map<String, Tramo> todosLosTramos) {

		if (paradaOrigen == null || paradaDestino == null || hora == null) {
			logger.error("paradaOrigen, paradaDestino y hora no pueden ser null");
			throw new IllegalArgumentException("paradaOrigen, paradaDestino y hora no pueden ser null");
		}
		if (conexionesParadas == null || todosLosTramos == null) {
			logger.error("conexionesParadas y todosLosTramos no pueden ser null");
			throw new IllegalArgumentException("conexionesParadas y todosLosTramos no pueden ser null");
		}
		if (diaSemana < 1 || diaSemana > 7) {
            logger.error("diaSemana fuera de rango [1..7]: {}", diaSemana);
			throw new IllegalArgumentException("diaSemana debe estar entre 1 y 7");
		}

		List<List<Recorrido>> soluciones = new ArrayList<>();

		try {
			for (Linea lineaOrigen : paradaOrigen.getLineas()) {
				List<Parada> paradasOrigen = lineaOrigen.getParadas();
				int indiceOrigen = paradasOrigen.indexOf(paradaOrigen);

				for (Linea lineaDestino : paradaDestino.getLineas()) {
					// Se busca una parada común que no sea el destino final
					Parada interseccion = buscarParadaTransbordo(
							paradasOrigen.subList(indiceOrigen + 1, paradasOrigen.size()), lineaDestino.getParadas(),
							paradaDestino);

					if (interseccion != null) {
						List<Parada> paradasDestino = lineaDestino.getParadas();
						int indiceInterseccionOrigen = paradasOrigen.indexOf(interseccion);
						int indiceInterseccionDestino = paradasDestino.indexOf(interseccion);
						int indiceParadaDestino = paradasDestino.indexOf(paradaDestino);

						// Se verifica que el flujo del viaje sea correcto en la segunda línea
						if (indiceParadaDestino > indiceInterseccionDestino) {
							Recorrido tramo1 = CalculosAuxiliares.crearRecorridoColectivo(lineaOrigen, paradasOrigen,
									indiceOrigen, indiceInterseccionOrigen, diaSemana, hora, conexionesParadas);
							if (tramo1 != null) {
								// Para el segundo tramo, la hora de llegada es la hora de salida del tramo1 +
								// duración del tramo1
								LocalTime horaLlegadaTramo2 = tramo1.getHoraSalida().plusSeconds(tramo1.getDuracion());

								Recorrido tramo2 = CalculosAuxiliares.crearRecorridoColectivo(lineaDestino,
										paradasDestino, indiceInterseccionDestino, indiceParadaDestino, diaSemana,
										horaLlegadaTramo2, conexionesParadas);

								if (tramo2 != null)
									soluciones.add(new ArrayList<>(Arrays.asList(tramo1, tramo2)));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error inesperado durante la búsqueda con transbordo", e);
		}

		return soluciones;
	}

	/**
	 * Busca una parada común entre las paradas posteriores al origen y las paradas de la línea destino.
	 * La parada de transbordo debe estar presente en ambas listas pero no puede ser la parada final del destino.
	 * Este método permite encontrar el punto de conexión óptimo para realizar el cambio de línea.
	 *
	 * @param paradasPosterioresOrigen lista de paradas posteriores al origen en la primera línea
	 * @param paradasLineaDestino lista de todas las paradas de la línea de destino
	 * @param paradaDestinoFinal la parada final del destino que debe excluirse como punto de transbordo
	 * @return la primera parada común encontrada o null si no existe intersección válida
	 * @throws IllegalArgumentException si algún parámetro es null
	 */
	private Parada buscarParadaTransbordo(List<Parada> paradasPosterioresOrigen, List<Parada> paradasLineaDestino,
			Parada paradaDestinoFinal) {
        if (paradasPosterioresOrigen == null || paradasLineaDestino == null || paradaDestinoFinal == null) {
            logger.error("paradasPosterioresOrigen, paradasLineaDestino y paradaDestinoFinal no pueden ser null");
            throw new IllegalArgumentException("Parámetros de búsqueda de transbordo inválidos");
        }

		Set<Parada> paradasDestinoSet = new HashSet<>(paradasLineaDestino);
		paradasDestinoSet.remove(paradaDestinoFinal);

		for (Parada parada : paradasPosterioresOrigen)
			if (paradasDestinoSet.contains(parada))
				return parada;

		return null;
	}
}
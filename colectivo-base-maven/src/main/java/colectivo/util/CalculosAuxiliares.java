package colectivo.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.logica.Recorrido;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Clase de utilidad que encapsula métodos de cálculo auxiliares compartidos
 * por todas las estrategias de búsqueda de recorridos.
 * Proporciona funcionalidad para crear recorridos, calcular duraciones,
 * asignar horas de salida y obtener el próximo horario disponible de una línea.
 *
 * @see Recorrido
 * @see Linea
 * @see Parada
 * @see Tramo
 */
public class CalculosAuxiliares {

	private static final Logger logger = LogManager.getLogger(CalculosAuxiliares.class);

	/**
	 * Crea un recorrido de colectivo entre dos índices de paradas en una línea.
	 * Calcula la duración del trayecto, asigna la hora de salida más próxima disponible
	 * y válida todos los parámetros de entrada.
	 *
	 * @param linea la {@link Linea} de transporte a utilizar
	 * @param paradas lista completa de paradas de la línea
	 * @param indiceInicio índice de la parada de inicio en la lista
	 * @param indiceFin índice de la parada de fin en la lista (inclusive)
	 * @param diaSemana día de la semana (1=lunes, 7=domingo)
	 * @param horaLlegadaParada hora de llegada del usuario a la parada de inicio
	 * @param conexionesParadas mapa de conexiones entre paradas con sus tramos
	 * @return el {@link Recorrido} creado con hora de salida asignada, o null si no es posible
	 */
	public static Recorrido crearRecorridoColectivo(Linea linea, List<Parada> paradas, int indiceInicio, int indiceFin,
			int diaSemana, LocalTime horaLlegadaParada, Map<Parada, List<Tramo>> conexionesParadas) {
		// Validación temprana de parámetros
		if (linea == null) {
			logger.error("linea no puede ser null");
			return null;
		}
		if (paradas == null) {
			logger.error("paradas no puede ser null");
			return null;
		}
		if (indiceInicio < 0 || indiceFin >= paradas.size() || indiceInicio > indiceFin){
			logger.error("Índices inválidos: indiceInicio={}, indiceFin={}, tamaño paradas={}", indiceInicio, indiceFin,
					paradas.size());
			return null;
		}
		if (conexionesParadas == null) {
			logger.error("conexionesParadas no puede ser null");
			return null;
		}

		try {
			List<Parada> tramoParadas = paradas.subList(indiceInicio, indiceFin + 1);
			int duracion = calcularDuracion(tramoParadas, conexionesParadas);

			Recorrido recorrido = new Recorrido(linea, tramoParadas, null, duracion);
			List<Recorrido> listaRecorrido = new ArrayList<>();
			listaRecorrido.add(recorrido);
			asignarHorasSalida(listaRecorrido, diaSemana, horaLlegadaParada, conexionesParadas);

			if (recorrido.getHoraSalida() == null) {
				logger.warn("No se pudo asignar hora de salida para el recorrido de línea {} en día {}",
						linea.getNombre(), diaSemana);
				return null;
			}

			logger.debug("Recorrido creado exitosamente para línea {} con {} paradas", linea.getNombre(),
					tramoParadas.size());
			return recorrido;

		} catch (IndexOutOfBoundsException e) {
			logger.error("Error al crear sublista de paradas: índices fuera de rango", e);
			return null;
		} catch (IllegalArgumentException e) {
			logger.error("Argumentos inválidos al crear recorrido: {}", e.getMessage(), e);
			return null;
		} catch (Exception e) {
			logger.error("Error inesperado al crear recorrido colectivo para línea {}", linea.getNombre(), e);
			return null;
		}
	}

	/**
	 * Asigna horas de salida a una secuencia de recorridos considerando tiempos de conexión.
	 * Para cada recorrido calcula cuándo debe salir el colectivo de cabecera para llegar
	 * a tiempo a la parada del usuario, y actualiza la hora actual para el siguiente recorrido.
	 *
	 * @param recorridos lista de {@link Recorrido} a los que asignar horas de salida
	 * @param diaSemana día de la semana (1=lunes, 7=domingo)
	 * @param horaLlegadaParada hora inicial de llegada del usuario a la primera parada
	 * @param conexionesParadas mapa de conexiones entre paradas
	 * @throws IllegalArgumentException si algún parámetro es null o diaSemana está fuera de rango
	 */
	public static void asignarHorasSalida(List<Recorrido> recorridos, int diaSemana, LocalTime horaLlegadaParada,
			Map<Parada, List<Tramo>> conexionesParadas) {
		if (recorridos == null) {
			logger.error("recorridos no puede ser null");
			throw new IllegalArgumentException("recorridos no puede ser null");
		}
		if (horaLlegadaParada == null) {
			logger.error("horaLlegadaParada no puede ser null");
			throw new IllegalArgumentException("horaLlegadaParada no puede ser null");
		}
		if (conexionesParadas == null) {
			logger.error("conexionesParadas no puede ser null");
			throw new IllegalArgumentException("conexionesParadas no puede ser null");
		}
		if (diaSemana < 1 || diaSemana > 7) {
			logger.error("diaSemana fuera de rango [1..7]: " + diaSemana);
			throw new IllegalArgumentException("diaSemana debe estar entre 1 y 7");
		}

		try {
			LocalTime horaActual = horaLlegadaParada;
			for (Recorrido recorrido : recorridos) {
				if (recorrido.getLinea() != null) {
					Linea linea = recorrido.getLinea();
					Parada paradaInicio = recorrido.getParadas().getFirst();
					List<Parada> paradasLinea = linea.getParadas();
					int indiceInicio = paradasLinea.indexOf(paradaInicio);
					int duracionHastaParada = (indiceInicio > 0)
							? calcularDuracion(paradasLinea.subList(0, indiceInicio + 1), conexionesParadas)
							: 0;

					LocalTime horaSalida = obtenerProximaHoraSalida(linea, diaSemana, horaActual, duracionHastaParada);
					recorrido.setHoraSalida(horaSalida);
					if (horaSalida != null) {
						horaActual = horaSalida.plusSeconds(recorrido.getDuracion());
					}
				} else {
					recorrido.setHoraSalida(horaActual);
					horaActual = recorrido.getHoraSalida().plusSeconds(recorrido.getDuracion());
				}
			}
		} catch (Exception e) {
			logger.error("Error inesperado al asignar horas de salida", e);
			throw e;
		}

	}

	/**
	 * Calcula la duración total en segundos de un camino entre paradas.
	 * Suma los tiempos de todos los tramos consecutivos que conectan las paradas del camino.
	 *
	 * @param camino lista ordenada de {@link Parada} que forman el camino
	 * @param conexionesDeParadas mapa de conexiones entre paradas con sus tramos
	 * @return duración total en segundos del camino
	 * @throws IllegalArgumentException si algún parámetro es null
	 */
	private static int calcularDuracion(List<Parada> camino, Map<Parada, List<Tramo>> conexionesDeParadas) {
        if (camino == null) {
            logger.error("camino no puede ser null");
            throw new IllegalArgumentException("camino no puede ser null");
        }
        if (conexionesDeParadas == null) {
            logger.error("conexionesDeParadas no puede ser null");
            throw new IllegalArgumentException("conexionesDeParadas no puede ser null");
        }

        try {
		int duracion = 0;
		for (int i = 0; i < camino.size() - 1; i++) {
			Parada origen = camino.get(i);
			Parada destino = camino.get(i + 1);
			List<Tramo> tramosDesdeOrigen = conexionesDeParadas.get(origen);
            boolean tramoEncontrado = false;
            if (tramosDesdeOrigen != null) {
                for (Tramo tramo : tramosDesdeOrigen) {
                    if (!tramoEncontrado && tramo.getFin().equals(destino)) {
                        duracion += tramo.getTiempo();
                        tramoEncontrado = true;
                    }
				}
			}
		}
        logger.debug("Duración calculada para camino de {} paradas: {} segundos", camino.size(), duracion);
        return duracion;
        } catch (Exception e) {
            logger.error("Error inesperado al calcular la duración del camino", e);
            return 0;
        }
	}

	/**
	 * Obtiene la próxima hora de salida disponible de una línea que permite llegar a tiempo.
	 * Primero busca en el día actual, y si no encuentra opciones, busca en el día siguiente.
	 * Considera la duración desde la cabecera hasta la parada del usuario para calcular
	 * la hora mínima de salida requerida.
	 *
	 * @param linea la {@link Linea} de la cual obtener horarios
	 * @param diaSemana día de la semana actual (1=lunes, 7=domingo)
	 * @param horaLlegadaUsuario hora en que el usuario llega a la parada
	 * @param duracionHastaParada tiempo en segundos desde la cabecera hasta la parada del usuario
	 * @return hora de llegada a la parada del usuario, o null si no hay horarios disponibles
	 * @throws IllegalArgumentException si algún parámetro es null o diaSemana está fuera de rango
	 */
	private static LocalTime obtenerProximaHoraSalida(Linea linea, int diaSemana, LocalTime horaLlegadaUsuario,
			int duracionHastaParada) {
        if (linea == null) {
            logger.error("linea no puede ser null");
            throw new IllegalArgumentException("linea no puede ser null");
        }
        if (horaLlegadaUsuario == null) {
            logger.error("horaLlegadaUsuario no puede ser null");
            throw new IllegalArgumentException("horaLlegadaUsuario no puede ser null");
        }
        if (diaSemana < 1 || diaSemana > 7) {
            logger.error("diaSemana fuera de rango [1..7]: " + diaSemana);
            throw new IllegalArgumentException("diaSemana debe estar entre 1 y 7");
        }

        try {

		List<LocalTime> horasHoy = linea.getHorasFrecuencia(diaSemana);
        if (horasHoy == null || horasHoy.isEmpty()) {
            logger.debug("No hay horas de frecuencia para línea {} en día {}", linea.getNombre(), diaSemana);  // ✅ DEBUG en lugar de ERROR
        }

		LocalTime horaSalidaMinima = horaLlegadaUsuario.minusSeconds(duracionHastaParada);
        logger.debug("Buscando próxima salida para línea {} el día {} - Hora mínima requerida: {}", 
                linea.getNombre(), diaSemana, horaSalidaMinima);  // ✅ LOG DE CONTEXTO

		LocalTime mejorHoraSalidaHoy = null;

		// Encuentra el bus más temprano de HOY que salga DESPUÉS de horaSalidaMinima
		if (horasHoy != null) {
			for (LocalTime horaCabecera : horasHoy) {
				// Si la hora de salida es DESPUÉS o IGUAL a la mínima requerida
				if (!horaCabecera.isBefore(horaSalidaMinima)) {

					// Si es el primero que encontramos, o si es más temprano que el que ya teníamos
					if (mejorHoraSalidaHoy == null || horaCabecera.isBefore(mejorHoraSalidaHoy)) {
						mejorHoraSalidaHoy = horaCabecera;
					}
				}
			}
		}

		if (mejorHoraSalidaHoy != null) {
            LocalTime horaLlegada = mejorHoraSalidaHoy.plusSeconds(duracionHastaParada);
            logger.debug("Salida encontrada hoy: {} (llegada a parada: {})", mejorHoraSalidaHoy, horaLlegada);  // ✅ LOG DE ÉXITO
            // Devuelve la hora de llegada a la parada del usuario
            return horaLlegada;
		}

		int diaSiguiente = (diaSemana % 7) + 1;

		List<LocalTime> horasManana = linea.getHorasFrecuencia(diaSiguiente);

		LocalTime mejorHoraSalidaManana = null;

		if (horasManana != null) {
			for (LocalTime horaCabecera : horasManana) {
				if (mejorHoraSalidaManana == null || horaCabecera.isBefore(mejorHoraSalidaManana)) {
					mejorHoraSalidaManana = horaCabecera;
				}
			}
		}

		if (mejorHoraSalidaManana != null) {
            LocalTime horaLlegada = mejorHoraSalidaManana.plusSeconds(duracionHastaParada);
            logger.debug("Salida encontrada mañana (día {}): {} (llegada a parada: {})", 
                    diaSiguiente, mejorHoraSalidaManana, horaLlegada); 
            // Devuelve la hora de llegada a la parada (ej: 06:10 de mañana)
            return horaLlegada;
        }

        logger.warn("No hay salidas disponibles para línea {} en día {} ni día siguiente",
                linea.getNombre(), diaSemana);
        return null;
        
    } catch (Exception e) {
        logger.error("Error inesperado al obtener la próxima hora de salida para línea {}", 
                linea.getNombre(), e); 
        return null;
    }
	}

	/**
	 * Construye un mapa de conexiones de paradas a partir de un mapa de tramos.
	 * Organiza los tramos por su parada de inicio, facilitando la búsqueda de
	 * todas las conexiones salientes desde cada parada.
	 *
	 * @param tramos mapa de {@link Tramo} indexados por identificador
	 * @return mapa donde cada {@link Parada} tiene asociada su lista de tramos salientes
	 * @throws IllegalArgumentException si el parámetro tramos es null
	 */
	public static Map<Parada, List<Tramo>> conexionesParadas(Map<String, Tramo> tramos) {
        if (tramos == null) {
            logger.error("tramos no puede ser null");
            throw new IllegalArgumentException("tramos no puede ser null");
        }

        try {
            Map<Parada, List<Tramo>> conexionesDeParadas = new HashMap<>();
            for (Tramo tramo : tramos.values()) {
                Parada origen = tramo.getInicio();
                conexionesDeParadas.computeIfAbsent(origen, k -> new ArrayList<>()).add(tramo);
            }
            return conexionesDeParadas;
        } catch (Exception e) {
            logger.error("Error inesperado al construir las conexiones de paradas", e);
            return new HashMap<>();
        }
    }
}

package colectivo.interfaz;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.logica.Recorrido;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

/**
 * Clase de utilidad para formatear y presentar información de recorridos.
 * Proporciona métodos para generar resúmenes de líneas, formatear resultados
 * de búsqueda en texto legible para el usuario, y convertir recorridos a formato JSON
 * para visualización en mapas interactivos.
 * Soporta internacionalización mediante {@link ResourceBundle}.
 *
 * @see Recorrido
 * @see Parada
 * @see Linea
 */
public class Formateador {

	private static final Logger logger = LogManager.getLogger(Formateador.class);

	private static ResourceBundle resourceBundle;

	/**
	 * Establece el ResourceBundle para internacionalización de mensajes.
	 *
	 * @param bundle el {@link ResourceBundle} con las etiquetas traducidas
	 */
	public static void setResourceBundle(ResourceBundle bundle) {
		resourceBundle = bundle;
	}

	/**
	 * Genera un resumen compacto de las líneas utilizadas en cada opción de recorrido.
	 * Muestra la secuencia de líneas necesarias para cada alternativa, indicando
	 * transbordos con flechas y tramos caminando cuando corresponda.
	 *
	 * @param recorridos lista de listas de {@link Recorrido}, cada lista es una opción de viaje
	 * @return cadena formateada con el resumen numerado de todas las opciones
	 * @throws IllegalArgumentException si recorridos es null
	 */
	public static String resumenLineas(List<List<Recorrido>> recorridos) {
		if (recorridos == null) {
			logger.error("recorridos no puede ser null");
			throw new IllegalArgumentException("recorridos no puede ser null");
		}

		StringBuilder sb = new StringBuilder();
		try {
			for (int i = 0; i < recorridos.size(); i++) {
				List<Recorrido> recorrido = recorridos.get(i);
				sb.append("[").append(i + 1).append("] ");

				for (int j = 0; j < recorrido.size(); j++) {
					Linea linea = recorrido.get(j).getLinea();
					if (linea != null) {
						sb.append(linea.getCodigo());
						if (j < recorrido.size() - 1)
							sb.append(" -> ");
					} else {
						// caminando paradaBajada -> paradaSubida
						List<Parada> paradas = recorrido.get(j).getParadas();
						String walkingArrow = (resourceBundle != null)
								? resourceBundle.getString("formatter.walkingArrow")
								: " -> caminando -> ";
						sb.append(" P").append(paradas.getFirst().getCodigo()).append(walkingArrow).append(" P")
								.append(paradas.getLast().getCodigo()).append(" -> ");
					}
				}

				sb.append("\t\n");
			}
		} catch (Exception e) {
			logger.error("Error al generar el resumen de líneas", e);
		}
		return sb.toString();
	}

	/**
	 * Formatea de manera detallada las opciones de recorrido para presentación al usuario.
	 * Incluye información completa de cada tramo: líneas, paradas, horarios de salida,
	 * duraciones parciales y totales, y hora de llegada final. Utiliza etiquetas
	 * internacionalizadas del {@link ResourceBundle} configurado.
	 *
	 * @param listaRecorridos lista de listas de {@link Recorrido} con todas las opciones de viaje
	 * @param paradaOrigen {@link Parada} de inicio del viaje
	 * @param paradaDestino {@link Parada} de destino del viaje
	 * @param horaLlegaParada hora en que el usuario llega a la parada de origen
	 * @return cadena formateada con todos los detalles de las opciones de recorrido
	 * @throws IllegalArgumentException si algún parámetro es null
	 */
	public static String formatear(List<List<Recorrido>> listaRecorridos, Parada paradaOrigen, Parada paradaDestino,
			LocalTime horaLlegaParada) {

		if (listaRecorridos == null) {
			logger.error("listaRecorridos no puede ser null");
			throw new IllegalArgumentException("listaRecorridos no puede ser null");
		}
		if (paradaOrigen == null || paradaDestino == null || horaLlegaParada == null) {
			logger.error("paradaOrigen, paradaDestino y horaLlegaParada no pueden ser null");
			throw new IllegalArgumentException("paradaOrigen, paradaDestino y horaLlegaParada no pueden ser null");
		}

		StringBuilder sb = new StringBuilder();
		try {
			// Get labels from ResourceBundle with fallback to Spanish
			String lblResults = getLabel("formatter.results", "Resultados");
			String lblOriginStop = getLabel("formatter.originStop", "Parada origen:");
			String lblDestinationStop = getLabel("formatter.destinationStop", "Parada destino:");
			String lblArrivalTime = getLabel("formatter.arrivalTime", "Llega a la parada:");
			String lblNoRoutes = getLabel("formatter.noRoutesAvailable", "No hay recorridos disponibles.");
			String lblWalking = getLabel("formatter.walking", "Caminando");
			String lblLine = getLabel("formatter.line", "Linea:");
			String lblStops = getLabel("formatter.stops", "Paradas:");
			String lblDepartureTime = getLabel("formatter.departureTime", "Hora de Salida:");
			String lblDuration = getLabel("formatter.duration", "Duración:");
			String lblTotalDuration = getLabel("formatter.totalDuration", "Duración total:");
			String lblArrivalTimeEnd = getLabel("formatter.arrivalTimeEnd", "Hora de llegada:");

			sb.append("\n===== ").append(lblResults).append(" =====\n\n");

			sb.append(lblOriginStop).append(" ").append(paradaOrigen).append("\n");
			sb.append(lblDestinationStop).append(" ").append(paradaDestino).append("\n");
			sb.append(lblArrivalTime).append(" ").append(formatoHorario(horaLlegaParada)).append("\n");
			sb.append("============================\n");

			if (listaRecorridos.isEmpty()) {
				sb.append(lblNoRoutes).append("\n");
			} else {
				for (List<Recorrido> viaje : listaRecorridos) {
					LocalTime horaActual = horaLlegaParada;
					long duracionTotal = 0;

					for (Recorrido recorrido : viaje) {
						long segundosEspera = Duration.between(horaActual, recorrido.getHoraSalida()).getSeconds();
						if (segundosEspera < 0) {
							segundosEspera += 86400;
						}
						duracionTotal += segundosEspera;

						if (recorrido.getLinea() == null) {
							sb.append(lblWalking).append("\n");
						} else {
							sb.append(lblLine).append(" ").append(recorrido.getLinea().getNombre()).append("\n");
						}

						sb.append(lblStops).append(" ").append(recorrido.getParadas()).append("\n");
						sb.append(lblDepartureTime).append(" ").append(formatoHorario(recorrido.getHoraSalida()))
								.append("\n");

						LocalTime duracion = LocalTime.MIDNIGHT.plusSeconds(recorrido.getDuracion());
						sb.append(lblDuration).append(" ").append(formatoHorario(duracion)).append("\n");
						sb.append("============================\n");

						duracionTotal += recorrido.getDuracion();
						horaActual = recorrido.getHoraSalida().plusSeconds(recorrido.getDuracion());
					}

					LocalTime duracionTotalTiempo = LocalTime.MIDNIGHT.plusSeconds(duracionTotal);
					sb.append(lblTotalDuration).append(" ").append(formatoHorario(duracionTotalTiempo)).append("\n");
					sb.append(lblArrivalTimeEnd).append(" ").append(formatoHorario(horaActual)).append("\n");
					sb.append("============================\n");
					sb.append("\n----------------------------\n\n");
				}
			}
		} catch (Exception e) {
			logger.error("Error al formatear resultados", e);
		}

		return sb.toString();
	}

	/**
	 * Obtiene una etiqueta traducida del ResourceBundle configurado.
	 * Si la clave no existe o el bundle no está configurado, retorna el valor por defecto.
	 *
	 * @param key clave de la etiqueta en el ResourceBundle
	 * @param defaultValue valor por defecto si no se encuentra la clave
	 * @return la etiqueta traducida o el valor por defecto
	 */
	private static String getLabel(String key, String defaultValue) {
        try {
            if (resourceBundle != null && resourceBundle.containsKey(key)) {
                return resourceBundle.getString(key);
            }
        } catch (Exception e) {
            logger.error("Error al obtener etiqueta del ResourceBundle para la clave: {}", key, e);
        }
        return defaultValue;
    }

	/**
	 * Formatea una hora para presentación al usuario.
	 * Si la hora no tiene segundos, usa el formato HH:mm, caso contrario HH:mm:ss.
	 *
	 * @param hora la hora a formatear
	 * @return cadena con la hora formateada
	 * @throws IllegalArgumentException si hora es null
	 */
	private static String formatoHorario(LocalTime hora) {
        if (hora == null) {
            logger.error("La hora no puede ser null");
            throw new IllegalArgumentException("La hora no puede ser null");
        }
		if (hora.getSecond() == 0)
			return hora.format(DateTimeFormatter.ofPattern("HH:mm"));
		else
			return hora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	}

	/**
	 * Convierte las opciones de recorrido a formato JSON para visualización en mapas.
	 * Genera un array JSON donde cada opción contiene segmentos, y cada segmento
	 * contiene un array de coordenadas [latitud, longitud] de las paradas.
	 *
	 * @param opciones lista de listas de {@link Recorrido} con las opciones de viaje
	 * @return cadena JSON con las coordenadas de todas las rutas
	 */
	public static String recorridoJson(List<List<Recorrido>> opciones) {
		StringBuilder json = new StringBuilder("["); // Array de Opciones
		boolean primeraOpcion = true;

		for (List<Recorrido> opcion : opciones) {
			if (!primeraOpcion) {
				json.append(",");
			}
			json.append("["); // Array de Segmentos
			boolean primerSegmento = true;

			for (Recorrido segmento : opcion) {
				if (!primerSegmento) {
					json.append(",");
				}
				json.append("["); // Array de Paradas
				boolean primeraParada = true;

				for (Parada parada : segmento.getParadas()) {
					if (!primeraParada) {
						json.append(",");
					}
					// Formato: [lat, lon]
					json.append(String.format(Locale.US, "[%.6f,%.6f]", parada.getLatitud(), parada.getLongitud()));
					primeraParada = false;
				}
				json.append("]");
				primerSegmento = false;
			}
			json.append("]");
			primeraOpcion = false;
		}
		json.append("]");
		return json.toString();
	}

}

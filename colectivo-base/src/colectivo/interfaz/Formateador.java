package colectivo.interfaz;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;

public class Formateador {

    private static final Logger logger = LogManager.getLogger(Formateador.class);

    private static ResourceBundle resourceBundle;

    /**
     * Sets the ResourceBundle for internationalization.
     * This method should be called during system initialization.
     * 
     * @param bundle the ResourceBundle to use for i18n
     */
    public static void setResourceBundle(ResourceBundle bundle) {
        resourceBundle = bundle;
    }

    /**
     * Formatea los resultados de la busqueda a modo de resumen
     * mostrando solo las lineas de cada recorrido
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
                        if (j < recorrido.size() - 1) sb.append(" -> ");
                    } else {
                        // caminando paradaBajada -> paradaSubida
                        List<Parada> paradas = recorrido.get(j).getParadas();
                        String walkingArrow = (resourceBundle != null)
                                ? resourceBundle.getString("formatter.walkingArrow")
                                : " -> caminando -> ";
                        sb.append(" P").append(paradas.getFirst().getCodigo())
                          .append(walkingArrow)
                          .append(" P").append(paradas.getLast().getCodigo())
                          .append(" -> ");
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
     * Formatea los resultados de una búsqueda para ser mostrados en la GUI.
     * Replica el formato de la InterfazConsola.
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
                    int duracionTotal = 0;

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
                        sb.append(lblDepartureTime).append(" ").append(formatoHorario(recorrido.getHoraSalida())).append("\n");

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
     * Helper method to get a label from ResourceBundle with fallback.
     * 
     * @param key the resource key
     * @param defaultValue the default value if key is not found
     * @return the localized string or default value
     */
    private static String getLabel(String key, String defaultValue) {
        try {
            if (resourceBundle != null && resourceBundle.containsKey(key)) {
                return resourceBundle.getString(key);
            }
        } catch (Exception e) {
            logger.error("Error al obtener etiqueta del ResourceBundle para la clave: " + key, e);
        }
        return defaultValue;
    }

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
}

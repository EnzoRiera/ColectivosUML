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
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

/**
 * Estrategia de búsqueda que encuentra rutas realizando un único transbordo
 * entre dos líneas de colectivo.
 */
public class BusquedaConTransbordo implements EstrategiaBusqueda {

    private static final Logger logger = LogManager.getLogger(BusquedaConTransbordo.class);

    public BusquedaConTransbordo() {
    }

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
            logger.error("diaSemana fuera de rango [1..7]: " + diaSemana);
            throw new IllegalArgumentException("diaSemana debe estar entre 1 y 7");
        }

        List<List<Recorrido>> soluciones = new ArrayList<>();

        try {
            for (Linea lineaOrigen : paradaOrigen.getLineas()) {
                List<Parada> paradasOrigen = lineaOrigen.getParadas();
                int indiceOrigen = paradasOrigen.indexOf(paradaOrigen);

                for (Linea lineaDestino : paradaDestino.getLineas()) {
                    // Se busca una parada común que no sea el destino final
                    Parada interseccion = buscarParadaTransbordo(paradasOrigen.subList(indiceOrigen + 1, paradasOrigen.size()),
                            lineaDestino.getParadas(), paradaDestino);

                    if (interseccion != null) {
                        List<Parada> paradasDestino = lineaDestino.getParadas();
                        int indiceInterseccionOrigen = paradasOrigen.indexOf(interseccion);
                        int indiceInterseccionDestino = paradasDestino.indexOf(interseccion);
                        int indiceParadaDestino = paradasDestino.indexOf(paradaDestino);

                        // Se verifica que el flujo del viaje sea correcto en la segunda línea
                        if (indiceParadaDestino > indiceInterseccionDestino) {
                            Recorrido tramo1 = CalculosAuxiliares.crearRecorridoColectivo(
                                    lineaOrigen, paradasOrigen, indiceOrigen, indiceInterseccionOrigen,
                                    diaSemana, hora, conexionesParadas
                            );

                            if (tramo1 != null) {
                                // Para el segundo tramo, la hora de llegada es la salida del tramo1 + duración
                                LocalTime horaLlegadaTramo2 = tramo1.getHoraSalida().plusSeconds(tramo1.getDuracion());

                                Recorrido tramo2 = CalculosAuxiliares.crearRecorridoColectivo(
                                        lineaDestino, paradasDestino, indiceInterseccionDestino, indiceParadaDestino,
                                        diaSemana, horaLlegadaTramo2, conexionesParadas
                                );

                                if (tramo2 != null) {
                                    soluciones.add(new ArrayList<>(Arrays.asList(tramo1, tramo2)));
                                }
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
     * Busca una parada de transbordo común entre la ruta de una línea de origen
     * y la ruta completa de una línea de destino.
     */
    private Parada buscarParadaTransbordo(List<Parada> paradasPosterioresOrigen, List<Parada> paradasLineaDestino, Parada paradaDestinoFinal) {
        if (paradasPosterioresOrigen == null || paradasLineaDestino == null || paradaDestinoFinal == null) {
            logger.error("paradasPosterioresOrigen, paradasLineaDestino y paradaDestinoFinal no pueden ser null");
            throw new IllegalArgumentException("Parámetros de búsqueda de transbordo inválidos");
        }

        Set<Parada> paradasDestinoSet = new HashSet<>(paradasLineaDestino);
        paradasDestinoSet.remove(paradaDestinoFinal); // No se puede hacer transbordo en el destino final

        for (Parada parada : paradasPosterioresOrigen)
            if (paradasDestinoSet.contains(parada))
                return parada; // Devuelve la primera parada común encontrada

        return null; // No se encontraron paradas comunes
    }
}

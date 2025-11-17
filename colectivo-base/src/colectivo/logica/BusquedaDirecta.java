package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
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

public class BusquedaDirecta implements EstrategiaBusqueda {

    private static final Logger logger = LogManager.getLogger(BusquedaDirecta.class);

    public BusquedaDirecta() {
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
            Set<Linea> lineasDestino = new HashSet<>(paradaDestino.getLineas());

            for (Linea linea : paradaOrigen.getLineas()) {
                if (lineasDestino.contains(linea)) {
                    List<Parada> paradas = linea.getParadas();
                    int indiceOrigen = paradas.indexOf(paradaOrigen);
                    int indiceDestino = paradas.indexOf(paradaDestino);

                    if (indiceDestino > indiceOrigen) {
                        Recorrido recorrido = CalculosAuxiliares.crearRecorridoColectivo(
                                linea, paradas, indiceOrigen, indiceDestino, diaSemana, hora, conexionesParadas
                        );
                        if (recorrido != null) {
                            soluciones.add(new ArrayList<>(Collections.singletonList(recorrido)));
                        } else {
                            logger.error("No se pudo crear el recorrido para la línea: " + linea.getNombre());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error inesperado durante la búsqueda directa", e);
        }

        return soluciones;
    }
}

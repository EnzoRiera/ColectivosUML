package colectivo.logica;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Interfaz para definir diferentes algoritmos (estrategias) de búsqueda de recorridos.
 */
public interface EstrategiaBusqueda {

    List<List<Recorrido>> buscar(Parada origen, Parada destino, int diaSemana, LocalTime hora,
                                 Map<Parada, List<Tramo>> conexionesParadas,
                                 Map<String, Tramo> todosLosTramos);
}

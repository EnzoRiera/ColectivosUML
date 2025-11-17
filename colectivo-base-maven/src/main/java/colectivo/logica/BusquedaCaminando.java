package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Constantes;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Estrategia de búsqueda que permite encontrar recorridos incluyendo tramos caminando.
 * Busca combinaciones donde el usuario puede caminar entre dos paradas para conectar
 * diferentes líneas de transporte, expandiendo así las posibilidades de rutas.
 * Utiliza internamente una búsqueda directa para conectar origen-bajada y subida-destino.
 */
public class BusquedaCaminando implements EstrategiaBusqueda {

    private static final Logger logger = LogManager.getLogger(BusquedaCaminando.class);

    private final EstrategiaBusqueda busquedaDirecta;

    /**
     * Constructor que inicializa la estrategia con una búsqueda directa interna.
     */
    public BusquedaCaminando() {
        this.busquedaDirecta = new BusquedaDirecta();
    }

    /**
     * Busca recorridos que incluyen tramos caminando entre paradas.
     * Itera sobre todos los tramos de tipo CAMINANDO y combina rutas directas
     * (origen -> bajada) con (subida -> destino) para formar soluciones completas.
     *
     * @param paradaOrigen la parada de origen del recorrido
     * @param paradaDestino la parada de destino del recorrido
     * @param diaSemana el día de la semana (1=lunes, 7=domingo)
     * @param hora la hora de salida deseada
     * @param conexionesParadas mapa de paradas con sus tramos de conexión
     * @param todosLosTramos mapa de todos los tramos disponibles por identificador
     * @return lista de listas de recorridos, cada lista representa una solución completa
     * @throws IllegalArgumentException si algún parámetro obligatorio es null
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
        
        List<List<Recorrido>> soluciones = new ArrayList<>();
        
        try {
            for (Tramo tramoCaminando : todosLosTramos.values()) {
                if (tramoCaminando.getTipo() == Constantes.CAMINANDO) {

                    Parada paradaBajada = tramoCaminando.getInicio();
                    Parada paradaSubida = tramoCaminando.getFin();

                    // Origen -> Bajada
                    List<List<Recorrido>> rutasIniciales = busquedaDirecta.buscar(paradaOrigen, paradaBajada, diaSemana, hora, conexionesParadas, todosLosTramos);
                    if (!rutasIniciales.isEmpty()) {
                        // Subida -> Destino
                        List<List<Recorrido>> rutasFinales = busquedaDirecta.buscar(paradaSubida, paradaDestino, diaSemana, hora, conexionesParadas, todosLosTramos);
                        if (!rutasFinales.isEmpty()) {
                            for (List<Recorrido> inicio : rutasIniciales) {
                                for (List<Recorrido> fin : rutasFinales) {
                                    Recorrido ultimoInicio = inicio.getLast();
                                    LocalTime horaInicioCaminando = ultimoInicio.getHoraSalida().plusSeconds(ultimoInicio.getDuracion());
                                    Recorrido recorridoCaminando = new Recorrido(null, Arrays.asList(paradaBajada, paradaSubida), horaInicioCaminando, tramoCaminando.getTiempo());

                                    List<Recorrido> solucionCompleta = new ArrayList<>(inicio);
                                    solucionCompleta.add(recorridoCaminando);
                                    solucionCompleta.addAll(fin);
                                    soluciones.add(solucionCompleta);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error inesperado durante la búsqueda con tramo caminando", e);
        }
        return soluciones;
    }
}

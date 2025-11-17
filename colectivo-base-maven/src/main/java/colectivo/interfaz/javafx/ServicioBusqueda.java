package colectivo.interfaz.javafx;

import java.time.LocalTime;
import java.util.List;

import javafx.concurrent.Task;
import colectivo.controlador.Coordinador;
import colectivo.interfaz.Formateador;
import colectivo.logica.Recorrido;
import colectivo.modelo.Parada;

/**
 * Servicio especializado para ejecutar búsquedas de recorridos de forma asíncrona.
 *
 * <p>Encapsula la lógica de búsqueda en tareas de JavaFX ({@link Task}) que se ejecutan
 * en hilos separados, evitando bloquear la interfaz de usuario durante operaciones
 * potencialmente costosas. Los resultados se formatean automáticamente para su
 * presentación en diferentes formatos (resumen, logs detallados, JSON para mapas).</p>
 *
 * <p>El servicio delega la búsqueda real al {@link Coordinador} y utiliza
 * {@link Formateador} para generar las diferentes representaciones del resultado.</p>
 *
 * @see Task
 * @see Coordinador
 * @see Formateador
 * @see ResultadoFormateado
 */
public class ServicioBusqueda {

    /** Coordinador de la aplicación que ejecuta las búsquedas. */
    private final Coordinador coordinador;

    /**
     * Construye un servicio de búsqueda con el coordinador especificado.
     *
     * @param coordinador el {@link Coordinador} que ejecutará las búsquedas
     */
    public ServicioBusqueda(Coordinador coordinador) {
        this.coordinador = coordinador;
    }

    /**
     * Crea una tarea asíncrona para buscar recorridos entre dos paradas.
     *
     * <p>La tarea se ejecuta en un hilo separado y realiza las siguientes operaciones:</p>
     * <ol>
     *   <li>Busca recorridos usando el coordinador</li>
     *   <li>Si encuentra resultados, los formatea en tres representaciones:
     *     <ul>
     *       <li>Resumen: líneas utilizadas de forma compacta</li>
     *       <li>Logs: detalles completos del recorrido</li>
     *       <li>JSON: coordenadas para dibujar en el mapa</li>
     *     </ul>
     *   </li>
     *   <li>Retorna un {@link ResultadoFormateado} con todos los formatos</li>
     *   <li>Si no encuentra resultados, retorna null</li>
     * </ol>
     *
     * @param origen {@link Parada} de inicio del recorrido
     * @param destino {@link Parada} de destino del recorrido
     * @param numeroDia día de la semana (1=lunes, 7=domingo)
     * @param hora hora de llegada a la parada de origen
     * @return {@link Task} que producirá un {@link ResultadoFormateado} o null si no hay rutas
     */
    public Task<ResultadoFormateado> crearTareaBusqueda(Parada origen, Parada destino,
                                                        int numeroDia, LocalTime hora) {
        return new Task<>() {
            @Override
            protected ResultadoFormateado call() {
                List<List<Recorrido>> recorridos =
                        coordinador.buscarRecorridos(origen, destino, numeroDia, hora);

                if (!recorridos.isEmpty()) {
                    String resumen = Formateador.resumenLineas(recorridos);
                    String logs = Formateador.formatear(recorridos, origen, destino, hora);
                    String jsonRuta = Formateador.recorridoJson(recorridos);

                    return new ResultadoFormateado(resumen, logs, jsonRuta);
                }

                return null;
            }
        };
    }
}

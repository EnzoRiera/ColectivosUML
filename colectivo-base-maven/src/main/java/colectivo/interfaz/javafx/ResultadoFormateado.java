package colectivo.interfaz.javafx;

/**
 * Registro inmutable que encapsula un resultado de búsqueda formateado en múltiples representaciones.
 *
 * <p>Contiene tres formatos del mismo conjunto de recorridos encontrados:</p>
 * <ul>
 *   <li><strong>resumen</strong>: versión compacta mostrando las líneas utilizadas en cada opción</li>
 *   <li><strong>logs</strong>: detalles completos con horarios, paradas y duraciones</li>
 *   <li><strong>jsonRuta</strong>: coordenadas geográficas en formato JSON para visualización en mapa</li>
 * </ul>
 *
 * <p>Este record se utiliza como resultado de las tareas asíncronas de búsqueda,
 * permitiendo actualizar simultáneamente diferentes componentes de la interfaz
 * (lista de opciones, área de texto, mapa interactivo).</p>
 *
 * @param resumen representación compacta de las líneas de cada opción de recorrido
 * @param logs representación detallada con horarios, paradas y duraciones completas
 * @param jsonRuta coordenadas de las rutas en formato JSON para visualización en mapa
 *
 * @see ServicioBusqueda
 */
public record ResultadoFormateado(String resumen, String logs, String jsonRuta) {}
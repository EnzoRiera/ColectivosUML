package colectivo.interfaz.javafx;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.PauseTransition;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

/**
 * Clase de utilidad que proporciona funcionalidad de autocompletado para ComboBox de JavaFX.
 *
 * <p>Permite búsqueda incremental en ComboBox mediante escritura de texto, normalizando
 * automáticamente los acentos y la capitalización para facilitar la búsqueda. El usuario
 * puede escribir caracteres y el sistema buscará coincidencias que comiencen con el texto
 * ingresado (búsqueda por prefijo) o que lo contengan (búsqueda por contenido).</p>
 *
 * <p>Características principales:</p>
 * <ul>
 *   <li>Búsqueda insensible a mayúsculas/minúsculas y acentos</li>
 *   <li>Prioriza coincidencias por prefijo sobre coincidencias por contenido</li>
 *   <li>Buffer de búsqueda que se limpia automáticamente tras un período de inactividad</li>
 *   <li>Soporte para teclas especiales: BACKSPACE (borrar último carácter) y ESCAPE (cancelar)</li>
 *   <li>Caché de textos normalizados para mejor rendimiento</li>
 * </ul>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>
 * ComboBox&lt;Parada&gt; combo = new ComboBox&lt;&gt;();
 * Autocompletado.activarAutocompletado(combo, Parada::getDireccion);
 * </pre>
 *
 * @see ComboBox
 * @see PauseTransition
 */
public final class Autocompletado {

	private static final Logger logger = LogManager.getLogger(Autocompletado.class);

    /**
     * Duración por defecto del temporizador antes de limpiar el buffer de búsqueda.
     * Configurado en 1 segundo de inactividad.
     */
    private static final Duration DEFAULT_CLEAR_DELAY = Duration.seconds(1.0);

    /**
     * Tamaño máximo del buffer de búsqueda en caracteres.
     * Limita la cantidad de caracteres que se pueden escribir consecutivamente.
     */
    private static final int MAX_BUFFER_SIZE = 20;

    private Autocompletado() {}

    /**
     * Normaliza una cadena de texto eliminando acentos y convirtiéndola a minúsculas.
     *
     * <p>Utiliza la forma de descomposición NFD (Canonical Decomposition) para separar
     * los caracteres base de sus marcas diacríticas, y luego elimina todas las marcas
     * diacríticas mediante una expresión regular. Finalmente convierte el resultado
     * a minúsculas para búsquedas insensibles a mayúsculas.</p>
     *
     * <p>Ejemplos de normalización:</p>
     * <ul>
     *   <li>"Álvarez" → "alvarez"</li>
     *   <li>"José María" → "jose maria"</li>
     *   <li>"CIUDAD" → "ciudad"</li>
     * </ul>
     *
     * @param input cadena de texto a normalizar
     * @return cadena normalizada sin acentos y en minúsculas, o null si la entrada es null
     */
    private static String normalizar(String  input) {
        String textoNormalizado = null;
        if (input != null) {
            String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
            textoNormalizado = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            textoNormalizado = textoNormalizado.toLowerCase();
        }
        return textoNormalizado;
    }

    /**
     * Activa la funcionalidad de autocompletado en un ComboBox de JavaFX.
     *
     * <p>Configura el ComboBox para responder a eventos de teclado y realizar búsqueda
     * incremental sobre sus elementos. El usuario puede escribir caracteres y el sistema
     * buscará automáticamente elementos coincidentes, priorizando aquellos que comienzan
     * con el texto ingresado sobre aquellos que simplemente lo contienen.</p>
     *
     * <p>Funcionamiento interno:</p>
     * <ul>
     *   <li>Mantiene un buffer de caracteres escritos que se limpia tras 1 segundo de inactividad</li>
     *   <li>Cachea versiones normalizadas de los textos de los elementos para mejor rendimiento</li>
     *   <li>Filtra eventos KEY_TYPED para capturar caracteres escritos</li>
     *   <li>Filtra eventos KEY_PRESSED para manejar BACKSPACE y ESCAPE</li>
     *   <li>Selecciona automáticamente el mejor elemento coincidente y muestra el desplegable</li>
     * </ul>
     *
     * <p>Teclas especiales:</p>
     * <ul>
     *   <li>BACKSPACE: elimina el último carácter del buffer de búsqueda</li>
     *   <li>ESCAPE: limpia el buffer y cierra el desplegable</li>
     * </ul>
     *
     * @param <T> tipo de los elementos en el ComboBox
     * @param comboBox el {@link ComboBox} al que añadir autocompletado
     * @param toStringFn función que extrae el texto representativo de cada elemento
     * @throws IllegalArgumentException si comboBox o toStringFn son null
     */
    public static <T> void activarAutocompletado(ComboBox<T> comboBox, Function<T, String> toStringFn) {
		if (comboBox == null) {
			logger.error("comboBox no puede ser null");
			throw new IllegalArgumentException("comboBox no puede ser null");
		}
		if (toStringFn == null) {
			logger.error("toStringFn no puede ser null");
			throw new IllegalArgumentException("toStringFn no puede ser null");
		}

        comboBox.setEditable(false);

        final Map<T, String> cacheNormalizado = new HashMap<>();
        final StringBuilder buffer = new StringBuilder();
        final PauseTransition clearBuffer = new PauseTransition(DEFAULT_CLEAR_DELAY);
        clearBuffer.setOnFinished(e -> buffer.setLength(0));

		comboBox.addEventFilter(KeyEvent.KEY_TYPED, ev -> {
			try {
				final String ch = ev.getCharacter();

				if (ch != null && !ch.isEmpty()) {
					final char c = ch.charAt(0);

                    // solo maneja chars
					if (!Character.isISOControl(c)) {
						if (buffer.length() < MAX_BUFFER_SIZE) {
							buffer.append(c);

							final String q = normalizar(buffer.toString());

							T bestMatch = null;
							T potentialMatch = null;

							Iterator<T> iterator = comboBox.getItems().iterator();

							while (iterator.hasNext() && bestMatch == null) {
								T item = iterator.next();

								// Obtenemos el texto normalizado del caché
								String textoNormalizado = cacheNormalizado.computeIfAbsent(item, k -> normalizar(toStringFn.apply(k)));

								if (textoNormalizado != null) {
									if (textoNormalizado.startsWith(q)) {
										bestMatch = item;
									}
									// Solo lo guardamos si aún no tenemos un 'potentialMatch'
									else if (potentialMatch == null && textoNormalizado.contains(q)) {
										potentialMatch = item;
									}
								}
							}

							T itemToSelect = null;

							if (bestMatch != null) {
								itemToSelect = bestMatch;
							} else if (potentialMatch != null) {
								itemToSelect = potentialMatch;
							}

							if (itemToSelect != null) {
								comboBox.getSelectionModel().select(itemToSelect);
								if (!comboBox.isShowing())
									comboBox.show();
							}
						}

						clearBuffer.playFromStart();
						ev.consume();
					}
				}
			} catch (Exception ex) {
				logger.error("Error al procesar evento de tecla (KEY_TYPED) en autocompletado", ex);
			}
		});

        comboBox.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
			try {
				if (ev.getCode() == KeyCode.BACK_SPACE && !buffer.isEmpty()) {
					buffer.setLength(Math.max(0, buffer.length() - 1));
					ev.consume();
				} else if (ev.getCode() == KeyCode.ESCAPE) {
					buffer.setLength(0);
					comboBox.hide();
					ev.consume();
				}
			} catch (Exception ex) {
				logger.error("Error al procesar evento de tecla (KEY_PRESSED) en autocompletado", ex);
			}
		});
    }
}
package colectivo.interfaz.javafx;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import javafx.animation.PauseTransition;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public final class Autocompletado {

	private static final Logger logger = LogManager.getLogger(Autocompletado.class);

	private static final Duration DEFAULT_CLEAR_DELAY = Duration.seconds(1.0);
	private static final int MAX_BUFFER_SIZE = 20;

	private Autocompletado() {
	}

	private static String normalizar(String input) {
		String textoNormalizado = null;
		if (input != null) {
			String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
			textoNormalizado = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			textoNormalizado = textoNormalizado.toLowerCase();
		}
		return textoNormalizado;
	}

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

					if (!Character.isISOControl(c)) {
						if (buffer.length() < MAX_BUFFER_SIZE) {
							buffer.append(c);

							final String q = normalizar(buffer.toString());

							// --- LÓGICA DE BÚSQUEDA PRIORIZADA ---

							// 'bestMatch' es un item que COMIENZA con q (Prioridad 1)
							T bestMatch = null;

							// 'potentialMatch' es el *primer* item que CONTIENE q (Prioridad 2)
							T potentialMatch = null;

							Iterator<T> iterator = comboBox.getItems().iterator();

							while (iterator.hasNext() && bestMatch == null) {
								T item = iterator.next();

								// Obtenemos el texto normalizado del caché
								String textoNormalizado = cacheNormalizado.computeIfAbsent(item,
										k -> normalizar(toStringFn.apply(k)));

								if (textoNormalizado != null) {
									// 1. Chequeo de Prioridad 1: ¿Comienza con q?
									if (textoNormalizado.startsWith(q)) {
										bestMatch = item; // Encontrado. El bucle se detendrá.
									}
									// 2. Chequeo de Prioridad 2: ¿Contiene q?
									// Solo lo guardamos si aún no tenemos un 'potentialMatch'
									else if (potentialMatch == null && textoNormalizado.contains(q)) {
										potentialMatch = item; // Guardamos, pero seguimos buscando.
									}
								}
							}

							T itemToSelect = null;
							if (bestMatch != null) {
								itemToSelect = bestMatch; // Prioridad 1
							} else if (potentialMatch != null) {
								itemToSelect = potentialMatch; // Prioridad 2
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
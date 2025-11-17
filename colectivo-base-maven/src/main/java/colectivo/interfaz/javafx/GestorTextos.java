package colectivo.interfaz.javafx;

import java.util.ResourceBundle;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.modelo.Parada;

/**
 * Gestor especializado para la actualización de textos de la interfaz según el idioma.
 *
 * <p>Se encarga de aplicar las traducciones del {@link ResourceBundle} a todos los
 * componentes visuales de la interfaz gráfica. Actualiza etiquetas, botones, textos
 * de ayuda (prompts) y el título de la ventana principal.</p>
 *
 * <p>Maneja errores de forma robusta, registrando advertencias cuando faltan claves
 * de traducción pero continuando con la actualización de los demás componentes.</p>
 *
 * <p>Componentes que actualiza:</p>
 * <ul>
 *   <li>Etiquetas (Labels): títulos y descripciones de campos</li>
 *   <li>Botones: textos de acciones</li>
 *   <li>Prompts: textos de ayuda en campos vacíos</li>
 *   <li>Ventana: título de la aplicación</li>
 * </ul>
 *
 * @see ResourceBundle
 * @see Label
 * @see Button
 * @see ComboBox
 */
public class GestorTextos {
	private static final Logger logger = LogManager.getLogger(GestorTextos.class);

	/**
	 * Actualiza todos los textos de la interfaz según el ResourceBundle proporcionado.
	 *
	 * <p>Este método coordina la actualización de todos los componentes de la interfaz
	 * llamando a métodos especializados para cada tipo de componente. Maneja excepciones
	 * de forma robusta, registrando errores pero permitiendo que la aplicación continúe.</p>
	 *
	 * @param bundle el {@link ResourceBundle} con las traducciones a aplicar
	 * @param scene la {@link Scene} de la aplicación para actualizar el título de ventana
	 * @param mainTitle etiqueta del título principal
	 * @param secondaryTitle etiqueta del título secundario
	 * @param labelOrigen etiqueta del campo de origen
	 * @param labelDestino etiqueta del campo de destino
	 * @param labelDia etiqueta del campo de día
	 * @param labelLlegada etiqueta del campo de hora de llegada
	 * @param labelOpciones etiqueta del título de opciones
	 * @param botonVista botón para cambiar vista
	 * @param botonIdioma botón para cambiar idioma
	 * @param botonBuscar botón de búsqueda
	 * @param comboOrigen ComboBox de selección de parada de origen
	 * @param comboDestino ComboBox de selección de parada de destino
	 * @param comboDia ComboBox de selección de día
	 * @param resultsArea área de texto para mostrar resultados
	 */
	public void actualizar(ResourceBundle bundle, Scene scene, Label mainTitle, Label secondaryTitle, Label labelOrigen,
			Label labelDestino, Label labelDia, Label labelLlegada, Label labelOpciones, Button botonVista,
			Button botonIdioma, Button botonBuscar, ComboBox<Parada> comboOrigen, ComboBox<Parada> comboDestino,
			ComboBox<String> comboDia, TextArea resultsArea) {

		if (bundle != null) {
            try {
                actualizarLabels(bundle, mainTitle, secondaryTitle, labelOrigen, labelDestino, labelDia, labelLlegada,
                        labelOpciones);

                actualizarBotones(bundle, botonVista, botonIdioma, botonBuscar);

                actualizarPrompts(bundle, comboOrigen, comboDestino, comboDia, resultsArea);

                actualizarVentana(bundle, scene);

            } catch (java.util.MissingResourceException e) {
                logger.error("Falta clave de idioma: {}", e.getKey(), e);
            } catch (Exception e) {
                logger.error("Error inesperado al actualizar textos de la interfaz", e);
            }

		} else {
            logger.warn("ResourceBundle es null, no se actualizarán los textos");
        }
	}

	/**
	 * Actualiza los textos de todas las etiquetas (Labels) de la interfaz.
	 *
	 * <p>Recibe un número variable de etiquetas y las actualiza en orden usando
	 * un array de claves predefinido. Si una clave no existe en el ResourceBundle,
	 * registra una advertencia pero continúa con las demás etiquetas.</p>
	 *
	 * @param bundle el {@link ResourceBundle} con las traducciones
	 * @param labels array variable de {@link Label} a actualizar
	 */
	private void actualizarLabels(ResourceBundle bundle, Label... labels) {
		String[] claves = { "title.main", "title.secondary", "label.origin", "label.destination", "label.day",
				"label.arrival", "options.title" };

		for (int i = 0; i < labels.length && i < claves.length; i++) {
			if (labels[i] != null) {
				try {
					labels[i].setText(bundle.getString(claves[i]));
				} catch (java.util.MissingResourceException e) {
					logger.warn("Clave no encontrada para label: {}", claves[i]);
				}
			}
		}
	}

	/**
	 * Actualiza los textos de todos los botones de la interfaz.
	 *
	 * <p>Recibe un número variable de botones y los actualiza en orden usando
	 * un array de claves predefinido. Si una clave no existe en el ResourceBundle,
	 * registra una advertencia pero continúa con los demás botones.</p>
	 *
	 * @param bundle el {@link ResourceBundle} con las traducciones
	 * @param botones array variable de {@link Button} a actualizar
	 */
	private void actualizarBotones(ResourceBundle bundle, Button... botones) {
		String[] claves = { "button.view", "button.language", "button.search" };

		for (int i = 0; i < botones.length && i < claves.length; i++) {
			if (botones[i] != null) {
				try {
					botones[i].setText(bundle.getString(claves[i]));
				} catch (java.util.MissingResourceException e) {
					logger.warn("Clave no encontrada para botón: {}", claves[i]);
				}
			}
		}
	}

	/**
	 * Actualiza los textos de ayuda (prompts) de los controles de entrada.
	 *
	 * <p>Establece los textos de ayuda que se muestran cuando los campos están vacíos,
	 * guiando al usuario sobre qué información debe ingresar en cada control.</p>
	 *
	 * @param bundle el {@link ResourceBundle} con las traducciones
	 * @param comboOrigen {@link ComboBox} de selección de parada de origen
	 * @param comboDestino {@link ComboBox} de selección de parada de destino
	 * @param comboDia {@link ComboBox} de selección de día de la semana
	 * @param resultsArea {@link TextArea} para mostrar resultados de búsqueda
	 */
	private void actualizarPrompts(ResourceBundle bundle, ComboBox<Parada> comboOrigen, ComboBox<Parada> comboDestino,
			ComboBox<String> comboDia, TextArea resultsArea) {

        comboOrigen.setPromptText(bundle.getString("prompt.selectOrigin"));
        comboDestino.setPromptText(bundle.getString("prompt.selectDestination"));
		comboDia.setPromptText(bundle.getString("prompt.selectDay"));
		resultsArea.setPromptText(bundle.getString("results.prompt"));
	}

	/**
	 * Actualiza el título de la ventana principal de la aplicación.
	 *
	 * <p>Obtiene el {@link Stage} desde la escena y actualiza su título con
	 * la traducción correspondiente. Si no se encuentra la clave en el ResourceBundle,
	 * registra una advertencia pero no interrumpe la ejecución.</p>
	 *
	 * @param bundle el {@link ResourceBundle} con las traducciones
	 * @param scene la {@link Scene} desde la cual obtener la ventana principal
	 */
	private void actualizarVentana(ResourceBundle bundle, Scene scene) {
		if (scene != null) {
			Stage stage = (Stage) scene.getWindow();
			if (stage != null) {
				try {
					stage.setTitle(bundle.getString("title.window"));
				} catch (java.util.MissingResourceException e) {
					logger.warn("Clave no encontrada: title.window");
				}
			}
		}
	}
}
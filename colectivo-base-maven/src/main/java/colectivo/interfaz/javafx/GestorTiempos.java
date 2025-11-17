package colectivo.interfaz.javafx;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Gestor especializado para la configuración de selectores de tiempo (horas y minutos).
 *
 * <p>Se encarga de inicializar y configurar los ComboBox de horas y minutos con formato
 * numérico de dos dígitos (00-23 para horas, 00-59 para minutos). Aplica formato
 * personalizado mediante celdas especializadas y activa autocompletado para facilitar
 * la selección rápida de valores.</p>
 *
 * <p>Características:</p>
 * <ul>
 *   <li>Formato de dos dígitos con ceros a la izquierda (ej: 05, 09, 23)</li>
 *   <li>Valores por defecto establecidos en 0</li>
 *   <li>Autocompletado habilitado para búsqueda incremental</li>
 *   <li>Manejo robusto de errores con logging</li>
 * </ul>
 *
 * @see ComboBox
 * @see Autocompletado
 */
public class GestorTiempos {
	private static final Logger logger = LogManager.getLogger(GestorTiempos.class);

	/**
	 * Inicializa el ComboBox de horas con valores de 0 a 23.
	 *
	 * <p>Carga las 24 horas del día (0-23), configura el formato de dos dígitos,
	 * establece el valor por defecto en 0 y activa el autocompletado para
	 * búsqueda incremental.</p>
	 *
	 * @param comboHora el {@link ComboBox} de horas a inicializar
	 * @throws RuntimeException si ocurre un error durante la inicialización
	 */
	public void inicializarHoras(ComboBox<Integer> comboHora) {
		try {

			for (int i = 0; i < 24; i++)
				comboHora.getItems().add(i);

			configurarFormatoNumerico(comboHora);
			comboHora.setValue(0);
			Autocompletado.activarAutocompletado(comboHora, String::valueOf);
			logger.debug("ComboBox de horas inicializado correctamente");
		} catch (Exception e) {
			logger.error("Error al inicializar comboBox de horas", e);
			throw e;
		}
	}

	/**
	 * Inicializa el ComboBox de minutos con valores de 0 a 59.
	 *
	 * <p>Limpia el ComboBox, carga los 60 minutos (0-59), configura el formato
	 * de dos dígitos, establece el valor por defecto en 0 y activa el autocompletado
	 * para búsqueda incremental.</p>
	 *
	 * @param comboMinuto el {@link ComboBox} de minutos a inicializar
	 * @throws RuntimeException si ocurre un error durante la inicialización
	 */
	public void inicializarMinutos(ComboBox<Integer> comboMinuto) {
		try {
			comboMinuto.getItems().clear();
			for (int i = 0; i < 60; i++) {
				comboMinuto.getItems().add(i);
			}
			configurarFormatoNumerico(comboMinuto);
			comboMinuto.setValue(0);
			Autocompletado.activarAutocompletado(comboMinuto, String::valueOf);
		} catch (Exception e) {
			logger.error("Error al inicializar comboBox de minutos", e);
			throw e;
		}
	}

	/**
	 * Configura el formato numérico de dos dígitos en un ComboBox de enteros.
	 *
	 * <p>Establece una celda personalizada ({@link NumeroListCell}) tanto para
	 * la lista desplegable como para el botón del ComboBox. Esto asegura que
	 * los números se muestren con ceros a la izquierda (ej: 05, 09, 23).</p>
	 *
	 * @param comboBox el {@link ComboBox} a configurar
	 * @throws RuntimeException si ocurre un error durante la configuración
	 */
	private void configurarFormatoNumerico(ComboBox<Integer> comboBox) {
        try {
            comboBox.setCellFactory(lv -> new NumeroListCell());
            comboBox.setButtonCell(new NumeroListCell());
        } catch (Exception e) {
            logger.error("Error al configurar formato numérico del ComboBox", e);
            throw e;
        }
    }

	/**
	 * Celda personalizada para mostrar números enteros con formato de dos dígitos.
	 *
	 * <p>Formatea los números con ceros a la izquierda usando el formato "%02d".
	 * Por ejemplo, el número 5 se muestra como "05" y el 23 como "23".</p>
	 *
	 * <p>Si la celda está vacía o el valor es null, no muestra ningún texto.</p>
	 */
	private static class NumeroListCell extends ListCell<Integer> {

		/**
		 * Actualiza el contenido de la celda con el número formateado.
		 *
		 * <p>Aplica el formato de dos dígitos al número, agregando un cero
		 * a la izquierda si es necesario (0-9 se convierten en 00-09).</p>
		 *
		 * @param item el número entero a mostrar
		 * @param empty si la celda está vacía
		 */
		@Override
		protected void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null)
				setText(null);
			else
				setText(String.format("%02d", item));
		}
	}
}

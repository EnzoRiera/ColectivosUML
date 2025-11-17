package colectivo.interfaz.javafx;

import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.control.ComboBox;

/**
 * Gestor especializado para la configuración del selector de días de la semana.
 *
 * <p>Se encarga de inicializar y configurar el ComboBox de días con los nombres
 * traducidos según el idioma actual. Los días se formatean con su número (1-7)
 * seguido del nombre traducido (ej: "1 - Lunes"). También activa la funcionalidad
 * de autocompletado para facilitar la selección.</p>
 *
 * @see Autocompletado
 * @see ResourceBundle
 */
public class GestorDias {

	/**
	 * Inicializa el ComboBox de días de la semana con los nombres traducidos.
	 *
	 * <p>Obtiene los nombres de los días desde el ResourceBundle, los formatea
	 * con su número correspondiente (1=lunes, 7=domingo) y los carga en el ComboBox.
	 * Finalmente activa el autocompletado para búsqueda incremental.</p>
	 *
	 * @param comboDia el {@link ComboBox} de días a inicializar
	 * @param bundle el {@link ResourceBundle} con las traducciones de los días
	 */
	public void inicializar(ComboBox<String> comboDia, ResourceBundle bundle) {
		List<String> dias = obtenerDias(bundle);

		comboDia.getItems().clear();
		for (int i = 0; i < dias.size(); i++) {
			comboDia.getItems().add((i + 1) + " - " + dias.get(i));
		}

		Autocompletado.activarAutocompletado(comboDia, s -> s);
	}

	/**
	 * Obtiene la lista de nombres de días de la semana traducidos.
	 *
	 * <p>Extrae las cadenas de los días desde el ResourceBundle en el orden
	 * estándar: lunes, martes, miércoles, jueves, viernes, sábado, domingo.</p>
	 *
	 * @param bundle el {@link ResourceBundle} con las claves de traducción
	 * @return lista inmutable con los 7 nombres de días traducidos
	 */
	private List<String> obtenerDias(ResourceBundle bundle) {
		return List.of(
                bundle.getString("day.monday"),
                bundle.getString("day.tuesday"),
                bundle.getString("day.wednesday"),
                bundle.getString("day.thursday"),
                bundle.getString( "day.friday"),
                bundle.getString("day.saturday"),
                bundle.getString("day.sunday"));
	}
}
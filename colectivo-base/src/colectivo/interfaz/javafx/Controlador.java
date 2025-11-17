package colectivo.interfaz.javafx;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Coordinador;
import colectivo.interfaz.Formateador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;

public class Controlador {

	private static final Logger logger = LogManager.getLogger(Controlador.class);

	private Coordinador coordinador;

	private ResourceBundle resourceBundle;

	@FXML
	private ComboBox<Integer> comboHora;

	@FXML
	private ComboBox<Integer> comboMinuto;

	@FXML
	private ComboBox<Parada> comboOrigen;

	@FXML
	private ComboBox<Parada> comboDestino;

	@FXML
	private ComboBox<String> comboDia;

	@FXML
	private Label optionsSummaryLabel;

	@FXML
	private TextArea resultsArea;

	@FXML
	private Label queryCompleted;

	public void setCoordinador(Coordinador coordinador) {
		this.coordinador = coordinador;
	}

	/**
	 * Gets the ResourceBundle from the coordinator's configuration.
	 *
	 * @return the ResourceBundle for internationalization
	 */
	private ResourceBundle getResourceBundle() {
		if (resourceBundle == null && coordinador != null) {
			resourceBundle = coordinador.getConfiguracion().getResourceBundle();
		}
		return resourceBundle;
	}

	public void initData() {
		if (coordinador != null) {
			inicializarParadas();
			inicializarDias();
			inicializarHoras();
			inicializarMinutos();
		}
	}

	private void inicializarParadas() {
		Map<Integer, Parada> nombresParadas = coordinador.mapearParadas();
		comboOrigen.getItems().addAll(nombresParadas.values());
		comboDestino.getItems().addAll(nombresParadas.values());

		configurarComboBoxParada(comboOrigen);
		configurarComboBoxParada(comboDestino);

		Function<Parada, String> paradaToString = p -> p == null ? "" : p.getCodigo() + " - " + p.getDireccion();

		Autocompletado.activarAutocompletado(comboOrigen, paradaToString);
		Autocompletado.activarAutocompletado(comboDestino, paradaToString);
	}

	/**
	 * Configura un ComboBox de Parada para que muestre solo el codigo y el nombre
	 * de la parada, pero siga almacenando el objeto Parada completo.
	 *
	 * @param comboBox El ComboBox a configurar.
	 */
	private void configurarComboBoxParada(ComboBox<Parada> comboBox) {

		// Esta fábrica controla cómo se ven los items en la lista desplegable
		comboBox.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(Parada parada, boolean empty) {
				super.updateItem(parada, empty);

				if (empty || parada == null)
					setText(null);
				else
					setText(parada.getCodigo() + " - " + parada.getDireccion());
			}
		});

		// Esta fábrica controla cómo se ve el item SELECCIONADO en el botón principal
		comboBox.setButtonCell(new ListCell<>() {
			@Override
			protected void updateItem(Parada parada, boolean empty) {
				super.updateItem(parada, empty);

				if (empty || parada == null)
					setText(null);
				else
					setText(parada.getCodigo() + " - " + parada.getDireccion());
			}
		});
	}

	private void inicializarDias() {
		ResourceBundle bundle = getResourceBundle();

		List<String> dias = List.of((bundle != null) ? bundle.getString("day.monday") : "Lunes",
				(bundle != null) ? bundle.getString("day.tuesday") : "Martes",
				(bundle != null) ? bundle.getString("day.wednesday") : "Miércoles",
				(bundle != null) ? bundle.getString("day.thursday") : "Jueves",
				(bundle != null) ? bundle.getString("day.friday") : "Viernes",
				(bundle != null) ? bundle.getString("day.saturday") : "Sábado",
				(bundle != null) ? bundle.getString("day.sunday") : "Domingo");

		comboDia.getItems().clear();
		for (int i = 0; i < dias.size(); i++)
			comboDia.getItems().add((i + 1) + " - " + dias.get(i));

		Autocompletado.activarAutocompletado(comboDia, s -> s);
	}

	private void inicializarHoras() {
		comboHora.getItems().clear();

		for (int i = 0; i < 24; i++)
			comboHora.getItems().add(i);

		configurarFormatoNumerico(comboHora);
		comboHora.setValue(0);

		Autocompletado.activarAutocompletado(comboHora, String::valueOf);
	}

	private void inicializarMinutos() {
		comboMinuto.getItems().clear();

		for (int i = 0; i < 60; i++)
			comboMinuto.getItems().add(i);

		configurarFormatoNumerico(comboMinuto);
		comboMinuto.setValue(0);

		Autocompletado.activarAutocompletado(comboMinuto, String::valueOf);
	}

	/**
	 * Configura un ComboBox de Integer para que muestre los números con dos dígitos
	 * (ej. "01", "05", "10"), pero siga almacenando el objeto Integer.
	 *
	 * @param comboBox El ComboBox a configurar.
	 */
	private void configurarFormatoNumerico(ComboBox<Integer> comboBox) {

		// Esta fábrica controla cómo se ven los items en la lista desplegable
		comboBox.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(String.format("%02d", item));
				}
			}
		});

		// Esta fábrica controla cómo se ve el item SELECCIONADO en el botón principal
		comboBox.setButtonCell(new ListCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(String.format("%02d", item));
				}
			}
		});
	}

	@FXML
	public void onSearchButtonClick() {

		if (resultsArea != null) {
			resultsArea.clear();
		}
		ResourceBundle bundle = getResourceBundle();

		try {
			Parada origen = comboOrigen.getValue();
			Parada destino = comboDestino.getValue();
			String dia = comboDia.getValue().split(" ")[0];
			int numeroDia = Integer.parseInt(dia);

			int hora = comboHora.getValue();
			int minuto = comboMinuto.getValue();
			LocalTime time = LocalTime.of(hora, minuto);

			List<List<Recorrido>> recorridos = coordinador.buscarRecorridos(origen, destino, numeroDia, time);

			if (recorridos.isEmpty()) {
				String noRoutesMsg = (bundle != null) ? bundle.getString("search.noRoutesFound")
						: "No hay opciones de recorrido";
				String queryFailedMsg = (bundle != null) ? bundle.getString("search.queryCompletedFailed")
						: "Consulta completada sin éxito";

				optionsSummaryLabel.setText(noRoutesMsg);
				queryCompleted.setText(queryFailedMsg);
				queryCompleted.getStyleClass().add("label-warning");
			} else {
				String resumen = Formateador.resumenLineas(recorridos);
				optionsSummaryLabel.setText(resumen);

				String logs = Formateador.formatear(recorridos, origen, destino, time);
				resultsArea.setText(logs);

				String queryCompletedMsg = (bundle != null) ? bundle.getString("search.queryCompleted")
						: "Consulta completada";
				queryCompleted.setText(queryCompletedMsg);
				queryCompleted.getStyleClass().add("label-success");
			}
		} catch (NullPointerException e) {
			logger.error("Campos obligatorios no seleccionados o coordinador no disponible", e);
			String fillFieldsMsg = (bundle != null) ? bundle.getString("search.fillAllFields")
					: "Es necesario llenar todos los campos para poder hacer una búsqueda";
			String queryFailedMsg = (bundle != null) ? bundle.getString("search.queryFailed") : "Fallo la consulta";

			optionsSummaryLabel.setText(fillFieldsMsg);
			queryCompleted.setText(queryFailedMsg);
			queryCompleted.getStyleClass().add("label-error");
		} catch (Exception e) {
			logger.error("Error inesperado al realizar la búsqueda", e);
			String queryFailedMsg = (bundle != null) ? bundle.getString("search.queryFailed") : "Fallo la consulta";
			optionsSummaryLabel.setText(queryFailedMsg);
			queryCompleted.setText(queryFailedMsg);
			queryCompleted.getStyleClass().add("label-error");
		}
	}
}

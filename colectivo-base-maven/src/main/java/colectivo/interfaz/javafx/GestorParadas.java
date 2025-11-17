package colectivo.interfaz.javafx;

import java.util.Map;
import java.util.function.Function;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import colectivo.modelo.Parada;

/**
 * Gestor especializado para la configuración de los selectores de paradas.
 *
 * <p>Se encarga de inicializar y configurar los ComboBox de origen y destino
 * con las paradas disponibles. Aplica formato personalizado para mostrar
 * las paradas como "código - dirección" y activa la funcionalidad de
 * autocompletado para facilitar la búsqueda y selección.</p>
 *
 * <p>Utiliza una celda personalizada ({@link ParadaListCell}) para renderizar
 * las paradas tanto en la lista desplegable como en el botón del ComboBox.</p>
 *
 * @see Parada
 * @see Autocompletado
 * @see ComboBox
 */
public class GestorParadas {

    /**
     * Inicializa los ComboBox de origen y destino con las paradas disponibles.
     *
     * <p>Realiza las siguientes operaciones:</p>
     * <ul>
     *   <li>Carga todas las paradas en ambos ComboBox</li>
     *   <li>Configura celdas personalizadas para mostrar "código - dirección"</li>
     *   <li>Activa el autocompletado con búsqueda por código o dirección</li>
     * </ul>
     *
     * @param comboOrigen el {@link ComboBox} de paradas de origen
     * @param comboDestino el {@link ComboBox} de paradas de destino
     * @param nombresParadas mapa de paradas disponibles indexadas por código
     */
    public void inicializar(ComboBox<Parada> comboOrigen, ComboBox<Parada> comboDestino,
                            Map<Integer, Parada> nombresParadas) {
        comboOrigen.getItems().addAll(nombresParadas.values());
        comboDestino.getItems().addAll(nombresParadas.values());

        configurarComboBox(comboOrigen);
        configurarComboBox(comboDestino);

        Function<Parada, String> paradaToString = p -> p == null ? "" :
                p.getCodigo() + " - " + p.getDireccion();

        Autocompletado.activarAutocompletado(comboOrigen, paradaToString);
        Autocompletado.activarAutocompletado(comboDestino, paradaToString);
    }

    /**
     * Configura un ComboBox con celdas personalizadas para mostrar paradas.
     *
     * <p>Establece la fábrica de celdas para la lista desplegable y la celda
     * del botón, ambas usando {@link ParadaListCell}.</p>
     *
     * @param comboBox el {@link ComboBox} a configurar
     */
    private void configurarComboBox(ComboBox<Parada> comboBox) {
        comboBox.setCellFactory(lv -> new ParadaListCell());
        comboBox.setButtonCell(new ParadaListCell());
    }

    /**
     * Celda personalizada para mostrar paradas en formato "código - dirección".
     *
     * <p>Renderiza cada parada mostrando su código numérico seguido de un guión
     * y la dirección completa. Si la parada es null o la celda está vacía,
     * no muestra ningún texto.</p>
     */
    private static class ParadaListCell extends ListCell<Parada> {

        /**
         * Actualiza el contenido de la celda según la parada a mostrar.
         *
         * <p>Formatea la parada como "código - dirección" si existe,
         * o deja la celda vacía en caso contrario.</p>
         *
         * @param parada la {@link Parada} a mostrar
         * @param empty si la celda está vacía
         */
        @Override
        protected void updateItem(Parada parada, boolean empty) {
            super.updateItem(parada, empty);
            if (empty || parada == null) setText(null);
            else setText(parada.getCodigo() + " - " + parada.getDireccion());
        }
    }
}
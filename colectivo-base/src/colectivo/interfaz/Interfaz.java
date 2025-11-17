package colectivo.interfaz;

import colectivo.aplicacion.Coordinador;
import colectivo.modelo.Recorrido;

/**
 * Abstraction for the application's user interface.
 *
 * <p>
 * Implementations of this interface provide different user interaction modes
 * (for example, console-based or graphical UIs). The concrete implementation
 * is selected at startup through a Factory used by
 * {@code InicializadorSistema.iniciar()}, enabling runtime switching between
 * available interfaces for development, testing or production.
 * </p>
 *
 * <p>
 * Implementations should be responsible for obtaining user input (origin/
 * destination stops, day of week, arrival time) and presenting results
 * (lists of {@link Recorrido} sequences). Methods are synchronous and may
 * block waiting for user input depending on the UI modality.
 * </p>
 */
public interface Interfaz {
    /**
     * Attach the application coordinator used to perform business operations.
     *
     * @param coordinador the {@link Coordinador} instance to be used by the UI
     */
    void setCoordinador(Coordinador coordinador);

    void iniciarInterfaz();

}
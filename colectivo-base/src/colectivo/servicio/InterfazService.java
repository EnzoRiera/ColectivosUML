package colectivo.servicio;

import colectivo.aplicacion.Coordinador;

public interface InterfazService {
    /**
     * Attach the application coordinator used to perform business operations.
     *
     * @param coordinador the {@link Coordinador} instance to be used by the UI
     */
    void setCoordinador(Coordinador coordinador);

    void iniciarInterfaz();

}

package colectivo.servicio;

import colectivo.controlador.Coordinador;

/**
 * Interfaz que define el contrato para los servicios de interfaz de usuario.
 * Las implementaciones de esta interfaz son responsables de iniciar y gestionar
 * la interacción con el usuario, ya sea a través de una interfaz gráfica o consola.
 */
public interface InterfazService {

    /**
     * Establece el coordinador de la aplicación que gestionará la lógica de negocio.
     *
     * @param coordinador el coordinador que coordina los servicios y la lógica
     */
    void setCoordinador(Coordinador coordinador);

    /**
     * Inicia la interfaz de usuario y la deja lista para interactuar.
     * Este método debe ser llamado después de configurar el coordinador.
     */
    void iniciarInterfaz();

}

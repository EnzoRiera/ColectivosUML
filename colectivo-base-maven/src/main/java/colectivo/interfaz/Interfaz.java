package colectivo.interfaz;

import colectivo.controlador.Coordinador;

/**
 * Interfaz que define el contrato para las implementaciones de interfaz de usuario.
 * Permite abstraer la forma en que el usuario interactúa con la aplicación,
 * ya sea mediante interfaz gráfica (JavaFX, Swing) o interfaz de consola.
 *
 * @see Coordinador
 */
public interface Interfaz {

    /**
     * Establece el coordinador de la aplicación que gestionará la lógica de negocio.
     *
     * @param coordinador el {@link Coordinador} que coordina los servicios y la lógica
     */
    void setCoordinador(Coordinador coordinador);

    /**
     * Inicia la interfaz de usuario y la deja lista para interactuar con el usuario.
     * Este método debe ser llamado después de configurar el coordinador.
     */
    void iniciarInterfaz();

}
package colectivo.aplicacion;

import colectivo.interfaz.Interfaz;
import colectivo.logica.Calculo;

/**
 * Main application entry point for the bus collective simulation system.
 * 
 * <p>
 * This class serves as the bootstrap for the application, performing the
 * following:
 * <ul>
 * <li>Initializes the system configuration and resources</li>
 * <li>Instantiates and wires all business logic components</li>
 * <li>Launches the JavaFX user interface</li>
 * </ul>
 * 
 * <p>
 * <b>Execution Flow:</b>
 * <ol>
 * <li>Configuration loading (resources, locale, view files)</li>
 * <li>Component instantiation ({@link Ciudad}, {@link Calculo},
 * {@link Interfaz})</li>
 * <li>Dependency injection and relationship configuration</li>
 * <li>UI launch and event loop start</li>
 * </ol>
 * 
 * <p>
 * <b>Architecture:</b> This class contains the complete system initialization
 * logic, including component creation, dependency injection, and configuration
 * setup. The {@link Coordinador} acts as the central mediator between all major
 * components.
 * 
 * @see Coordinador
 * @see colectivo.interfaz.javafx.InterfazJavaFX
 * @author Miyen
 * @author Enzo
 * @author Agustin
 * @version 1.0
 * @since 1.0
 */
public class AplicacionConsultas {
	private static Coordinador coordinador;

	public static void main(String[] args) throws Exception {
		// El flujo de la app lo maneja el coordinador
		coordinador = new Coordinador();
		coordinador.inicializarAplicacion();
		coordinador.iniciarConsulta();
	}

}

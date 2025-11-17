package colectivo.servicio;

import colectivo.controlador.Coordinador;
import colectivo.interfaz.Interfaz;
import colectivo.util.Factory;

/**
 * Implementación del servicio de interfaz de usuario.
 * Actúa como intermediario entre la capa de aplicación y la interfaz específica,
 * delegando las operaciones a la instancia de {@link Interfaz} obtenida mediante
 * {@link Factory}. La implementación concreta de la interfaz se determina dinámicamente
 * según la configuración del sistema.
 *
 * @see InterfazService
 * @see Interfaz
 * @see Factory
 */
public class InterfazServiceImpl implements InterfazService {

	private final Interfaz interfaz;

	/**
	 * Constructor que inicializa la implementación obteniendo la instancia
	 * de interfaz configurada a través de la fábrica.
	 */
	public InterfazServiceImpl() {
		interfaz = Factory.getInstancia("INTERFAZ", Interfaz.class);
	}

	/**
	 * Establece el coordinador de la aplicación en la interfaz.
	 *
	 * @param coordinador el {@link Coordinador} que gestiona la lógica de la aplicación
	 */
	@Override
	public void setCoordinador(Coordinador coordinador) {
		interfaz.setCoordinador(coordinador);
	}

	/**
	 * Inicia la interfaz de usuario delegando la operación a la implementación concreta.
	 */
	@Override
	public void iniciarInterfaz() {
		interfaz.iniciarInterfaz();
	}

}

package colectivo.servicio;

import colectivo.aplicacion.Coordinador;
import colectivo.interfaz.Interfaz;
import colectivo.util.Factory;

public class InterfazServiceImpl implements InterfazService {

	private Interfaz interfaz;

	/**
	 * Constructs a new {@code InterfazServiceImpl}.
	 *
	 * <p>
	 * The constructor obtains a {@link Interfaz} instance from the {@link Factory}
	 * using the key \"INTERFAZ\". The factory is expected to return a fully
	 * initialized Interfaz compatible with the {@link Interfaz} interface.
	 * </p>
	 */
	public InterfazServiceImpl() {
		interfaz = Factory.getInstancia("INTERFAZ", Interfaz.class);
	}

	@Override
	public void setCoordinador(Coordinador coordinador) {
		interfaz.setCoordinador(coordinador);
	}

	@Override
	public void iniciarInterfaz() {
		interfaz.iniciarInterfaz();
	}

}

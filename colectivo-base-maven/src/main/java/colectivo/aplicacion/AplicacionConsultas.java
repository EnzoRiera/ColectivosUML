package colectivo.aplicacion;

import colectivo.controlador.Coordinador;

/**
 * Clase principal de la aplicación de consultas de recorridos de colectivo.
 * <p>
 * Esta clase contiene el método {@code main} que sirve como punto de entrada
 * de la aplicación. Su responsabilidad principal es inicializar y arrancar
 * el sistema delegando el control al {@link Coordinador}.
 * </p>
 * <p>
 * <b>Flujo de ejecución:</b>
 * <ol>
 *   <li>Crear una instancia del {@link Coordinador}</li>
 *   <li>Inicializar todos los componentes de la aplicación mediante
 *       {@link Coordinador#inicializarAplicacion()}</li>
 *   <li>Iniciar la interfaz de usuario mediante {@link Coordinador#iniciarConsulta()}</li>
 * </ol>
 * </p>
 * <p>
 * <b>Arquitectura:</b> Esta clase implementa el patrón de diseño "Application Controller",
 * donde el control de flujo y la coordinación entre componentes se delegan a un
 * coordinador central, manteniendo la clase principal simple y enfocada únicamente
 * en el arranque.
 * </p>
 *
 * @see Coordinador
 * @see Coordinador#inicializarAplicacion()
 * @see Coordinador#iniciarConsulta()
 * @author POO2025
 * @version 1.0
 */
public class AplicacionConsultas {

	/** Coordinador central que gestiona el flujo de la aplicación. */
	private static Coordinador coordinador;

	/**
	 * Método principal de la aplicación.
	 * <p>
	 * Este método es el punto de entrada de la aplicación. Crea una instancia
	 * del {@link Coordinador}, inicializa todos los componentes necesarios
	 * (configuración, ciudad, servicios, interfaz) y finalmente inicia la
	 * interfaz de usuario para consultas.
	 * </p>
	 * <p>
	 * <b>Secuencia de operaciones:</b>
	 * <ol>
	 *   <li>Crea el coordinador central de la aplicación</li>
	 *   <li>Invoca {@link Coordinador#inicializarAplicacion()} para cargar:
	 *     <ul>
	 *       <li>Configuración (idioma, estilos, vistas)</li>
	 *       <li>Modelo de la ciudad (paradas, líneas, tramos)</li>
	 *       <li>Servicios de negocio</li>
	 *       <li>Interfaz de usuario</li>
	 *     </ul>
	 *   </li>
	 *   <li>Invoca {@link Coordinador#iniciarConsulta()} para mostrar la interfaz
	 *       y permitir que el usuario realice consultas de recorridos</li>
	 * </ol>
	 * </p>
	 *
	 * @param args argumentos de línea de comandos (no utilizados actualmente)
	 * @throws Exception si ocurre un error durante la inicialización de la ciudad,
	 *         la carga de datos o el inicio de la interfaz
	 * @see Coordinador
	 */
	public static void main(String[] args) throws Exception {
		// El flujo de la app lo maneja el coordinador
		coordinador = new Coordinador();
		coordinador.inicializarAplicacion();
		coordinador.iniciarConsulta();
	}

}

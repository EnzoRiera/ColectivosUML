package colectivo.interfaz.consola;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.InputMismatchException;

import colectivo.aplicacion.Coordinador;
import colectivo.interfaz.Formateador;
import colectivo.interfaz.Interfaz;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;

/**
 * Console-based implementation of the {@code Interfaz} interface.
 *
 * <p>
 * This class provides an interactive console UI used for development and
 * debugging. It prompts the user for origin/destination stops, day of the week
 * and arrival time, and prints search results to standard output. An
 * alternative graphical interface exists for production/interactive use.
 * </p>
 *
 * <p>
 * Input is read from {@link System#in} via a static {@link Scanner}. Methods in
 * this class block waiting for user input and perform basic validation. Time
 * values use {@link java.time.LocalTime} and are formatted with
 * {@link java.time.format.DateTimeFormatter}.
 * </p>
 */
public class InterfazConsola implements Interfaz {

	private static final Logger logger = LogManager.getLogger(InterfazConsola.class);

	/**
	 * Shared scanner reading from standard input. Kept static to avoid multiple
	 * scanners on {@code System.in}.
	 */
	private static Scanner scanner = new Scanner(System.in);
	/**
	 * Coordinator for application logic; set by the application bootstrap.
	 */
	private Coordinador coordinador = null;
	private Parada destino;
	private int dia;

	private LocalTime hora;

	private Parada origen;

	/**
	 * Constructs a console interface instance.
	 *
	 * <p>
	 * Primarily used during development; prints a simple instantiation message.
	 * </p>
	 */
	public InterfazConsola() {
		// debug message
		System.out.println("InterfazConsola instanciada correctamente.");
	}

	/**
	 * Prompts the user to enter the day of the week.
	 *
	 * <p>
	 * Valid values are 1 (Monday) through 7 (Sunday/holiday). The method repeats
	 * until a valid integer in the range is provided.
	 * </p>
	 *
	 * @return integer representing the day of the week (1..7)
	 */
	private int ingresarDiaSemana() {
		while (true) {
			System.out.print("Ingrese día de la semana (1=lunes, 2=martes, ..., 7=domingo/feriado): ");
			try {
				int dia = scanner.nextInt();
				if (dia >= 1 && dia <= 7) {
					return dia;
				} else {
					System.out.println("Día inválido. Debe ser entre 1 y 7. Intente de nuevo.");
				}
			} catch (InputMismatchException ime) {
				logger.error("Entrada inválida para día de la semana.", ime);
				scanner.next(); // consumir token inválido
				System.out.println("Día inválido. Debe ser entre 1 y 7. Intente de nuevo.");
			}
		}
	}

	/**
	 * Prompts the user to enter an arrival time at a stop.
	 *
	 * <p>
	 * The expected format is "HH:mm". The method repeats until a valid
	 * {@link LocalTime} can be parsed from the input.
	 * </p>
	 *
	 * @return the parsed {@link LocalTime} representing arrival time
	 */

	private LocalTime ingresarHoraLlegaParada() {
		while (true) {
			System.out.print("Ingrese hora de llegada (HH:MM): ");
			String horaStr = scanner.next();
			try {
				return LocalTime.parse(horaStr, DateTimeFormatter.ofPattern("HH:mm"));
			} catch (Exception e) {
				logger.error("Formato de hora inválido. Use HH:MM.", e);
				System.out.println("Formato de hora inválido. Use HH:MM. Intente de nuevo.");
			}
		}
	}

	/**
	 * Prompts the user to enter a destination stop code and returns the matching
	 * {@link Parada} from the provided map.
	 *
	 * <p>
	 * Behavior mirrors {@link #ingresarParadaOrigen(Map)} and repeats until a valid
	 * stop code is entered.
	 * </p>
	 *
	 * @param paradas map of available stops keyed by integer code
	 * @return the selected destination {@link Parada}
	 */

	private Parada ingresarParadaDestino(Map<Integer, Parada> paradas) {
		while (true) {
			System.out.print("Ingrese código de parada destino: ");
			String codigo = scanner.next();
			try {
				Integer key = Integer.parseInt(codigo);
				Parada parada = paradas.get(key);
				if (parada != null) {
					return parada;
				}
			} catch (NumberFormatException nfe) {
				logger.error("Error de formato en el código de parada destino.", nfe);
			}
			System.out.println("Parada no encontrada. Intente de nuevo.");
		}
	}

	/**
	 * Prompts the user to enter an origin stop code and returns the matching
	 * {@link Parada} from the provided map.
	 *
	 * <p>
	 * This method loops until the user provides a valid integer code that exists in
	 * the {@code paradas} map. Non-integer input is reported and the prompt
	 * repeats.
	 * </p>
	 *
	 * @param paradas map of available stops keyed by integer code
	 * @return the selected origin {@link Parada}
	 */
	private Parada ingresarParadaOrigen(Map<Integer, Parada> paradas) {
		while (true) {
			System.out.print("Ingrese código de parada origen: ");
			String codigo = scanner.next();
			try {
				Integer key = Integer.parseInt(codigo);
				Parada parada = paradas.get(key);
				if (parada != null) {
					return parada;
				}
			} catch (NumberFormatException nfe) {
				logger.error("Error de formato en el código de parada origen.", nfe);
			}
			System.out.println("Parada no encontrada. Intente de nuevo.");
		}
	}

	/**
	 * Prompts the user for search parameters and displays results.
	 *
	 * <p>
	 * This method interacts with the user via standard input/output to gather
	 * origin/destination stops, day of the week, and arrival time. It then invokes
	 * the application coordinator to perform the search and prints formatted
	 * results to standard output.
	 * </p>
	 */
	@Override
	public void iniciarInterfaz() {
		try {
			this.origen = ingresarParadaOrigen(coordinador.mapearParadas());
			this.destino = ingresarParadaDestino(coordinador.mapearParadas());
			this.dia = ingresarDiaSemana();
			this.hora = ingresarHoraLlegaParada();

			List<List<Recorrido>> recorridos = coordinador.buscarRecorridos(origen, destino, dia, hora);

			String resumen = Formateador.resumenLineas(recorridos);
			System.out.println(resumen);

			String resultadosFormateados = Formateador.formatear(recorridos, origen, destino, hora);
			System.out.println(resultadosFormateados);
		} catch (Exception e) {
			logger.error("Error inesperado al ejecutar la interfaz de consola.", e);
			System.out.println("Ocurrió un error al procesar la consulta. Intente nuevamente.");
		}
	}

	/**
	 * Sets the application coordinator used by this interface.
	 *
	 * @param coordinador2 the {@link Coordinador} instance to attach
	 */
	@Override
	public void setCoordinador(Coordinador coordinador2) {
		this.coordinador = coordinador2;
	}

}
package colectivo.interfaz.consola;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.InputMismatchException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.controlador.Coordinador;
import colectivo.interfaz.Formateador;
import colectivo.interfaz.Interfaz;
import colectivo.logica.Recorrido;
import colectivo.modelo.Parada;

/**
 * Implementación de la interfaz de usuario basada en consola.
 *
 * <p>Esta clase proporciona una interfaz interactiva de consola utilizada para
 * desarrollo y depuración. Solicita al usuario paradas de origen/destino, día de
 * la semana y hora de llegada, e imprime los resultados de la búsqueda en la
 * salida estándar. Existe una interfaz gráfica alternativa para uso en producción
 * e interacción visual.</p>
 *
 * <p>La entrada se lee desde {@link System#in} mediante un {@link Scanner} estático.
 * Los métodos de esta clase se bloquean esperando la entrada del usuario y realizan
 * validación básica. Los valores de tiempo usan {@link java.time.LocalTime} y se
 * formatean con {@link java.time.format.DateTimeFormatter}.</p>
 *
 * @see Interfaz
 * @see Coordinador
 * @see Formateador
 */
public class InterfazConsola implements Interfaz {

	private static final Logger logger = LogManager.getLogger(InterfazConsola.class);

	/**
	 * Scanner compartido que lee desde la entrada estándar.
	 * Se mantiene estático para evitar múltiples scanners sobre {@code System.in}.
	 */
	private static final Scanner scanner = new Scanner(System.in);

	/**
	 * Coordinador de la lógica de aplicación, establecido por el bootstrap de la aplicación.
	 */
	private Coordinador coordinador = null;
    private Parada destino;
    private int dia;
    private LocalTime hora;
    private Parada origen;

    /**
     * Construye una instancia de interfaz de consola.
     *
     * <p>Utilizado principalmente durante el desarrollo; imprime un mensaje simple de instanciación.</p>
     */
    public InterfazConsola() {
        System.out.println("InterfazConsola instanciada correctamente.");
    }


    /**
     * Solicita al usuario que ingrese el día de la semana.
     *
     * <p>Los valores válidos son 1 (lunes) hasta 7 (domingo/feriado). El método
     * se repite hasta que se proporcione un entero válido dentro del rango.</p>
     *
     * @return entero que representa el día de la semana (1..7)
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
     * Solicita al usuario que ingrese una hora de llegada a la parada.
     *
     * <p>El formato esperado es "HH:mm". El método se repite hasta que se pueda
     * parsear un {@link LocalTime} válido desde la entrada.</p>
     *
     * @return el {@link LocalTime} parseado que representa la hora de llegada
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
     * Solicita al usuario que ingrese un código de parada de destino y retorna
     * la {@link Parada} correspondiente del mapa proporcionado.
     *
     * <p>El comportamiento es similar a {@link #ingresarParadaOrigen(Map)} y se repite
     * hasta que se ingrese un código de parada válido.</p>
     *
     * @param paradas mapa de paradas disponibles indexadas por código entero
     * @return la {@link Parada} de destino seleccionada
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
				logger.error("Error de formato en el código de parada destino", nfe);
			}
			System.out.println("Parada no encontrada. Intente de nuevo.");
		}
	}

    /**
     * Solicita al usuario que ingrese un código de parada de origen y retorna
     * la {@link Parada} correspondiente del mapa proporcionado.
     *
     * <p>Este método se repite hasta que el usuario proporcione un código entero
     * válido que exista en el mapa {@code paradas}. Las entradas no enteras se reportan
     * y el mensaje se vuelve a mostrar.</p>
     *
     * @param paradas mapa de paradas disponibles indexadas por código entero
     * @return la {@link Parada} de origen seleccionada
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
	 * Solicita al usuario los parámetros de búsqueda y muestra los resultados.
	 *
	 * <p>Este método interactúa con el usuario mediante entrada/salida estándar para
	 * recopilar paradas de origen/destino, día de la semana y hora de llegada.
	 * Luego invoca al coordinador de la aplicación para realizar la búsqueda e
	 * imprime los resultados formateados en la salida estándar.</p>
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
			logger.error("Error inesperado al ejecutar la interfaz de consola", e);
			System.out.println("Ocurrió un error al procesar la consulta. Intente nuevamente.");
		}
    }

    /**
     * Establece el coordinador de la aplicación utilizado por esta interfaz.
     *
     * @param coordinador2 la instancia de {@link Coordinador} a asociar
     */
    @Override
    public void setCoordinador(Coordinador coordinador2) {
        this.coordinador = coordinador2;
    }

}
package colectivo.util;

import java.time.LocalTime;

/**
 * Utility class with helpers for time conversions.
 *
 * <p>
 * Provides conversion from a count of seconds since midnight into a
 * {@link LocalTime} instance. The method delegates validation to
 * {@link LocalTime#of(int, int, int)} and will therefore throw a
 * {@link java.time.DateTimeException} for out-of-range values (for example,
 * negative seconds or values greater than or equal to 86400).
 * </p>
 *
 * @author POO-2025
 * @since 1.0
 */
public class Tiempo {

    /**
     * Converts a total number of seconds since midnight into a {@link LocalTime}.
     *
     * <p>
     * The input value is interpreted as seconds elapsed since 00:00:00. Valid
     * values are in the range 0..86399 (inclusive). Values outside this range
     * will cause {@link java.time.DateTimeException} to be thrown by the
     * underlying {@link LocalTime#of(int, int, int)} call.
     * </p>
     *
     * @param totalSegundos total seconds since midnight (0..86399)
     * @return the corresponding {@link LocalTime}
     * @throws java.time.DateTimeException if the computed hour, minute or second is invalid
     */
    public static LocalTime segundosATiempo(int totalSegundos) {

		// Calcular horas
		int horas = totalSegundos / 3600;
		int segundosRestantes = totalSegundos % 3600;

		// Calcular minutos
		int minutos = segundosRestantes / 60;
		int segundos = segundosRestantes % 60;

		return LocalTime.of(horas, minutos, segundos);
	}
}

package colectivo.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Clase util validacion
 * 
 */
public class Validation {

	/**
	 * convierte string en double
	 * 
	 * @param string
	 * @return double
	 */
	public static Double isDouble(String str) {
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * convierte string en integer
	 * 
	 * @param string
	 * @return integer
	 */
	public static int isInteger(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * convierte string en localDate
	 * 
	 * @param string date
	 * @return LocalDate
	 */

	public static LocalDate isDate(String date) {
		try {
			return LocalDate.parse(date);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	/**
	 * convierte formato de fechas
	 * 
	 * @param dateStr
	 * @param dateFormat
	 * @return DateFormat
	 */
	public static Date isDate(String dateStr, String dateFormat) {
		DateFormat sdf = new SimpleDateFormat(dateFormat);

		sdf.setLenient(false);
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			return null;
		}

	}
}

package colectivo.datos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;

import colectivo.aplicacion.Constantes;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * CSV data loader utility that populates in-memory model objects used by legacy
 * code and tests.
 *
 * <p>This class provides static helpers to read CSV files and construct the
 * corresponding {@link Parada}, {@link Tramo} and {@link Linea} objects.</p>
 *
 * <p>Notes and contract:
 * <ul>
 *   <li>This class is retained for backward-compatibility and for tests only.
 *       Production code should use the DAO implementations under the
 *       {@code colectivo.dao} package instead.</li>
 *   <li>Methods return non-null collections. Empty collections are returned
 *       when no valid entries are found.</li>
 *   <li>Parsing errors and data inconsistencies are reported to
 *       {@code System.err} as warnings; malformed records are skipped.</li>
 *   <li>This utility is not thread-safe and does not provide transactional
 *       guarantees. Callers should handle synchronization if required.</li>
 * </ul>
 * </p>
 *
 * deprecated Use DAO implementations in {@code colectivo.dao} for loading
 *             and accessing persisted data. This class remains available so
 *             the test-suite (one JUnit test and one manual App test) can
 *             validate legacy CSV loading behavior.
 * @author POO-2025
 * @version 1.0
 * @since 1.0
 */
public class CargarDatos {
	
	/**
	 * Loads stops (paradas) from a CSV file.
	 *
	 * <p>CSV format (semicolon-separated):</p>
	 * <pre>
	 * codigo;nombre;latitud;longitud
	 * </pre>
	 *
	 * <p>Behavior:</p>
	 * <ul>
	 *   <li>Returns a non-null {@link Map} (specifically a {@link TreeMap})
	 *       mapping stop codes ({@code Integer}) to {@link Parada} instances.</li>
	 *   <li>Lines with invalid numeric formats (code/lat/long) are skipped and
	 *       a warning is written to {@code System.err}.</li>
	 *   <li>If duplicate codes appear, the last occurrence in the file overwrites
	 *       previous entries.</li>
	 * </ul>
	 *
	 * @param nombreArchivo path to the CSV file encoded with {@link Constantes#ENCODING}
	 * @return a non-null {@link Map}{@code <Integer, Parada>} containing loaded stops;
	 *         may be empty if no valid records are found
	 * @throws IOException if an I/O error occurs opening or reading the file
	 */
	public static Map<Integer, Parada> cargarParadas(String nombreArchivo) throws IOException {
		TreeMap<Integer, Parada> paradas = new TreeMap<>();
		Parada paradaActual;

		try (Scanner read = new Scanner(new File(nombreArchivo), Constantes.ENCODING)) {
			while (read.hasNextLine()) {
				String linea = read.nextLine().trim();
				if (!linea.isEmpty()) {

					String[] partes = linea.split(Constantes.SEPARADOR);
					if (partes.length >= Constantes.CAMPOS_MINIMOS_PARADA) {
						try {
                            int codigo = Integer.parseInt(partes[0].strip());
                            String nombre = partes[1].strip();

                            // Normalizar separador decimal: reemplazar coma por punto
                            String latStr = partes[2].strip().replace(',', '.');
                            String lonStr = partes[3].strip().replace(',', '.');

                            // Ahora parsear como double
                            double latitud = Double.parseDouble(latStr);
                            double longitud = Double.parseDouble(lonStr);

                            // Crear y agregar la parada al mapa
                            paradaActual = new Parada(codigo, nombre, latitud, longitud);
                            paradas.put(codigo, paradaActual);
						} catch (NumberFormatException nfe) {
							System.err.println("Advertencia: formato numérico inválido en línea: " + linea + " - "
									+ nfe.getMessage());
						}
					}
				}
			}
		}

		return paradas;
	}


	/**
	 * Loads route segments (tramos) from a CSV file.
	 *
	 * <p>CSV format (semicolon-separated):</p>
	 * <pre>
	 * codigoInicio;codigoFin;tiempo;tipo
	 * </pre>
	 *
	 * <p>Behavior and key format:</p>
	 * <ul>
	 *   <li>Returns a non-null {@link Map}{@code <String, Tramo>} mapping tramo keys
	 *       to {@link Tramo} instances.</li>
	 *   <li>Map keys follow the format {@code "origin-destination-type"} (for example,
	 *       {@code "88-97-1"} meaning origin 88, destination 97, type 1).</li>
	 *   <li>Only tramos whose origin and destination {@link Parada} objects are
	 *       present in the provided {@code paradas} map are added. Missing paradas
	 *       produce a warning and the record is skipped.</li>
	 *   <li>For walking segments (type equals {@link Constantes#CAMINANDO}) the
	 *       loader also adds the inverse segment (destination->origin) with the
	 *       same duration and walking type.</li>
	 * </ul>
	 *
	 * @param nombreArchivo path to the CSV file encoded with {@link Constantes#ENCODING}
	 * @param paradas a non-null map of loaded {@link Parada} objects keyed by their code;
	 *                used to resolve origin and destination references
	 * @return a non-null {@link Map}{@code <String, Tramo>} containing loaded tramos;
	 *         may be empty if no valid records are found
	 * @throws FileNotFoundException if the CSV file cannot be found
	 * @throws NumberFormatException if numeric fields are malformed (caller may let this propagate
	 *                               or handle it as needed)
	 */
	public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas)
			throws FileNotFoundException {

		Map<String, Tramo> tramos = new HashMap<>();

		try (Scanner read = new Scanner(new File(nombreArchivo), Constantes.ENCODING)) {

			while (read.hasNextLine()) {

				String linea = read.nextLine().trim();

				if (!linea.isEmpty()) {
					String[] partes = linea.split(Constantes.SEPARADOR);
					if (partes.length >= Constantes.CAMPOS_MINIMOS_TRAMO) {

						// Leer datos del tramo
						int codigoInicio = Integer.parseInt(partes[0].trim());
						int codigoFin = Integer.parseInt(partes[1].trim());

						// Obtener las paradas desde el mapa
						Parada paradaInicio = paradas.get(codigoInicio);
						Parada paradaFin = paradas.get(codigoFin);

						int tiempo = Integer.parseInt(partes[2].trim());
						int tipo = Integer.parseInt(partes[3].trim());

						if (paradaInicio != null && paradaFin != null) {
							// Crear y agregar el tramo al mapa
							// Los tramos tipo 1 son unidireccionales (colectivo)
							Tramo tramo = new Tramo(paradaInicio, paradaFin, tiempo, tipo);
							String claveTramo = codigoInicio + "-" + codigoFin + "-" + tipo;
							tramos.put(claveTramo, tramo);
							if (tipo == Constantes.CAMINANDO) {
								// Agregar tramo inverso para caminando
								Tramo tramoInverso = new Tramo(paradaFin, paradaInicio, tiempo, 0);
								// Para evitar duplicacion de carga de paradas caminando, seteamos el tipo
								// despues
								tramoInverso.setTipo(Constantes.CAMINANDO);
								String claveTramoInverso = codigoFin + "-" + codigoInicio + "-" + Constantes.CAMINANDO;
								tramos.put(claveTramoInverso, tramoInverso);
							}
						}
					}
				}
			}
		}
		return tramos;
	}

	/**
	 * Loads bus lines and their frequencies from CSV files.
	 *
	 * <p>Lines CSV format (semicolon-separated):</p>
	 * <pre>
	 * codigoLinea;nombre;codigoParada1;codigoParada2;...
	 * </pre>
	 *
	 * <p>Frequencies CSV format (semicolon-separated):</p>
	 * <pre>
	 * codigoLinea;diaSemana;hora
	 * </pre>
	 *
	 * <p>Behavior:</p>
	 * <ul>
	 *   <li>First loads line definitions and resolves their stop sequence using the
	 *       provided {@code paradas} map. Lines without any valid stops are ignored
	 *       and a warning is issued.</li>
	 *   <li>After loading lines, frequencies are loaded once and attached to the
	 *       corresponding {@link Linea} objects. Frequencies referencing unknown
	 *       lines are ignored with a warning.</li>
	 * </ul>
	 *
	 * @param nombreArchivo path to the lines CSV file encoded with {@link Constantes#ENCODING}
	 * @param nombreArchivoFrecuencia path to the frequencies CSV file encoded with {@link Constantes#ENCODING}
	 * @param paradas a non-null map of loaded {@link Parada} objects keyed by their code
	 * @return a non-null {@link Map}{@code <String, Linea>} containing loaded lines keyed by line code;
	 *         may be empty if no valid records are found
	 * @throws FileNotFoundException if either CSV file cannot be found
	 */
	public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia,
			Map<Integer, Parada> paradas) throws FileNotFoundException {

		Map<String, Linea> lineas = new TreeMap<String, Linea>();

		// PASO 1: Cargar las líneas y sus paradas primero
		try (Scanner read = new Scanner(new File(nombreArchivo), Constantes.ENCODING)) {
			String codigo;
			String nombre;
			Linea lineaObj;
			int codigoParada;
			Parada parada;

			while (read.hasNextLine()) {
				String linea = read.nextLine().strip();

				if (!linea.isEmpty()) {
					String[] partes = linea.split(Constantes.SEPARADOR);

					if (partes.length >= Constantes.CAMPOS_MINIMOS_LINEA) {
						codigo = partes[0].strip();
						nombre = partes[1].strip();
						lineaObj = new Linea(codigo, nombre);
						for (int i = 2; i < partes.length; i++) {
							codigoParada = Integer.parseInt(partes[i].strip());
							parada = paradas.get(codigoParada);
							if (parada != null) {
								// Agregar la parada a la línea y viceversa (dentro de modelo Linea)
								lineaObj.agregarParada(parada);			
							} else {
								System.err.println("Advertencia: código de parada " + codigoParada
										+ " no encontrado para la línea " + codigo);
							}
						}

						if (!lineaObj.getParadas().isEmpty()) {
							lineas.put(codigo, lineaObj);
						} else {
							System.err.println("Advertencia: línea " + codigo + " sin paradas, ignorada.");
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + nombreArchivo);
			throw e;
		} catch (NoSuchElementException | IllegalStateException e) {
			System.err.println("Error reading file: " + nombreArchivo);
			throw e;
		} // fin carga de lineas

		// PASO 2: Cargar las frecuencias UNA SOLA VEZ y agregarlas a las líneas
		// correspondientes
		cargarFrecuencias(nombreArchivoFrecuencia, lineas);
		return lineas;
	}

	/**
	 * Loads frequency entries from the given CSV and attaches them to the corresponding lines.
	 *
	 * <p>CSV format (semicolon-separated):</p>
	 * <pre>
	 * codigoLinea;diaSemana;hora
	 * </pre>
	 *
	 * <p>Behavior:</p>
	 * <ul>
	 *   <li>Parses {@code hora} using {@link LocalTime#parse(CharSequence)}; malformed times
	 *       are skipped with a warning.</li>
	 *   <li>Frequencies for unknown lines are ignored with a warning.</li>
	 * </ul>
	 *
	 * @param nombreArchivoFrecuencia path to the frequencies CSV file encoded with {@link Constantes#ENCODING}
	 * @param lineasCargadas map of already loaded {@link Linea} objects keyed by line code
	 * @throws FileNotFoundException if the frequencies CSV file cannot be found
	 */
	private static void cargarFrecuencias(String nombreArchivoFrecuencia, Map<String, Linea> lineasCargadas)
			throws FileNotFoundException {

		try (Scanner freqRead = new Scanner(new File(nombreArchivoFrecuencia), Constantes.ENCODING)) {

			// Leer frecuencias
			while (freqRead.hasNextLine()) {
				String lineaFreq = freqRead.nextLine().strip();

				if (!lineaFreq.isEmpty()) {
					String[] partesFreq = lineaFreq.split(Constantes.SEPARADOR);
					if (partesFreq.length >= Constantes.CAMPOS_MINIMOS_FRECUENCIA) {
						String codigoLinea = partesFreq[0].strip();
						int diaSemana = Integer.parseInt(partesFreq[1].strip());
						String horaString = partesFreq[2].strip();

						// Buscar la línea en el mapa
						Linea lineaActual = lineasCargadas.get(codigoLinea);
						if (lineaActual != null) {
							try {
								LocalTime hora = LocalTime.parse(horaString);
								lineaActual.agregarFrecuencia(diaSemana, hora);
							} catch (DateTimeParseException dtpe) {
								System.err.println("Advertencia: formato de hora inválido '" + horaString
										+ "' para la línea " + codigoLinea + ": " + dtpe.getMessage());
							}
						} else {
							System.err.println("Advertencia: frecuencia para línea inexistente " + codigoLinea);
						}
					}
				}
			} // fin while frecuencias
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + nombreArchivoFrecuencia);
			throw e;
		} catch (NoSuchElementException | IllegalStateException e) {
			System.err.println("Error reading file: " + nombreArchivoFrecuencia);
			throw e;
		} // fin try
	}
}

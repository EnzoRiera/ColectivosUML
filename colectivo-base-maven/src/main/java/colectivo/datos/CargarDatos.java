package colectivo.datos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
 * Clase de utilidad para cargar datos del sistema de transporte desde archivos de texto.
 *
 * <p>Proporciona métodos estáticos para leer y parsear archivos de configuración que contienen:</p>
 * <ul>
 *   <li>Paradas: código, nombre y coordenadas geográficas</li>
 *   <li>Tramos: conexiones entre paradas con tiempo y tipo de transporte</li>
 *   <li>Líneas: recorridos con sus paradas asociadas</li>
 *   <li>Frecuencias: horarios de salida por día de semana</li>
 * </ul>
 *
 * <p>Los archivos se cargan desde el classpath usando {@link ClassLoader#getResourceAsStream(String)},
 * lo que permite compatibilidad con Maven y empaquetado en JAR. Los datos se parsean con formato
 * CSV usando el separador definido en {@link Constantes#SEPARADOR}.</p>
 *
 * <p>Manejo de errores robusto con validaciones y mensajes de advertencia para datos inconsistentes.</p>
 *
 * @see Parada
 * @see Tramo
 * @see Linea
 * @see Constantes
 */
public class CargarDatos {
	
	/**
	 * Carga todas las paradas desde un archivo de texto.
	 *
	 * <p>El archivo debe tener una parada por línea con el formato:
	 * código;nombre;latitud;longitud</p>
	 *
	 * <p>Características:</p>
	 * <ul>
	 *   <li>Soporta separador decimal tanto punto como coma</li>
	 *   <li>Normaliza automáticamente comas a puntos en coordenadas</li>
	 *   <li>Ignora líneas vacías</li>
	 *   <li>Registra advertencias para líneas con formato inválido</li>
	 *   <li>Retorna un TreeMap ordenado por código de parada</li>
	 * </ul>
	 *
	 * @param nombreArchivo ruta relativa del archivo en el classpath
	 * @return mapa ordenado de {@link Parada} indexadas por código
	 * @throws IOException si ocurre un error de lectura del archivo
	 * @throws FileNotFoundException si el archivo no existe en el classpath
	 */
	public static Map<Integer, Parada> cargarParadas(String nombreArchivo) throws IOException {
		TreeMap<Integer, Parada> paradas = new TreeMap<>();
		Parada paradaActual;

		// Cargar desde classpath usando ClassLoader (compatible con Maven)
		InputStream inputStream = CargarDatos.class.getClassLoader().getResourceAsStream(nombreArchivo);
		
		if (inputStream == null) {
			throw new FileNotFoundException("No se encontró el archivo '" + nombreArchivo + "' en el classpath");
		}

		try (Scanner read = new Scanner(inputStream, Constantes.ENCODING)) {
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
	 * Carga todos los tramos de conexión entre paradas desde un archivo de texto.
	 *
	 * <p>El archivo debe tener un tramo por línea con el formato:
	 * códigoInicio;códigoFin;tiempo;tipo</p>
	 *
	 * <p>Características:</p>
	 * <ul>
	 *   <li>Valida que las paradas de inicio y fin existan en el mapa proporcionado</li>
	 *   <li>Para tramos tipo {@link Constantes#CAMINANDO}, crea automáticamente el tramo inverso</li>
	 *   <li>Los tramos de colectivo (tipo 1) son unidireccionales</li>
	 *   <li>Registra advertencias para tramos con paradas inexistentes</li>
	 *   <li>Ignora líneas vacías y con formato incorrecto</li>
	 * </ul>
	 *
	 * @param nombreArchivo ruta relativa del archivo en el classpath
	 * @param paradas mapa de {@link Parada} previamente cargadas para validación
	 * @return mapa de {@link Tramo} indexados por clave "códigoInicio-códigoFin-tipo"
	 * @throws FileNotFoundException si el archivo no existe en el classpath
	 */
	public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas)
			throws FileNotFoundException {

		Map<String, Tramo> tramos = new HashMap<>();

		// Cargar desde classpath usando ClassLoader (compatible con Maven)
		InputStream inputStream = CargarDatos.class.getClassLoader().getResourceAsStream(nombreArchivo);
		
		if (inputStream == null) {
			throw new FileNotFoundException("No se encontró el archivo '" + nombreArchivo + "' en el classpath");
		}

		try (Scanner read = new Scanner(inputStream, Constantes.ENCODING)) {

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
	 * Carga todas las líneas de transporte con sus paradas y frecuencias desde archivos de texto.
	 *
	 * <p>El archivo de líneas debe tener una línea por fila con el formato:
	 * código;nombre;códigoParada1;códigoParada2;...</p>
	 *
	 * <p>Proceso de carga en dos pasos:</p>
	 * <ol>
	 *   <li>Cargar líneas y sus paradas del archivo principal</li>
	 *   <li>Cargar frecuencias del archivo secundario y asociarlas a las líneas</li>
	 * </ol>
	 *
	 * <p>Características:</p>
	 * <ul>
	 *   <li>Valida que todas las paradas referenciadas existan</li>
	 *   <li>Ignora líneas sin paradas válidas</li>
	 *   <li>Establece relación bidireccional entre líneas y paradas</li>
	 *   <li>Registra advertencias para paradas inexistentes</li>
	 *   <li>Retorna un TreeMap ordenado por código de línea</li>
	 * </ul>
	 *
	 * @param nombreArchivo ruta relativa del archivo de líneas en el classpath
	 * @param nombreArchivoFrecuencia ruta relativa del archivo de frecuencias en el classpath
	 * @param paradas mapa de {@link Parada} previamente cargadas para validación
	 * @return mapa ordenado de {@link Linea} indexadas por código
	 * @throws FileNotFoundException si alguno de los archivos no existe en el classpath
	 * @throws RuntimeException si ocurre un error al leer los archivos
	 */
	public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia,
			Map<Integer, Parada> paradas) throws FileNotFoundException {

		Map<String, Linea> lineas = new TreeMap<>();

		// PASO 1: Cargar las líneas y sus paradas primero
		// Cargar desde classpath usando ClassLoader (compatible con Maven)
		InputStream inputStream = CargarDatos.class.getClassLoader().getResourceAsStream(nombreArchivo);
		
		if (inputStream == null) {
			throw new FileNotFoundException("No se encontró el archivo '" + nombreArchivo + "' en el classpath");
		}

		try (Scanner read = new Scanner(inputStream, Constantes.ENCODING)) {
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
		} catch (NoSuchElementException | IllegalStateException e) {
			System.err.println("Error reading file: " + nombreArchivo);
			throw new RuntimeException("Error reading file: " + nombreArchivo, e);
		}

		// PASO 2: Cargar las frecuencias UNA SOLA VEZ y agregarlas a las líneas
		// correspondientes
		cargarFrecuencias(nombreArchivoFrecuencia, lineas);
		return lineas;
	}

	/**
	 * Carga las frecuencias (horarios de salida) desde un archivo y las asocia a las líneas cargadas.
	 *
	 * <p>El archivo debe tener una frecuencia por línea con el formato:
	 * códigoLínea;díaSemana;hora (en formato HH:mm:ss)</p>
	 *
	 * <p>Características:</p>
	 * <ul>
	 *   <li>Valida que la línea referenciada exista en el mapa proporcionado</li>
	 *   <li>Parsea horas usando {@link LocalTime#parse(CharSequence)}</li>
	 *   <li>Registra advertencias para líneas inexistentes o formato de hora inválido</li>
	 *   <li>Ignora líneas vacías</li>
	 *   <li>Asocia cada frecuencia a su línea correspondiente</li>
	 * </ul>
	 *
	 * @param nombreArchivoFrecuencia ruta relativa del archivo de frecuencias en el classpath
	 * @param lineasCargadas mapa de {@link Linea} previamente cargadas donde asociar las frecuencias
	 * @throws FileNotFoundException si el archivo no existe en el classpath
	 * @throws RuntimeException si ocurre un error al leer el archivo
	 */
	private static void cargarFrecuencias(String nombreArchivoFrecuencia, Map<String, Linea> lineasCargadas)
			throws FileNotFoundException {

		// Cargar desde classpath usando ClassLoader (compatible con Maven)
		InputStream inputStream = CargarDatos.class.getClassLoader().getResourceAsStream(nombreArchivoFrecuencia);
		
		if (inputStream == null) {
			throw new FileNotFoundException("No se encontró el archivo '" + nombreArchivoFrecuencia + "' en el classpath");
		}

		try (Scanner freqRead = new Scanner(inputStream, Constantes.ENCODING)) {

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
			}
		} catch (NoSuchElementException | IllegalStateException e) {
			System.err.println("Error reading file: " + nombreArchivoFrecuencia);
			throw new RuntimeException("Error reading file: " + nombreArchivoFrecuencia, e);
		}
	}
}

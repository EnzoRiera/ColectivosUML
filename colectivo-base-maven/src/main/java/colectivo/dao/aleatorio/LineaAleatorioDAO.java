package colectivo.dao.aleatorio;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.AConnection;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.dao.secuencial.LineaSecuencialDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.util.Factory;
import colectivo.util.FileUtil;

/**
 * Implementación de {@link LineaDAO} para acceso aleatorio a archivos binarios.
 * <p>
 * Esta clase gestiona la persistencia y recuperación de líneas de colectivo desde un archivo
 * de acceso aleatorio binario ({@link RandomAccessFile}). Permite lectura y escritura eficiente
 * de registros de líneas con sus paradas asociadas y frecuencias de paso.
 * </p>
 * <p>
 * <b>Estructura del registro en el archivo:</b>
 * <ul>
 *   <li><b>Marcador de borrado</b> (char): ' ' si está activo, {@link FileUtil#DELETED} si está eliminado</li>
 *   <li><b>Código</b> (String de 10 caracteres): identificador único de la línea</li>
 *   <li><b>Nombre</b> (String de 50 caracteres): nombre descriptivo de la línea</li>
 *   <li><b>Número de paradas</b> (int): cantidad de paradas que tiene la línea</li>
 *   <li><b>Códigos de paradas</b> (int[]): array de códigos de paradas en secuencia</li>
 *   <li><b>Número de frecuencias</b> (int): cantidad total de frecuencias registradas</li>
 *   <li><b>Frecuencias</b> (pares de int y String): día de semana (1-7) y hora (5 caracteres)</li>
 * </ul>
 * </p>
 * <p>
 * Si el archivo está vacío al momento de la construcción, se puebla automáticamente
 * desde {@link LineaSecuencialDAO}.
 * </p>
 *
 * @see LineaDAO
 * @see Linea
 * @see Parada
 * @see RandomAccessFile
 * @see AConnection
 * @see LineaSecuencialDAO
 */
public class LineaAleatorioDAO implements LineaDAO {

	private static final Logger logger = LogManager.getLogger(LineaAleatorioDAO.class);

	private final RandomAccessFile archivo;
	private final Map<String, Linea> lineas;
	private final Map<Integer, Parada> paradas;

	/** Tamaño fijo en caracteres para el campo código de línea en el archivo. */
	private static final int SIZE_CODIGO = 10;

	/** Tamaño fijo en caracteres para el campo nombre de línea en el archivo. */
	private static final int SIZE_NOMBRE = 50;

	/** Tamaño fijo en caracteres para el campo hora en el archivo (formato HH:mm). */
	private static final int SIZE_HORA = 5;

	/**
	 * Constructor por defecto.
	 * <p>
	 * Inicializa el archivo de acceso aleatorio para líneas usando {@link AConnection},
	 * carga las paradas disponibles y luego:
	 * <ul>
	 *   <li>Si el archivo está vacío, lo puebla desde {@link LineaSecuencialDAO}</li>
	 *   <li>Si el archivo tiene contenido, carga las líneas existentes</li>
	 * </ul>
	 * </p>
	 *
	 * @throws RuntimeException si ocurre un error de I/O al abrir el archivo
	 * @see AConnection#getInstancia(String)
	 * @see #cargarParadas()
	 * @see #llenarDesdeSecuencial()
	 * @see #cargarLineasDesdeArchivo()
	 */
	public LineaAleatorioDAO() {
		try {
			this.archivo = AConnection.getInstancia("linea");
			this.lineas = new TreeMap<>();
			this.paradas = cargarParadas();

			if (archivo.length() == 0L) {
				llenarDesdeSecuencial();
			} else {
				cargarLineasDesdeArchivo();
			}

		} catch (IOException e) {
			logger.error("Error al abrir archivo de acceso aleatorio para líneas", e);
			throw new RuntimeException("Error al abrir archivo de acceso aleatorio para líneas", e);
		}
	}

	/**
	 * Busca y retorna todas las líneas cargadas en memoria.
	 * <p>
	 * Retorna una copia del mapa de líneas para evitar modificaciones externas.
	 * Las líneas se mantienen ordenadas por código gracias a {@link TreeMap}.
	 * </p>
	 *
	 * @return un {@link Map} con todas las líneas indexadas por su código
	 * @throws Exception si ocurre un error (aunque en esta implementación no se espera)
	 */
	@Override
	public Map<String, Linea> buscarTodos() throws Exception {
		return new TreeMap<>(lineas);
	}

	/**
	 * Carga todas las paradas desde el {@link ParadaDAO}.
	 * <p>
	 * Obtiene la instancia de {@link ParadaDAO} desde {@link Factory} y carga todas las paradas disponibles.
	 * Si ocurre un error de ejecución ({@link RuntimeException}), lo propaga.
	 * Si ocurre cualquier otra excepción o si ParadaDAO es null, registra una advertencia y retorna un mapa vacío.
	 * </p>
	 *
	 * @return un {@link Map} con las paradas indexadas por su código
	 * @throws RuntimeException si ocurre un error de ejecución al cargar las paradas
	 * @see Factory#getInstancia(String, Class)
	 * @see ParadaDAO#buscarTodos()
	 */
	private Map<Integer, Parada> cargarParadas() {
		try {
			ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
			if (paradaDAO != null) {
				Map<Integer, Parada> paradasCargadas = paradaDAO.buscarTodos();
				return (paradasCargadas != null) ? paradasCargadas : new HashMap<>();
			}
			logger.warn("ParadaDAO es null, usando mapa vacío");
			return new HashMap<>();
		} catch (RuntimeException re) {
			logger.error("Error en cargarParadas (RuntimeException)", re);
			throw re;
		} catch (Exception e) {
			logger.warn("No se pudo cargar mapa de paradas", e);
			return new HashMap<>();
		}
	}

	/**
	 * Carga todas las líneas desde el archivo de acceso aleatorio a memoria.
	 * <p>
	 * Recorre el archivo desde el inicio hasta el final, leyendo cada registro mediante
	 * {@link #leerRegistro()}. Los registros válidos (no nulos) se agregan al mapa de líneas.
	 * </p>
	 * <p>
	 * Si se alcanza el final del archivo inesperadamente ({@link EOFException}), termina
	 * la lectura normalmente. Si ocurre otro error de I/O, lo registra con la posición del
	 * archivo donde ocurrió el error y lo propaga.
	 * </p>
	 *
	 * @throws IOException si ocurre un error al leer desde el archivo
	 * @see #leerRegistro()
	 * @see RandomAccessFile#seek(long)
	 * @see RandomAccessFile#getFilePointer()
	 */
	private void cargarLineasDesdeArchivo() throws IOException {
		archivo.seek(0L);

		while (archivo.getFilePointer() < archivo.length()) {
			try {
				Linea linea = leerRegistro();
				if (linea != null) {
					lineas.put(linea.getCodigo(), linea);
				}
			} catch (EOFException e) {
				return;
			} catch (IOException ioe) {
				long pos = 0L;
				try {
					pos = archivo.getFilePointer();
				} catch (IOException ignore) {
					// ignore
				}
				logger.error("Error leyendo archivo de líneas en posición " + pos, ioe);
				throw new IOException("Error leyendo archivo de líneas en posición " + pos, ioe);
			}
		}
	}

	/**
	 * Puebla el archivo de acceso aleatorio desde {@link LineaSecuencialDAO}.
	 * <p>
	 * Este método se invoca cuando el archivo está vacío. Realiza los siguientes pasos:
	 * <ol>
	 *   <li>Obtiene una instancia de {@link ParadaDAO} desde {@link Factory}</li>
	 *   <li>Crea una instancia de {@link LineaSecuencialDAO} con el ParadaDAO</li>
	 *   <li>Carga todas las líneas desde el DAO secuencial</li>
	 *   <li>Por cada línea, la escribe al final del archivo usando {@link #escribirRegistro(Linea)}</li>
	 *   <li>Agrega la línea al mapa de líneas en memoria</li>
	 * </ol>
	 * </p>
	 * <p>
	 * Si {@link ParadaDAO} no está disponible, registra un error y retorna sin hacer nada.
	 * Si ocurre un error de I/O o de ejecución durante el proceso, lo encapsula en una
	 * {@link RuntimeException} y la propaga.
	 * </p>
	 *
	 * @throws RuntimeException si ocurre un error al poblar el archivo desde el DAO secuencial
	 * @see LineaSecuencialDAO
	 * @see #escribirRegistro(Linea)
	 * @see Factory#getInstancia(String, Class)
	 */
	private void llenarDesdeSecuencial() {
		try {
			ParadaDAO paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class);
			if (paradaDAO == null) {
				logger.error("ParadaDAO no disponible, no se puede poblar líneas");
				return;
			}

			LineaSecuencialDAO secDAO = new LineaSecuencialDAO(paradaDAO);
			Map<String, Linea> secLineas = secDAO.buscarTodos();

			if (secLineas == null || secLineas.isEmpty()) {
				return;
			}

			for (Linea linea : secLineas.values()) {
				archivo.seek(archivo.length());
				escribirRegistro(linea);
				lineas.put(linea.getCodigo(), linea);
			}

		} catch (IOException ioe) {
			logger.error("I/O error poblando archivo aleatorio desde secuencial", ioe);
			throw new RuntimeException("I/O error poblando archivo aleatorio desde secuencial", ioe);
		} catch (RuntimeException re) {
			logger.error("Error de ejecución poblando archivo aleatorio desde secuencial", re);
			throw re;
		} catch (Exception e) {
			logger.error("Error inesperado poblando archivo aleatorio desde secuencial", e);
			throw new RuntimeException("Error inesperado poblando archivo aleatorio desde secuencial", e);
		}
	}

	/**
	 * Lee un registro completo de línea desde la posición actual del archivo.
	 * <p>
	 * Lee la información del registro en el siguiente orden:
	 * <ol>
	 *   <li>Marcador de borrado (char)</li>
	 *   <li>Código de la línea ({@value #SIZE_CODIGO} caracteres)</li>
	 *   <li>Nombre de la línea ({@value #SIZE_NOMBRE} caracteres)</li>
	 *   <li>Número de paradas (int)</li>
	 *   <li>Por cada parada: código de parada (int)</li>
	 *   <li>Número de frecuencias (int)</li>
	 *   <li>Por cada frecuencia: día de semana (int) y hora ({@value #SIZE_HORA} caracteres)</li>
	 * </ol>
	 * </p>
	 * <p>
	 * Si el registro está marcado como eliminado ({@link FileUtil#DELETED}) o tiene un código
	 * en blanco, se saltan todos los datos del registro y retorna null.
	 * </p>
	 * <p>
	 * Para líneas circulares, implementa control de duplicados: solo agrega una parada a la
	 * línea si no está ya presente, evitando duplicaciones. También mantiene la consistencia
	 * bidireccional entre líneas y paradas.
	 * </p>
	 * <p>
	 * Si encuentra una hora con formato inválido, registra una advertencia pero continúa
	 * procesando las demás frecuencias. Si ninguna parada del registro es válida,
	 * retorna null.
	 * </p>
	 *
	 * @return la {@link Linea} leída del archivo, o null si el registro está eliminado,
	 *         es inválido o no tiene paradas válidas
	 * @throws IOException si ocurre un error al leer desde el archivo
	 * @throws EOFException si se alcanza el final del archivo inesperadamente
	 * @see FileUtil#readString(RandomAccessFile, int)
	 * @see Linea#agregarParada(Parada)
	 * @see Parada#agregarLinea(Linea)
	 * @see Linea#agregarFrecuencia(int, LocalTime)
	 */
	private Linea leerRegistro() throws IOException {
		char deleted = archivo.readChar();
		String codigo = FileUtil.readString(archivo, SIZE_CODIGO).strip();
		String nombre = FileUtil.readString(archivo, SIZE_NOMBRE).strip();
		int numParadas = archivo.readInt();

		if (deleted == FileUtil.DELETED || codigo.isBlank()) {
			// Evitar procesar datos para registros eliminados o inválidos
			for (int i = 0; i < numParadas; i++) {
				archivo.readInt(); // saltarse codigoParada
			}
			int numFrecuencias = archivo.readInt();
			for (int i = 0; i < numFrecuencias; i++) {
				archivo.readInt(); // saltarse diaSemana
				FileUtil.readString(archivo, SIZE_HORA); // saltarse hora
			}
			return null;
		}

		// Crear Linea y cargar paradas
		Linea linea = new Linea(codigo, nombre);
		boolean tieneParadasValidas = false;

		for (int i = 0; i < numParadas; i++) {
			int codigoParada = archivo.readInt();
			Parada parada = paradas.get(codigoParada);
			if (parada != null) {
				// Control de duplicados para líneas circulares:
				// Solo agregamos la parada si no está ya en la línea
				if (!linea.getParadas().contains(parada)) {
					linea.agregarParada(parada);
					logger.debug("Parada {} agregada a línea {}", codigoParada, codigo);
				} else {
					logger.debug("Parada {} ya existe en línea {}, evitando duplicado", codigoParada, codigo);
				}
				// Control adicional: verificar que la línea esté en la parada
				// (necesario porque agregarParada llama a parada.agregarLinea)
				if (!parada.getLineas().contains(linea)) {
					parada.agregarLinea(linea);
				}
				tieneParadasValidas = true;
			} else {
				logger.warn("Parada " + codigoParada + " no encontrada para línea " + codigo);
			}
		}

		int numFrecuencias = archivo.readInt();
		for (int i = 0; i < numFrecuencias; i++) {
			int diaSemana = archivo.readInt();
			String horaStr = FileUtil.readString(archivo, SIZE_HORA).strip();

			if (!horaStr.isBlank()) {
				try {
					LocalTime hora = LocalTime.parse(horaStr);
					linea.agregarFrecuencia(diaSemana, hora);
				} catch (DateTimeParseException ex) {
                    logger.warn("Formato de hora inválido '" + horaStr + "' para la línea " + codigo, ex);
                }
			}
		}

		return tieneParadasValidas ? linea : null;
	}

	/**
	 * Escribe un registro completo de línea en la posición actual del archivo.
	 * <p>
	 * Escribe la información del registro en el siguiente orden:
	 * <ol>
	 *   <li>Marcador activo (char: ' ')</li>
	 *   <li>Código de la línea, ajustado a {@value #SIZE_CODIGO} caracteres</li>
	 *   <li>Nombre de la línea, ajustado a {@value #SIZE_NOMBRE} caracteres</li>
	 *   <li>Número de paradas (int)</li>
	 *   <li>Por cada parada: código de parada (int)</li>
	 *   <li>Número total de frecuencias (int)</li>
	 *   <li>Por cada frecuencia ordenada por día: día de semana (int) y hora ajustada a {@value #SIZE_HORA} caracteres</li>
	 * </ol>
	 * </p>
	 * <p>
	 * Las cadenas se ajustan al tamaño fijo usando {@link #rellenarORecortar(String, int)}.
	 * Las frecuencias se escriben ordenadas por día de semana (1-7) y luego por hora.
	 * </p>
	 *
	 * @param linea la línea a escribir en el archivo
	 * @throws IllegalArgumentException si la línea es null
	 * @throws IOException si ocurre un error al escribir en el archivo
	 * @see FileUtil#writeString(RandomAccessFile, String, int)
	 * @see Linea#getParadas()
	 * @see Linea#getHorasFrecuencia(int)
	 * @see #rellenarORecortar(String, int)
	 */
	private void escribirRegistro(Linea linea) throws IOException {
		if (linea == null) {
			throw new IllegalArgumentException("Linea no puede ser null");
		}

		// Escribir header
		archivo.writeChar(' '); // active flag
		FileUtil.writeString(archivo, rellenarORecortar(linea.getCodigo(), SIZE_CODIGO), SIZE_CODIGO);
		FileUtil.writeString(archivo, rellenarORecortar(linea.getNombre(), SIZE_NOMBRE), SIZE_NOMBRE);

		// Escribir paradas
		int numParadas = linea.getParadas().size();
		archivo.writeInt(numParadas);
		for (Parada parada : linea.getParadas()) {
			archivo.writeInt(parada.getCodigo());
		}

		// Contar y escribir frecuencias
		int totalFrecuencias = 0;
		for (int dia = 1; dia <= 7; dia++) {
			totalFrecuencias += linea.getHorasFrecuencia(dia).size();
		}
		archivo.writeInt(totalFrecuencias);

		for (int dia = 1; dia <= 7; dia++) {
			for (LocalTime hora : linea.getHorasFrecuencia(dia)) {
				archivo.writeInt(dia);
				FileUtil.writeString(archivo, rellenarORecortar(hora.toString(), SIZE_HORA), SIZE_HORA);
			}
		}
	}

	/**
	 * Ajusta una cadena a una longitud específica de caracteres.
	 * <p>
	 * Comportamiento según la longitud de la cadena de entrada:
	 * <ul>
	 *   <li>Si es null, se trata como cadena vacía</li>
	 *   <li>Si tiene la longitud exacta, se retorna sin cambios</li>
	 *   <li>Si es más larga, se recorta desde el final</li>
	 *   <li>Si es más corta, se rellena con espacios al final</li>
	 * </ul>
	 * </p>
	 *
	 * @param s la cadena a ajustar (puede ser null)
	 * @param longitudCaracteres la longitud deseada en caracteres
	 * @return la cadena ajustada a la longitud especificada
	 * @see String#format(String, Object...)
	 * @see String#substring(int, int)
	 */
	private static String rellenarORecortar(String s, int longitudCaracteres) {
		if (s == null) {
			s = "";
		}
		if (s.length() == longitudCaracteres) {
			return s;
		}
		if (s.length() > longitudCaracteres) {
			return s.substring(0, longitudCaracteres);
		}
		return String.format("%-" + longitudCaracteres + "s", s);
	}
}

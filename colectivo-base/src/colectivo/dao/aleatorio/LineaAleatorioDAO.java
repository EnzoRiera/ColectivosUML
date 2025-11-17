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

import colectivo.aplicacion.Configuracion;
import colectivo.conexion.AConnection;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.dao.secuencial.LineaSecuencialDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.util.Factory;
import colectivo.util.FileUtil;

/**
 * Random-access DAO implementation for {@link Linea} data persistence.
 *
 * <p>
 * This DAO manages a single binary file storing line definitions (codigo,
 * nombre, paradas) and their associated frequencies in variable-length records.
 * If the file does not exist on first instantiation, it is populated from
 * {@link LineaSecuencialDAO}.
 * </p>
 *
 * <p>
 * Binary record format (variable-length):
 * <ul>
 * <li>char - deletion flag (space = active, FileUtil.DELETED (currently '\*') =
 * deleted)</li>
 * <li>SIZE_CODIGO chars - line code (10 chars)</li>
 * <li>SIZE_NOMBRE chars - line name (50 chars)</li>
 * <li>int - count of stops</li>
 * <li>count × int - stop codes</li>
 * <li>int - count of frequencies</li>
 * <li>count × Frecuencia - frequency records (diaSemana:int + hora:SIZE_HORA
 * chars)
 * <p>
 * <em>Nota:</em> `FileUtil.writeString` escribe Java `char` usando
 * `writeChars`, por lo que cada carácter ocupa 2 bytes en disco. Por tanto cada
 * frecuencia ocupa 4 (int diaSemana) + (SIZE_HORA × 2) bytes. Con SIZE_HORA = 5
 * una frecuencia ocupa 14 bytes en el fichero.
 * </p>
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * Design decisions:
 * <ul>
 * <li>Single file design for consistency with {@link TramoAleatorioDAO}</li>
 * <li>Variable-length records to accommodate different numbers of stops and
 * frequencies</li>
 * <li>In-memory map for fast lookups after initial load</li>
 * <li>Shared {@link ParadaDAO} instance via Factory to avoid stop
 * duplication</li>
 * </ul>
 * </p>
 *
 * @author Miyo
 * @version 2.0
 * @since 1.0
 */
public class LineaAleatorioDAO implements LineaDAO {

    private static final Logger logger = LogManager.getLogger(LineaAleatorioDAO.class);

    private final RandomAccessFile archivo;
    private final Map<String, Linea> lineas;
    private Map<Integer, Parada> paradas;

    private static final int SIZE_CODIGO = 10;
    private static final int SIZE_NOMBRE = 50;
    private static final int SIZE_HORA = 5;

    /**
     * Constructs a new LineaAleatorioDAO and initializes the binary file.
     *
     * <p>
     * If the binary file is empty, this constructor populates it from the
     * sequential DAO. Otherwise, it reads existing data from the file. Paradas are
     * loaded from the shared {@link ParadaDAO} instance obtained via
     * {@link Factory}.
     * </p>
     *
     * @throws RuntimeException if file initialization or data loading fails
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
     * Returns all loaded {@link Linea} objects.
     *
     * @return a defensive copy of the lines map
     * @throws Exception if an error occurs during retrieval
     */
    @Override
    public Map<String, Linea> buscarTodos() throws Exception {
        return new TreeMap<>(lineas);
    }

    /**
     * Loads the map of {@link Parada} instances from the Factory.
     *
     * <p>
     * Obtains the ParadaDAO instance via Factory and loads all stops in memory. If
     * the DAO cannot be obtained or the map is empty, initializes an empty HashMap
     * and logs a warning.
     * </p>
     *
     * @return map of stops keyed by code (never {@code null})
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
            logger.warn("No se pudo cargar mapa de paradas: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Loads all active lines from the binary file into memory.
     *
     * <p>
     * Sequentially reads all records from the file, loading only those marked as
     * active. Each record contains line metadata, associated stops, and
     * frequencies. Deleted records are skipped without including them in the
     * in-memory map.
     * </p>
     *
     * @throws IOException if file reading fails
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
     * Populates the binary file from the sequential DAO.
     *
     * <p>
     * This method is called only when the binary file is empty (first run). It
     * reads all lines from {@link LineaSecuencialDAO} and writes them to the binary
     * file in the appropriate format.
     * </p>
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
     * Reads a complete line record from the current file position.
     *
     * <p>
     * Reading format:
     * <ol>
     * <li>Deletion flag (1 char)</li>
     * <li>Line code (SIZE_CODIGO chars)</li>
     * <li>Line name (SIZE_NOMBRE chars)</li>
     * <li>Number of stops (int)</li>
     * <li>Stop codes (numParadas × int)</li>
     * <li>Number of frequencies (int)</li>
     * <li>Frequency records (numFrecuencias × [diaSemana:int + hora:SIZE_HORA
     * chars])</li>
     * </ol>
     *
     * <p>
     * Returns {@code null} if:
     * <ul>
     * <li>The record is marked as deleted</li>
     * <li>Line code is empty</li>
     * <li>Referenced stops are not found (line without valid stops)</li>
     * </ul>
     *
     * @return constructed Linea object from record, or {@code null} if
     *         invalid/deleted
     * @throws IOException if file reading fails
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
                linea.agregarParada(parada);
                if (!parada.getLineas().contains(linea)) {
                    parada.agregarLinea(linea);
                }
                tieneParadasValidas = true;
            } else {
                logger.warn("Parada " + codigoParada + " no encontrada para línea " + codigo);
            }
        }

        // Load frequencies
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
     * Writes a line record at the current file position.
     *
     * <p>
     * Writes the line with active flag, all metadata, stops, and frequencies in the
     * standard variable-length binary format.
     * </p>
     *
     * @param linea line to write to file
     * @throws IOException              if writing fails
     * @throws IllegalArgumentException if linea is {@code null}
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
     * Pads or trims a string to the specified character length.
     *
     * @param s           the input string (may be {@code null}, treated as empty)
     * @param longitudCaracteres the desired length in characters
     * @return a string of exactly {@code lengthChars} characters
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

package colectivo.dao.aleatorio;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.conexion.AConnection;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.dao.secuencial.TramoSecuencialDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;
import colectivo.util.FileUtil;

/**
 * Random-access DAO implementation for {@link Tramo} records.
 *
 * <p>This DAO manages a single binary file with fixed-length records for transport segments.
 * If the file does not exist on first instantiation, it is populated from
 * {@link TramoSecuencialDAO}.</p>
 *
 * <p>Record format (fixed-length):</p>
 * <ul>
 *   <li>char - deletion flag (space = active, FileUtil.DELETED (currently '\*') = deleted)</li>
 *   <li>SIZE_CODIGO chars - origin stop code (integer as string)</li>
 *   <li>SIZE_CODIGO chars - destination stop code (integer as string)</li>
 *   <li>int - travel time in minutes</li>
 *   <li>int - segment type (1 = bus, 2 = walking)</li>
 * </ul>
 *
 * <p>Total record size: 50 bytes (calculated using Character.BYTES = 2)</p>
 *
 * @author POO-2025
 * @version 1.0
 * @since 1.0
 * @see TramoDAO
 * @see Tramo
 */
public class TramoAleatorioDAO implements TramoDAO {

    private static final Logger logger = LogManager.getLogger(TramoAleatorioDAO.class);

    private final RandomAccessFile file;
    private final Map<String, Tramo> tramos;
    private final Map<Integer, Parada> paradas;

    private static final int SIZE_CODIGO = 10;
    private static final int SIZE_RECORD = Character.BYTES // deletion flag
            + (Character.BYTES * SIZE_CODIGO * 2) // origen + destino
            + Integer.BYTES // tiempo
            + Integer.BYTES; // tipo

    /**
     * Constructs a new TramoAleatorioDAO and initializes the binary file.
     *
     * <p>If the tramo binary file is empty, this constructor populates it
     * from the sequential DAO. Otherwise, it reads existing data from the file.</p>
     *
     * @throws RuntimeException if file initialization or data loading fails
     */
    public TramoAleatorioDAO() {
        try {
            this.file = AConnection.getInstancia("tramo");
            this.tramos = new TreeMap<>();
            this.paradas = cargarParadas();

            if (file.length() == 0L) {
                llenarDesdeSecuencial();
            } else {
                cargarTramosDesdeArchivo();
            }

        } catch (IOException e) {
            logger.error("Error al abrir archivo de acceso aleatorio para tramos", e);
            throw new RuntimeException("Error al abrir archivo de acceso aleatorio para tramos", e);
        }
    }

    /**
     * Returns all loaded {@link Tramo} objects.
     *
     * @return a defensive copy of the segments map
     * @throws Exception if an error occurs during retrieval
     */
    @Override
    public Map<String, Tramo> buscarTodos() throws Exception {
        return new TreeMap<>(tramos);
    }

    /**
     * Loads the map of {@link Parada} instances from the Factory.
     *
     * <p>Obtains the ParadaDAO instance via Factory and loads all stops in memory.
     * If the DAO cannot be obtained or the map is empty, initializes an empty HashMap
     * and logs a warning.</p>
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
            logger.error("No se pudo cargar mapa de paradas", e);
            throw new RuntimeException("No se pudo cargar mapa de paradas", e);
        }
    }

    /**
     * Loads all active segments from the binary file into memory.
     *
     * <p>Sequentially reads all records from the file, loading only those marked as active.
     * Deleted records are skipped without including them in the in-memory map.</p>
     *
     * @throws IOException if file reading fails
     */
    private void cargarTramosDesdeArchivo() throws IOException {
        file.seek(0L);

        while (file.getFilePointer() < file.length()) {
            try {
                Tramo tramo = leerRegistro();
                if (tramo != null) {
                    String key = crearKey(
                            tramo.getInicio().getCodigo(),
                            tramo.getFin().getCodigo(),
                            tramo.getTipo()
                    );
                    tramos.put(key, tramo);
                }
            } catch (EOFException eof) {
                // Fin de archivo esperado al terminar un registro
                return;
            } catch (IOException ioe) {
                long pos;
                try {
                    pos = file.getFilePointer();
                } catch (IOException ex) {
                    pos = -1L;
                }
                logger.error("Error leyendo archivo de tramos en posición " + pos, ioe);
                throw new IOException("Error leyendo archivo de tramos en posición " + pos, ioe);
            }
        }
    }

    /**
     * Populates the binary file from the sequential DAO.
     *
     * <p>This method is called only when the binary file is empty (first run).
     * It reads all segments from {@link TramoSecuencialDAO} and writes them
     * to the binary file in the appropriate format.</p>
     * 
     * Wraps I/O or unexpected errors in a RuntimeException so initialization failures are not silently ignored.
     */
    private void llenarDesdeSecuencial() {
        try {
            TramoSecuencialDAO secDAO = new TramoSecuencialDAO();
            Map<String, Tramo> secTramos = secDAO.buscarTodos();

            if (secTramos == null || secTramos.isEmpty()) {
                return;
            }

            for (Tramo tramo : secTramos.values()) {
                file.seek(file.length());
                escribirRegistro(tramo);
                String key = crearKey(
                        tramo.getInicio().getCodigo(),
                        tramo.getFin().getCodigo(),
                        tramo.getTipo()
                );
                tramos.put(key, tramo);
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
     * Reads a complete record from the current file position.
     *
     * <p>Reading format:</p>
     * <ol>
     *   <li>Deletion flag (1 char)</li>
     *   <li>Origin stop code (SIZE_CODIGO chars)</li>
     *   <li>Destination stop code (SIZE_CODIGO chars)</li>
     *   <li>Travel time in minutes (int)</li>
     *   <li>Segment type (int: 1=bus, 2=walking)</li>
     * </ol>
     *
     * <p>Returns {@code null} if:</p>
     * <ul>
     *   <li>The record is marked as deleted</li>
     *   <li>Stop codes are empty</li>
     *   <li>Stop codes are not valid numbers</li>
     *   <li>Referenced stops are not found</li>
     * </ul>
     *
     * @return constructed Tramo object from record, or {@code null} if invalid/deleted
     * @throws IOException if file reading fails
     */
    private Tramo leerRegistro() throws IOException {
        char deleted = file.readChar();
        String codigoIniStr = FileUtil.readString(file, SIZE_CODIGO).strip();
        String codigoFinStr = FileUtil.readString(file, SIZE_CODIGO).strip();
        int tiempo = file.readInt();
        int tipo = file.readInt();

        if (deleted == FileUtil.DELETED || codigoIniStr.isBlank() || codigoFinStr.isBlank()) {
            return null;
        }

        try {
            int codigoIni = Integer.parseInt(codigoIniStr);
            int codigoFin = Integer.parseInt(codigoFinStr);

            Parada inicio = paradas.get(codigoIni);
            Parada fin = paradas.get(codigoFin);

            if (inicio == null || fin == null) {
                return null;
            }

            return new Tramo(inicio, fin, tiempo, tipo);

        } catch (NumberFormatException nfe) {
            long pos = -1L;
            try {
                pos = file.getFilePointer();
            } catch (IOException ignored) {
                // ignore
            }
            if (pos >= 0) {
                logger.warn("Código numérico inválido en registro en posición " + pos + ": " + nfe.getMessage(), nfe);
            } else {
                logger.warn("Código numérico inválido en registro: " + nfe.getMessage(), nfe);
            }
            return null;
        }
    }

    /**
     * Writes a segment record at the current file position.
     *
    * <p>Writes the segment with active flag and all its data in the standard
    * 50-byte binary format (chars are 2 bytes each in the file).</p>
     *
     * @param tramo segment to write to file
     * @throws IOException if writing fails
     * @throws IllegalArgumentException if tramo is {@code null}
     */
    private void escribirRegistro(Tramo tramo) throws IOException {
        if (tramo == null) {
            throw new IllegalArgumentException("Tramo no puede ser null");
        }

        file.writeChar(' '); // active flag

        String iniStr = rellenarORecortar(Integer.toString(tramo.getInicio().getCodigo()), SIZE_CODIGO);
        String finStr = rellenarORecortar(Integer.toString(tramo.getFin().getCodigo()), SIZE_CODIGO);

        FileUtil.writeString(file, iniStr, SIZE_CODIGO);
        FileUtil.writeString(file, finStr, SIZE_CODIGO);
        file.writeInt(tramo.getTiempo());
        file.writeInt(tramo.getTipo());
    }

    /**
     * Creates the unique key for indexing a segment in the map.
     *
     * <p>Format: "originCode-destinationCode-type"</p>
     * <p>Example: "88-97-1" (from stop 88 to 97, bus type)</p>
     *
     * @param codigoIni origin stop code
     * @param codigoFin destination stop code
     * @param tipo segment type (1=bus, 2=walking)
     * @return key string in standard format
     */
    private static String crearKey(int codigoIni, int codigoFin, int tipo) {
        return codigoIni + "-" + codigoFin + "-" + tipo;
    }

    /**
     * Pads or trims a string to the specified character length.
     *
     * @param s the input string (may be {@code null}, treated as empty)
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

package colectivo.dao.aleatorio;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.conexion.AConnection;
import colectivo.dao.ParadaDAO;
import colectivo.dao.secuencial.ParadaSecuencialDAO;
import colectivo.modelo.Parada;
import colectivo.util.FileUtil;

/**
 * Random-access DAO implementation for {@link Parada} records.
 *
 * <p>
 * This DAO manages a single binary file with fixed-length records for bus stops.
 * If the file does not exist on first instantiation, it is populated from
 * {@link ParadaSecuencialDAO}.
 * </p>
 *
 * <p>
 * Record format (fixed-length):
 * <ul>
 *   <li>char - deletion flag (space = active, FileUtil.DELETED (currently '*') = deleted)</li>
 *   <li>SIZE_CODIGO chars - stop code (integer as string)</li>
 *   <li>SIZE_DIRECCION chars - address/name</li>
 *   <li>SIZE_COORDENADAS chars - latitude (decimal as STRING, normalized with '.' as decimal separator)</li>
 *   <li>SIZE_COORDENADAS chars - longitude (decimal as STRING, normalized with '.' as decimal separator)</li>
 * </ul>
 * </p>
 *
 * @author Miyo
 * @version 1.0
 * @since 1.0
 */
public class ParadaAleatorioDAO implements ParadaDAO {

    private static final Logger logger = LogManager.getLogger(ParadaAleatorioDAO.class);

    private final RandomAccessFile file;
    private final Map<Integer, Parada> paradas;

    private static final int SIZE_CODIGO = 10;
    private static final int SIZE_DIRECCION = 50;
    private static final int SIZE_COORDENADAS = 20;

    /**
     * Constructs a new ParadaAleatorioDAO and initializes the binary file.
     *
     * <p>
     * If the parada binary file is empty, this constructor populates it
     * from the sequential DAO. Otherwise, it reads existing data from the file.
     * </p>
     *
     * @throws RuntimeException if file initialization or data loading fails
     */
    public ParadaAleatorioDAO() {
        try {
            this.file = AConnection.getInstancia("parada");
            this.paradas = new TreeMap<>();

            if (file.length() == 0L) {
                llenarDesdeSecuencial();
            } else {
                cargarParadasDesdeArchivo();
            }

        } catch (IOException e) {
            logger.error("Error al abrir archivo de acceso aleatorio para paradas", e);
            throw new RuntimeException("Error al abrir archivo de acceso aleatorio para paradas", e);
        }
    }

    /**
     * Returns all loaded {@link Parada} objects.
     *
     * @return a defensive copy of the stops map
     * @throws Exception if an error occurs during retrieval
     */
    @Override
    public Map<Integer, Parada> buscarTodos() throws Exception {
        return new TreeMap<>(paradas);
    }

    /**
     * Loads stop records from the binary file.
     *
     * <p>
     * Reads all records sequentially, skipping deleted entries.
     * Handles malformed numeric fields gracefully.
     * </p>
     *
     * @throws IOException if file reading fails
     */
    private void cargarParadasDesdeArchivo() throws IOException {
        file.seek(0L);

        while (file.getFilePointer() < file.length()) {
            try {
                Parada parada = leerRegistro();
                if (parada != null) {
                    paradas.put(parada.getCodigo(), parada);
                }

            } catch (EOFException eof) {
                logger.warn("Fin de archivo alcanzado inesperadamente: " + eof.getMessage(), eof);
                return;
            }
        }
    }

    /**
     * Populates the binary file from the sequential DAO.
     *
     * <p>
     * This method is called only when the file is empty (first run).
     * It reads all stops from {@link ParadaSecuencialDAO} and writes them
     * to the binary file in the appropriate format.
     * </p>
     */
    private void llenarDesdeSecuencial() {
        try {
            ParadaSecuencialDAO secDAO = new ParadaSecuencialDAO();
            Map<Integer, Parada> secParadas = secDAO.buscarTodos();

            if (secParadas == null || secParadas.isEmpty()) {
                return;
            }

            for (Parada parada : secParadas.values()) {
                escribirRegistro(parada);
                paradas.put(parada.getCodigo(), parada);
            }

        } catch (Exception e) {
            logger.error("Error al poblar archivo binario desde DAO secuencial", e);
            throw new RuntimeException("Error al poblar archivo binario desde DAO secuencial", e);
        }
    }

    /**
     * Reads a single {@link Parada} record from the current file position.
     *
     * <p>
     * Coordinates are stored as normalized strings (using '.' as decimal separator)
     * with fixed character length (SIZE_COORDENADAS). Malformed numeric fields cause the
     * record to be skipped with a warning.
     * </p>
     *
     * @return the parsed stop, or {@code null} if the record is deleted or malformed
     * @throws IOException if file reading fails
     */
    private Parada leerRegistro() throws IOException {
        try {
            char deleted = file.readChar();
            String codigoStr = FileUtil.readString(file, SIZE_CODIGO).strip();
            String direccion = FileUtil.readString(file, SIZE_DIRECCION).strip();
            String latStr = FileUtil.readString(file, SIZE_COORDENADAS).strip();
            String lonStr = FileUtil.readString(file, SIZE_COORDENADAS).strip();

            if (deleted == FileUtil.DELETED || codigoStr.isBlank()) {
                return null;
            }

            int codigo = Integer.parseInt(codigoStr);
            double latitud = Double.parseDouble(latStr);
            double longitud = Double.parseDouble(lonStr);

            return new Parada(codigo, direccion, latitud, longitud);

        } catch (NumberFormatException nfe) {
            logger.warn("Formato numérico inválido en registro: " + nfe.getMessage(), nfe);
            return null;
        }
    }

    /**
     * Writes a single {@link Parada} record to the binary file.
     *
     * <p>
     * Coordinates are normalized to use dot (.) as decimal separator
     * regardless of locale. All fields are validated before writing.
     * </p>
     *
     * @param parada the stop to write
     * @throws IOException if file writing fails
     * @throws IllegalArgumentException if parada data is invalid
     */
    private void escribirRegistro(Parada parada) throws IOException {
        if (parada == null) {
            throw new IllegalArgumentException("Parada no puede ser null");
        }

        int codigo = parada.getCodigo();
        if (codigo <= 0) {
            throw new IllegalArgumentException("Código de parada debe ser positivo");
        }

        String direccion = parada.getDireccion() == null ? "" : parada.getDireccion().strip();
        double lat = parada.getLatitud();
        double lon = parada.getLongitud();

        if (!Double.isFinite(lat) || !Double.isFinite(lon)) {
            throw new IllegalArgumentException("Latitud/longitud deben ser números finitos");
        }
        if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException("Latitud o longitud fuera del rango válido");
        }

        file.seek(file.length());
        file.writeChar(' ');

        String codigoStr = Integer.toString(codigo);
        String latStr = Double.toString(lat).replace(',', '.');
        String lonStr = Double.toString(lon).replace(',', '.');

        FileUtil.writeString(file, rellenarORecortar(codigoStr, SIZE_CODIGO), SIZE_CODIGO);
        FileUtil.writeString(file, rellenarORecortar(direccion, SIZE_DIRECCION), SIZE_DIRECCION);
        FileUtil.writeString(file, rellenarORecortar(latStr, SIZE_COORDENADAS), SIZE_COORDENADAS);
        FileUtil.writeString(file, rellenarORecortar(lonStr, SIZE_COORDENADAS), SIZE_COORDENADAS);
    }

    /**
     * Pads or trims a string to the specified character length.
     *
     * @param s the input string
     * @param longitudCaracteres the desired length
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

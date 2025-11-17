package colectivo.dao.aleatorio;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.AConnection;
import colectivo.dao.ParadaDAO;
import colectivo.dao.secuencial.ParadaSecuencialDAO;
import colectivo.modelo.Parada;
import colectivo.util.FileUtil;

/**
 * Implementación de {@link ParadaDAO} para acceso aleatorio a archivos binarios.
 * <p>
 * Esta clase gestiona la persistencia y recuperación de paradas de colectivo desde un archivo
 * de acceso aleatorio binario ({@link RandomAccessFile}). Permite lectura y escritura eficiente
 * de registros de paradas con su información geográfica.
 * </p>
 * <p>
 * <b>Estructura del registro en el archivo:</b>
 * <ul>
 *   <li><b>Marcador de borrado</b> (char): ' ' si está activo, {@link FileUtil#DELETED} si está eliminado</li>
 *   <li><b>Código</b> (String de 10 caracteres): identificador único de la parada</li>
 *   <li><b>Dirección</b> (String de 50 caracteres): ubicación física de la parada</li>
 *   <li><b>Latitud</b> (String de 20 caracteres): coordenada geográfica latitud</li>
 *   <li><b>Longitud</b> (String de 20 caracteres): coordenada geográfica longitud</li>
 * </ul>
 * </p>
 * <p>
 * Si el archivo está vacío al momento de la construcción, se puebla automáticamente
 * desde {@link ParadaSecuencialDAO}.
 * </p>
 *
 * @see ParadaDAO
 * @see Parada
 * @see RandomAccessFile
 * @see AConnection
 * @see ParadaSecuencialDAO
 */
public class ParadaAleatorioDAO implements ParadaDAO {

    private static final Logger logger = LogManager.getLogger(ParadaAleatorioDAO.class);

    private final RandomAccessFile file;
    private final Map<Integer, Parada> paradas;

    /** Tamaño fijo en caracteres para el campo código de parada en el archivo. */
    private static final int SIZE_CODIGO = 10;

    /** Tamaño fijo en caracteres para el campo dirección de parada en el archivo. */
    private static final int SIZE_DIRECCION = 50;

    /** Tamaño fijo en caracteres para el campo coordenadas (latitud/longitud) en el archivo. */
    private static final int SIZE_COORDENADAS = 20;

    /**
     * Constructor por defecto.
     * <p>
     * Inicializa el archivo de acceso aleatorio para paradas usando {@link AConnection},
     * y luego:
     * <ul>
     *   <li>Si el archivo está vacío, lo puebla desde {@link ParadaSecuencialDAO}</li>
     *   <li>Si el archivo tiene contenido, carga las paradas existentes</li>
     * </ul>
     * </p>
     *
     * @throws RuntimeException si ocurre un error de I/O al abrir el archivo
     * @see AConnection#getInstancia(String)
     * @see #llenarDesdeSecuencial()
     * @see #cargarParadasDesdeArchivo()
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
     * Busca y retorna todas las paradas cargadas en memoria.
     * <p>
     * Retorna una copia del mapa de paradas para evitar modificaciones externas.
     * Las paradas se mantienen ordenadas por código gracias a {@link TreeMap}.
     * </p>
     *
     * @return un {@link Map} con todas las paradas indexadas por su código
     * @throws Exception si ocurre un error (aunque en esta implementación no se espera)
     */
    @Override
    public Map<Integer, Parada> buscarTodos() throws Exception {
        return new TreeMap<>(paradas);
    }

    /**
     * Carga todas las paradas desde el archivo de acceso aleatorio a memoria.
     * <p>
     * Recorre el archivo desde el inicio hasta el final, leyendo cada registro mediante
     * {@link #leerRegistro()}. Los registros válidos (no nulos) se agregan al mapa de paradas
     * indexados por su código.
     * </p>
     * <p>
     * Si se alcanza el final del archivo inesperadamente ({@link EOFException}), registra
     * una advertencia y termina la lectura normalmente.
     * </p>
     *
     * @throws IOException si ocurre un error al leer desde el archivo
     * @see #leerRegistro()
     * @see RandomAccessFile#seek(long)
     * @see RandomAccessFile#getFilePointer()
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
            	logger.warn("Fin de archivo alcanzado inesperadamente", eof);
                return;
            }
        }
    }

    /**
     * Puebla el archivo de acceso aleatorio desde {@link ParadaSecuencialDAO}.
     * <p>
     * Este método se invoca cuando el archivo está vacío. Realiza los siguientes pasos:
     * <ol>
     *   <li>Crea una instancia de {@link ParadaSecuencialDAO}</li>
     *   <li>Carga todas las paradas desde el DAO secuencial</li>
     *   <li>Por cada parada, la escribe al final del archivo usando {@link #escribirRegistro(Parada)}</li>
     *   <li>Agrega la parada al mapa de paradas en memoria</li>
     * </ol>
     * </p>
     * <p>
     * Si el DAO secuencial retorna null o un mapa vacío, el método retorna sin hacer nada.
     * Si ocurre un error durante el proceso, lo registra, lo encapsula en una
     * {@link RuntimeException} y la propaga.
     * </p>
     *
     * @throws RuntimeException si ocurre un error al poblar el archivo desde el DAO secuencial
     * @see ParadaSecuencialDAO
     * @see #escribirRegistro(Parada)
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
            throw new RuntimeException("Error al poblar archivo binario desde DAO secuencial: ", e);
        }
    }

    /**
     * Lee un registro completo de parada desde la posición actual del archivo.
     * <p>
     * Lee la información del registro en el siguiente orden:
     * <ol>
     *   <li>Marcador de borrado (char)</li>
     *   <li>Código de la parada ({@value #SIZE_CODIGO} caracteres)</li>
     *   <li>Dirección de la parada ({@value #SIZE_DIRECCION} caracteres)</li>
     *   <li>Latitud ({@value #SIZE_COORDENADAS} caracteres)</li>
     *   <li>Longitud ({@value #SIZE_COORDENADAS} caracteres)</li>
     * </ol>
     * </p>
     * <p>
     * Si el registro está marcado como eliminado ({@link FileUtil#DELETED}) o tiene un código
     * en blanco, retorna null. Si encuentra un formato numérico inválido en código, latitud
     * o longitud, registra una advertencia y retorna null.
     * </p>
     *
     * @return la {@link Parada} leída del archivo, o null si el registro está eliminado,
     *         es inválido o tiene errores de formato
     * @throws IOException si ocurre un error al leer desde el archivo
     * @throws EOFException si se alcanza el final del archivo inesperadamente
     * @see FileUtil#readString(RandomAccessFile, int)
     * @see Parada#Parada(int, String, double, double)
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
            logger.warn("Formato numérico inválido en registro: ", nfe);
            return null;
        }
    }

    /**
     * Escribe un registro completo de parada en la posición actual del archivo.
     * <p>
     * Escribe la información del registro en el siguiente orden:
     * <ol>
     *   <li>Marcador activo (char: ' ')</li>
     *   <li>Código de la parada, ajustado a {@value #SIZE_CODIGO} caracteres</li>
     *   <li>Dirección de la parada, ajustada a {@value #SIZE_DIRECCION} caracteres</li>
     *   <li>Latitud, ajustada a {@value #SIZE_COORDENADAS} caracteres</li>
     *   <li>Longitud, ajustada a {@value #SIZE_COORDENADAS} caracteres</li>
     * </ol>
     * </p>
     * <p>
     * Las cadenas se ajustan al tamaño fijo usando {@link #rellenarORecortar(String, int)}.
     * Las coordenadas se convierten a String con punto decimal como separador.
     * </p>
     * <p>
     * <b>Validaciones:</b>
     * <ul>
     *   <li>La parada no puede ser null</li>
     *   <li>El código debe ser positivo (mayor que 0)</li>
     *   <li>Latitud y longitud deben ser números finitos</li>
     *   <li>Latitud debe estar en el rango [-90, 90]</li>
     *   <li>Longitud debe estar en el rango [-180, 180]</li>
     * </ul>
     * </p>
     *
     * @param parada la parada a escribir en el archivo
     * @throws IllegalArgumentException si la parada es null, tiene código inválido o coordenadas fuera de rango
     * @throws IOException si ocurre un error al escribir en el archivo
     * @see FileUtil#writeString(RandomAccessFile, String, int)
     * @see #rellenarORecortar(String, int)
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

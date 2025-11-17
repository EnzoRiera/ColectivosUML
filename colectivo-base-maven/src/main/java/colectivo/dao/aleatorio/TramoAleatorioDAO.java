package colectivo.dao.aleatorio;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.AConnection;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.dao.secuencial.TramoSecuencialDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;
import colectivo.util.FileUtil;

/**
 * Implementación de {@link TramoDAO} para acceso aleatorio a archivos binarios.
 * <p>
 * Esta clase gestiona la persistencia y recuperación de tramos entre paradas desde un archivo
 * de acceso aleatorio binario ({@link RandomAccessFile}). Maneja tanto tramos unidireccionales
 * (de tipo colectivo) como bidireccionales (de tipo {@link colectivo.aplicacion.Constantes#CAMINANDO}).
 * </p>
 * <p>
 * <b>Estructura del registro en el archivo:</b>
 * <ul>
 *   <li><b>Marcador de borrado</b> (char): ' ' si está activo, {@link FileUtil#DELETED} si está eliminado</li>
 *   <li><b>Código inicio</b> (String de 10 caracteres): código de la parada de inicio</li>
 *   <li><b>Código fin</b> (String de 10 caracteres): código de la parada de destino</li>
 *   <li><b>Tiempo</b> (int): tiempo de recorrido del tramo en minutos</li>
 *   <li><b>Tipo</b> (int): tipo de tramo (1 para colectivo o {@link colectivo.aplicacion.Constantes#CAMINANDO} para caminando)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Clave de indexación:</b> Los tramos se indexan mediante una clave compuesta formada por
 * "codigoInicio-codigoFin-tipo" para permitir búsqueda y actualización eficiente.
 * </p>
 * <p>
 * <b>Control de duplicados:</b> Para tramos de tipo {@link colectivo.aplicacion.Constantes#CAMINANDO}
 * (bidireccionales), verifica que no existan relaciones duplicadas antes de crear nuevos tramos,
 * ya que el constructor de {@link Tramo} establece relaciones bidireccionales automáticamente.
 * </p>
 * <p>
 * Si el archivo está vacío al momento de la construcción, se puebla automáticamente
 * desde {@link TramoSecuencialDAO}.
 * </p>
 *
 * @see TramoDAO
 * @see Tramo
 * @see Parada
 * @see RandomAccessFile
 * @see AConnection
 * @see TramoSecuencialDAO
 */
public class TramoAleatorioDAO implements TramoDAO {

    private static final Logger logger = LogManager.getLogger(TramoAleatorioDAO.class);

    private final RandomAccessFile file;
    private final Map<String, Tramo> tramos;
    private final Map<Integer, Parada> paradas;

    /** Tamaño fijo en caracteres para los campos de código de parada en el archivo. */
    private static final int SIZE_CODIGO = 10;

    /**
     * Constructor por defecto.
     * <p>
     * Inicializa el archivo de acceso aleatorio para tramos usando {@link AConnection},
     * carga las paradas disponibles desde {@link ParadaDAO} y luego:
     * <ul>
     *   <li>Si el archivo está vacío, lo puebla desde {@link TramoSecuencialDAO}</li>
     *   <li>Si el archivo tiene contenido, carga los tramos existentes</li>
     * </ul>
     * </p>
     *
     * @throws RuntimeException si ocurre un error de I/O al abrir el archivo
     * @see AConnection#getInstancia(String)
     * @see #cargarParadas()
     * @see #llenarDesdeSecuencial()
     * @see #cargarTramosDesdeArchivo()
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
     * Busca y retorna todos los tramos cargados en memoria.
     * <p>
     * Retorna una copia del mapa de tramos para evitar modificaciones externas.
     * Los tramos se mantienen ordenados por su clave compuesta (codigoInicio-codigoFin-tipo)
     * gracias a {@link TreeMap}.
     * </p>
     *
     * @return un {@link Map} con todos los tramos indexados por su clave compuesta
     * @throws Exception si ocurre un error (aunque en esta implementación no se espera)
     * @see #crearKey(int, int, int)
     */
    @Override
    public Map<String, Tramo> buscarTodos() throws Exception {
        return new TreeMap<>(tramos);
    }

    /**
     * Carga todas las paradas desde el {@link ParadaDAO}.
     * <p>
     * Obtiene la instancia de {@link ParadaDAO} desde {@link Factory} y carga todas las paradas disponibles.
     * Las paradas son necesarias para establecer las relaciones de inicio y fin en cada tramo.
     * </p>
     * <p>
     * <b>Manejo de errores:</b>
     * <ul>
     *   <li>Si {@link ParadaDAO} es null, registra una advertencia y retorna un mapa vacío</li>
     *   <li>Si el mapa cargado es null, retorna un mapa vacío</li>
     *   <li>Si ocurre un {@link RuntimeException}, lo propaga directamente</li>
     *   <li>Si ocurre cualquier otra excepción, la encapsula en {@link RuntimeException} y la propaga</li>
     * </ul>
     * </p>
     *
     * @return un {@link Map} con las paradas indexadas por su código
     * @throws RuntimeException si ocurre un error al cargar las paradas
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
            logger.error("No se pudo cargar mapa de paradas", e);
            throw new RuntimeException("No se pudo cargar mapa de paradas", e);
        }
    }

    /**
     * Carga todos los tramos desde el archivo de acceso aleatorio a memoria.
     * <p>
     * Recorre el archivo desde el inicio hasta el final, leyendo cada registro mediante
     * {@link #leerRegistro()}. Por cada tramo válido (no nulo):
     * <ol>
     *   <li>Crea una clave compuesta usando {@link #crearKey(int, int, int)}</li>
     *   <li>Agrega el tramo al mapa indexado por esa clave</li>
     * </ol>
     * </p>
     * <p>
     * <b>Manejo de errores:</b>
     * <ul>
     *   <li>Si se alcanza el final del archivo ({@link EOFException}), termina la lectura normalmente</li>
     *   <li>Si ocurre otro error de I/O, registra el error con la posición del archivo y lo propaga</li>
     * </ul>
     * </p>
     *
     * @throws IOException si ocurre un error al leer desde el archivo
     * @see #leerRegistro()
     * @see #crearKey(int, int, int)
     * @see RandomAccessFile#seek(long)
     * @see RandomAccessFile#getFilePointer()
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
                // Normal end of file alcanzado al leer un registro — detiene carga
                return;
            } catch (IOException ioe) {
                long pos;
                try {
                    pos = file.getFilePointer();
                } catch (IOException ex) {
                    pos = -1L;
                }
                logger.error("Error leyendo archivo de tramos en posición {}", pos, ioe);
                throw new IOException("Error leyendo archivo de tramos en posición " + pos, ioe);
            }
        }
    }

    /**
     * Puebla el archivo de acceso aleatorio desde {@link TramoSecuencialDAO}.
     * <p>
     * Este método se invoca cuando el archivo está vacío. Realiza los siguientes pasos:
     * <ol>
     *   <li>Crea una instancia de {@link TramoSecuencialDAO}</li>
     *   <li>Carga todos los tramos desde el DAO secuencial</li>
     *   <li>Por cada tramo:
     *     <ul>
     *       <li>Posiciona el puntero al final del archivo</li>
     *       <li>Escribe el tramo usando {@link #escribirRegistro(Tramo)}</li>
     *       <li>Crea una clave compuesta y agrega el tramo al mapa en memoria</li>
     *     </ul>
     *   </li>
     * </ol>
     * </p>
     * <p>
     * Si el DAO secuencial retorna null o un mapa vacío, el método retorna sin hacer nada.
     * </p>
     * <p>
     * <b>Manejo de errores:</b>
     * <ul>
     *   <li>Si ocurre un error de I/O, lo registra y lo encapsula en {@link RuntimeException}</li>
     *   <li>Si ocurre un {@link RuntimeException}, lo registra y lo propaga directamente</li>
     *   <li>Si ocurre cualquier otra excepción, la registra, encapsula y propaga como {@link RuntimeException}</li>
     * </ul>
     * </p>
     *
     * @throws RuntimeException si ocurre un error al poblar el archivo desde el DAO secuencial
     * @see TramoSecuencialDAO
     * @see #escribirRegistro(Tramo)
     * @see #crearKey(int, int, int)
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
     * Lee un registro completo de tramo desde la posición actual del archivo.
     * <p>
     * Lee la información del registro en el siguiente orden:
     * <ol>
     *   <li>Marcador de borrado (char)</li>
     *   <li>Código de parada de inicio ({@value #SIZE_CODIGO} caracteres)</li>
     *   <li>Código de parada de fin ({@value #SIZE_CODIGO} caracteres)</li>
     *   <li>Tiempo de recorrido en minutos (int)</li>
     *   <li>Tipo de tramo (int): 1 para colectivo o {@link colectivo.aplicacion.Constantes#CAMINANDO} para caminando</li>
     * </ol>
     * </p>
     * <p>
     * <b>Validaciones y filtros:</b>
     * <ul>
     *   <li>Si el registro está marcado como eliminado ({@link FileUtil#DELETED}), retorna null</li>
     *   <li>Si algún código está en blanco, retorna null</li>
     *   <li>Si encuentra un formato numérico inválido, registra una advertencia y retorna null</li>
     *   <li>Si alguna de las paradas no existe en el mapa, retorna null</li>
     * </ul>
     * </p>
     * <p>
     * <b>Control de duplicados para tramos CAMINANDO:</b><br>
     * Para tramos de tipo {@link colectivo.aplicacion.Constantes#CAMINANDO}, verifica si la
     * relación bidireccional ya existe entre las paradas. Si ambas paradas ya se reconocen
     * mutuamente como paradas caminando, retorna null para evitar duplicados, ya que el
     * constructor de {@link Tramo} establece relaciones bidireccionales automáticamente.
     * </p>
     *
     * @return el {@link Tramo} leído del archivo, o null si el registro está eliminado,
     *         es inválido, tiene errores de formato, o es un duplicado (para tramos CAMINANDO)
     * @throws IOException si ocurre un error al leer desde el archivo
     * @throws EOFException si se alcanza el final del archivo inesperadamente
     * @see FileUtil#readString(RandomAccessFile, int)
     * @see Tramo#Tramo(Parada, Parada, int, int)
     * @see Parada#getParadaCaminando()
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

            // Control de duplicados para tramos CAMINANDO:
            // El constructor de Tramo establece relaciones bidireccionales en las paradas
            // Por eso debemos verificar que no existan antes de crear el objeto
            if (tipo == colectivo.aplicacion.Constantes.CAMINANDO) {
                if (inicio.getParadaCaminando().contains(fin) && fin.getParadaCaminando().contains(inicio)) {
                    // La relación bidireccional ya existe, no crear el tramo para evitar duplicados
                    logger.debug("Tramo caminando entre {} y {} ya existe, evitando duplicado", codigoIni, codigoFin);
                    return null;
                }
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
                logger.warn("Código numérico inválido en registro en posición {}", pos, nfe);
            } else {
                logger.warn("Código numérico inválido en registro", nfe);
            }
            return null;
        }
    }

    /**
     * Escribe un registro completo de tramo en la posición actual del archivo.
     * <p>
     * Escribe la información del registro en el siguiente orden:
     * <ol>
     *   <li>Marcador activo (char: ' ')</li>
     *   <li>Código de parada de inicio, ajustado a {@value #SIZE_CODIGO} caracteres</li>
     *   <li>Código de parada de fin, ajustado a {@value #SIZE_CODIGO} caracteres</li>
     *   <li>Tiempo de recorrido en minutos (int)</li>
     *   <li>Tipo de tramo (int)</li>
     * </ol>
     * </p>
     * <p>
     * Los códigos de parada se convierten a String y se ajustan al tamaño fijo
     * usando {@link #rellenarORecortar(String, int)}.
     * </p>
     *
     * @param tramo el tramo a escribir en el archivo
     * @throws IllegalArgumentException si el tramo es null
     * @throws IOException si ocurre un error al escribir en el archivo
     * @see FileUtil#writeString(RandomAccessFile, String, int)
     * @see #rellenarORecortar(String, int)
     * @see Tramo#getInicio()
     * @see Tramo#getFin()
     * @see Tramo#getTiempo()
     * @see Tramo#getTipo()
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
     * Crea una clave compuesta para indexar tramos.
     * <p>
     * La clave se forma concatenando el código de parada de inicio, el código de parada
     * de fin y el tipo de tramo, separados por guiones.
     * </p>
     * <p>
     * <b>Formato:</b> "codigoInicio-codigoFin-tipo"
     * </p>
     * <p>
     * <b>Ejemplo:</b> Para un tramo desde la parada 100 hasta la parada 200 de tipo colectivo (1),
     * la clave sería "100-200-1"
     * </p>
     *
     * @param codigoIni el código de la parada de inicio del tramo
     * @param codigoFin el código de la parada de fin del tramo
     * @param tipo el tipo de tramo (1 para colectivo o {@link colectivo.aplicacion.Constantes#CAMINANDO} para caminando)
     * @return la clave compuesta como String
     */
    private static String crearKey(int codigoIni, int codigoFin, int tipo) {
        return codigoIni + "-" + codigoFin + "-" + tipo;
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

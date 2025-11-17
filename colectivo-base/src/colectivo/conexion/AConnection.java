package colectivo.conexion;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.util.FileUtil;

/**
 * Genera una unica instancia de conexion para un archivo aleatorio.
 *
 */
public class AConnection {

    private static final Logger logger = LogManager.getLogger(AConnection.class);
    private static Hashtable<String, RandomAccessFile> files = new Hashtable<String, RandomAccessFile>();

    /**
     * Obtiene la instancia unica de conexion.
     * 
     * @param name
     * @return file
     */
    public static RandomAccessFile getInstancia(String name) {
        try {
            RandomAccessFile file = files.get(name);
            if (file == null) {
                ResourceBundle rb = ResourceBundle.getBundle("aleatorio");
                String fileName = rb.getString(name);
                file = new RandomAccessFile(fileName, "rw");
                files.put(name, file);
                logger.debug("Conexion al archivo: " + name + " -> " + fileName);
            }
            return file;
        } catch (IOException ex) {
            logger.error("Error al crear la conexion para: " + name, ex);
            throw new RuntimeException("Error al crear la conexion", ex);
        }
    }

    /**
     * Metodo a invocar antes de finalizar JVM
     * 
     */
    static class MiShDwnHook extends Thread {
        public void run() {
            try {
                for (RandomAccessFile file : files.values()) {
                    if (file != null) {
                        file.close();
                    }
                }
            } catch (Exception ex) {
                logger.error("Error al cerrar archivos en shutdown hook", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Copia de seguridad 
     * 
     * @param name del archivo a hacer backup
     */
    public static void backup(String name) {
        ResourceBundle rb = ResourceBundle.getBundle("file");
        String fileName = rb.getString(name);
        try {
            FileUtil.copyFile(fileName, fileName + ".bak");
        } catch (IOException e) {
            logger.error("Error al crear backup para: " + name, e);
        }
    }

    /**
     * Cierra la conexion.
     * 
     * @param name
     */
    private static void close(String name) {
        RandomAccessFile file = files.get(name);
        try {
            if (file != null) {
                file.close();
                files.remove(name);
            } else {
                logger.debug("Intento de cerrar conexion inexistente: " + name);
            }
        } catch (IOException e) {
            logger.error("Error al cerrar archivo: " + name, e);
        }
    }

    /**
     * Elimina un archivo
     * 
     * @param name nombre del archivo a eliminar
     */
    public static void delete(String name) {
        ResourceBundle rb = ResourceBundle.getBundle("file");
        String fileName = rb.getString(name);
        try {
            close(name);
            Files.delete(Paths.get(fileName));
        } catch (IOException e) {
            logger.error("Error al eliminar archivo: " + fileName, e);
        }
    }
}

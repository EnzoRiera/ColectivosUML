package colectivo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Clase de utilidad para operaciones de lectura y escritura en archivos,
 * especialmente archivos de acceso aleatorio.
 * Proporciona métodos para leer/escribir cadenas, fechas y copiar archivos.
 */
public class FileUtil {

	/** Carácter que marca un registro como eliminado. */
	public static final char DELETED = '*';


	/**
	 * Lee una cadena de longitud fija desde un archivo de acceso aleatorio.
	 * Elimina caracteres nulos y espacios en blanco al final.
	 *
	 * @param file archivo de acceso aleatorio desde donde leer
	 * @param length longitud de la cadena en caracteres
	 * @return cadena leída y limpiada de espacios
	 * @throws IOException si ocurre un error de lectura
	 */
	public static String readString(RandomAccessFile file, int length) throws IOException {
		char[] s = new char[length];
		for (int i = 0; i < s.length; i++)
			s[i] = file.readChar();
		return new String(s).replace('\0', ' ').trim();
	}

	/**
	 * Escribe una cadena de longitud fija en un archivo de acceso aleatorio.
	 * Si la cadena es más corta que la longitud especificada, se rellena con espacios.
	 *
	 * @param file archivo de acceso aleatorio donde escribir
	 * @param s cadena a escribir (puede ser null)
	 * @param length longitud fija de la cadena en caracteres
	 * @throws IOException si ocurre un error de escritura
	 */
	public static void writeString(RandomAccessFile file, String s, int length) throws IOException {
		StringBuffer buffer = null;
		if (s != null)
			buffer = new StringBuffer(s);
		else
			buffer = new StringBuffer(length);
		buffer.setLength(length);
		file.writeChars(buffer.toString());
	}


	public static void copyFile(String nameSource, String nameDest) throws IOException {
		File source = new File(nameSource);
		File dest = new File(nameDest);
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}
}

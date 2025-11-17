package colectivo.aplicacion;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.controlador.Coordinador;

/**
 * Clase Singleton que gestiona la configuración de la aplicación.
 * <p>
 * Esta clase centraliza la carga y gestión de configuraciones desde archivos properties,
 * incluyendo la internacionalización (i18n), estilos visuales y rutas de vistas.
 * Implementa el patrón Singleton para garantizar una única instancia de configuración.
 * </p>
 * <p>
 * <b>Configuraciones gestionadas:</b>
 * <ul>
 *   <li><b>Internacionalización:</b> Carga de {@link ResourceBundle} para múltiples idiomas</li>
 *   <li><b>Vistas:</b> Rutas a archivos FXML de la interfaz gráfica</li>
 *   <li><b>Estilos:</b> Archivos CSS para temas claro y oscuro</li>
 *   <li><b>Locale:</b> Configuración de idioma y país</li>
 * </ul>
 * </p>
 * <p>
 * <b>Estrategia de carga del archivo config.properties:</b>
 * <ol>
 *   <li><b>Prioridad principal:</b> Desde el classpath (Maven resources)</li>
 *   <li><b>Fallback:</b> Variable de entorno {@code COLECTIVO_CONFIG} para configuraciones externas</li>
 * </ol>
 * Si no se encuentra el archivo en ninguna ubicación, se lanza {@link RuntimeException}.
 * </p>
 * <p>
 * <b>Propiedades esperadas en config.properties:</b>
 * <ul>
 *   <li>{@code vista} - Archivo FXML de la vista principal (default: "view.fxml")</li>
 *   <li>{@code estiloOscuro} - Archivo CSS para tema oscuro (default: "modoOscuro.css")</li>
 *   <li>{@code estiloClaro} - Archivo CSS para tema claro (default: "modoClaro.css")</li>
 *   <li>{@code language} - Código de idioma ISO 639 (default: "en")</li>
 *   <li>{@code country} - Código de país ISO 3166 (default: "US")</li>
 *   <li>{@code labels} - Nombre base del ResourceBundle (default: "labels")</li>
 * </ul>
 * </p>
 *
 * @see ResourceBundle
 * @see Locale
 * @see Properties
 * @see Coordinador
 */
public class Configuracion {
	
    private static final Logger logger = LogManager.getLogger(Configuracion.class);
	
	/** Ruta del archivo CSS para el estilo oscuro de la interfaz. */
	private static String archivoEstiloOscuro;

	/** Ruta del archivo CSS para el estilo claro de la interfaz. */
	private static String archivoEstiloClaro;

	/** Ruta del archivo FXML de la vista principal de la aplicación. */
	private static String archivoVista;

	/** Instancia única de Configuracion (patrón Singleton). */
	private static Configuracion configuracion = null;

	/** Referencia al coordinador central de la aplicación. */
	private Coordinador coordinador;

	/** ResourceBundle para la internacionalización de mensajes y etiquetas. */
	private ResourceBundle resourceBundle;

	/** Nombre base del ResourceBundle (sin locale ni extensión). */
	private String labelsBaseName;

	/**
	 * Constructor privado para implementar el patrón Singleton.
	 * <p>
	 * Este constructor realiza las siguientes operaciones:
	 * <ol>
	 *   <li>Intenta cargar {@code config.properties} desde el classpath</li>
	 *   <li>Si falla, intenta cargarlo desde la variable de entorno {@code COLECTIVO_CONFIG}</li>
	 *   <li>Si no se encuentra, lanza {@link RuntimeException}</li>
	 *   <li>Carga las propiedades de configuración con valores por defecto</li>
	 *   <li>Inicializa el idioma según la configuración cargada</li>
	 * </ol>
	 * </p>
	 * <p>
	 * <b>Validaciones:</b> Todas las propiedades de texto se normalizan con {@code strip()}
	 * y se validan para no estar en blanco, usando valores por defecto si es necesario.
	 * </p>
	 *
	 * @throws RuntimeException si no se puede encontrar o cargar el archivo config.properties
	 * @see #cargarIdioma(String, String)
	 * @see Properties#load(InputStream)
	 */
	private Configuracion() {

		Properties prop = new Properties();
		InputStream input;

		// 1) Intentar cargar desde el classpath (Maven resources) - PRIORIDAD PRINCIPAL
		input = getClass().getClassLoader().getResourceAsStream("config.properties");
		
		// 2) Variable de entorno COLECTIVO_CONFIG (fallback para configuraciones externas)
		if (input == null) {
			String envPath = System.getenv("COLECTIVO_CONFIG");
			if (envPath != null) {
				try {
					input = new FileInputStream(envPath);
					logger.debug("Cargando configuración desde variable de entorno: {}", envPath);
				} catch (FileNotFoundException e) {
                    logger.warn("Warning: COLECTIVO_CONFIG points to non-existent file: {}", envPath);
				}
			}
		}

		// Cargar las propiedades
		if (input != null) {
			try {
				prop.load(input);
				input.close();
			} catch (IOException e) {
			    logger.error("Failed to load configuration file 'config.properties'.", e);
			    throw new RuntimeException("Cannot initialize application without config.properties", e);
			}
        }
        else {
            logger.error("Configuration file 'config.properties' not found in classpath or environment variable.");
            throw new RuntimeException("Cannot initialize application without config.properties");
        }

		archivoVista = prop.getProperty("vista", "view.fxml").strip();
		archivoEstiloOscuro = prop.getProperty("estiloOscuro", "modoOscuro.css").strip();
        archivoEstiloClaro = prop.getProperty("estiloClaro", "modoClaro.css").strip();

        // Obtener propiedades con valores por defecto
        String lang = prop.getProperty("language", "en");
        String country = prop.getProperty("country", "US");

        // Guardar el nombre base en el campo de la clase
        this.labelsBaseName = prop.getProperty("labels", "labels");

        lang = (lang != null && !lang.isBlank()) ? lang.strip() : "en";
        country = (country != null && !country.isBlank()) ? country.strip() : "US";
        this.labelsBaseName = (this.labelsBaseName != null && !this.labelsBaseName.isBlank())
                ? this.labelsBaseName.strip() : "labels";

        cargarIdioma(lang, country);
	}

	/**
	 * Obtiene la instancia única de Configuracion (patrón Singleton).
	 * <p>
	 * Si la instancia no existe, la crea invocando el constructor privado.
	 * Este método implementa lazy initialization (inicialización perezosa).
	 * </p>
	 * <p>
	 * <b>Nota:</b> Este método no es thread-safe. En aplicaciones multi-hilo se debe
	 * sincronizar externamente si hay posibilidad de acceso concurrente durante la
	 * primera inicialización.
	 * </p>
	 *
	 * @return la instancia única de {@link Configuracion}
	 * @throws RuntimeException si ocurre un error durante la construcción de la instancia
	 * @see #Configuracion()
	 */
	public static Configuracion getConfiguracion() {
        if (configuracion == null) {
            configuracion = new Configuracion();
        }
        return configuracion;
    }

	/**
	 * Carga el ResourceBundle para el idioma y país especificados.
	 * <p>
	 * Este método privado es invocado tanto desde el constructor como desde
	 * {@link #cambiarIdioma(String, String)}. Realiza las siguientes operaciones:
	 * </p>
	 * <ol>
	 *   <li>Construye un {@link Locale} usando el idioma y país proporcionados</li>
	 *   <li>Intenta cargar el ResourceBundle correspondiente desde el classpath</li>
	 *   <li>Si no encuentra una versión exacta, Java realiza fallback a versiones más generales</li>
	 *   <li>Registra en el log el resultado de la carga</li>
	 * </ol>
	 * <p>
	 * <b>Estrategia de fallback de ResourceBundle:</b><br>
	 * Para locale {@code es_ES}, busca en orden:
	 * <ol>
	 *   <li>{@code labels_es_ES.properties}</li>
	 *   <li>{@code labels_es.properties}</li>
	 *   <li>{@code labels.properties} (bundle por defecto)</li>
	 * </ol>
	 * </p>
	 *
	 * @param lang código de idioma ISO 639 (ej: "es", "en")
	 * @param country código de país ISO 3166 (ej: "ES", "US")
	 * @throws RuntimeException si no se puede cargar ningún ResourceBundle (ni siquiera el default)
	 * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
	 * @see Locale.Builder
	 */
	private void cargarIdioma(String lang, String country) {
        Locale locale = new Locale.Builder().setLanguage(lang).setRegion(country).build();

        try {
            this.resourceBundle = ResourceBundle.getBundle(this.labelsBaseName, locale,
                    Configuracion.class.getClassLoader());

            logger.debug("ResourceBundle cargado (o con fallback) exitosamente para locale: {}", locale);

        } catch (Exception e) {
            logger.error("No se pudo cargar el ResourceBundle base '{}'.", this.labelsBaseName, e);
            throw new RuntimeException("Fallo crítico al cargar ResourceBundle", e);
        }
    }

	/**
	 * Cambia el idioma de la aplicación de forma dinámica.
	 * <p>
	 * Este método permite cambiar el idioma en tiempo de ejecución recargando
	 * el {@link ResourceBundle} con el nuevo locale. Es útil para aplicaciones
	 * que permiten al usuario cambiar el idioma sin reiniciar.
	 * </p>
	 * <p>
	 * <b>Nota:</b> Después de llamar a este método, los componentes de la interfaz
	 * de usuario deben actualizarse manualmente para reflejar el nuevo idioma,
	 * ya que este método solo cambia el ResourceBundle subyacente.
	 * </p>
	 *
	 * @param lang código de idioma ISO 639 (ej: "es", "en")
	 * @param country código de país ISO 3166 (ej: "ES", "US")
	 * @throws RuntimeException si no se puede cargar el ResourceBundle para el nuevo idioma
	 * @see #cargarIdioma(String, String)
	 * @see Coordinador#pedirCambioIdioma(String, String)
	 */
	public void cambiarIdioma(String lang, String country) {
        cargarIdioma(lang, country);
    }

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setCoordinador(Coordinador coordinador) {
		this.coordinador = coordinador;
	}

	public String getArchivoVista() {
		return archivoVista;
	}

	public String getArchivoEstiloOscuro() {
		return archivoEstiloOscuro;
	}

	public String getArchivoEstiloClaro() {
        return archivoEstiloClaro;
    }

}

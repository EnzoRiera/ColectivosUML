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


public class Configuracion {
	
    private static final Logger logger = LogManager.getLogger(Configuracion.class);
	
	private static String archivoEstilo;
	
	private static String archivoVista;
	private static Configuracion configuracion = null;
	
	public static Configuracion getConfiguracion() {
		if (configuracion == null) {
			configuracion = new Configuracion();
		}
		return configuracion;
	}

	private Coordinador coordinador;	

	private ResourceBundle resourceBundle;

	private Configuracion() {

	    Properties prop = new Properties();
	    

	    try (InputStream input = new FileInputStream("config.properties")) {
	        prop.load(input);
	        
	        archivoVista = prop.getProperty("vista", "view.fxml").strip();
	        archivoEstilo = prop.getProperty("estilo", "style.css").strip();
	        
            String lang = prop.getProperty("language");
            String country = prop.getProperty("country");
            String labels = prop.getProperty("labels");

            // Trim to remove leading/trailing spaces and provide defaults
            lang = (lang != null) ? lang.strip() : "en";
            country = (country != null) ? country.strip() : "US";
            labels = (labels != null) ? labels.strip() : "labels";
	        
	        Locale locale = new Locale.Builder()
	                .setLanguage(lang)
	                .setRegion(country)
	                .build();

            Locale.setDefault(locale);
            resourceBundle = ResourceBundle.getBundle(labels, locale);
            
	    } catch (FileNotFoundException e) {
	        logger.error("Error: Configuration file `config.properties` not found. " + e.getMessage(), e);
	    } catch (IOException e) {
	        logger.error("Error: Failed to load configuration file `config.properties`. " + e.getMessage(), e);
	    }
	}


	public String getArchivoEstilo() {
		return archivoEstilo;
	}

	public String getArchivoVista() {
		return archivoVista;
	}
	
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	public void setCoordinador(Coordinador coordinador) {
		this.coordinador = coordinador;
	}
	
}

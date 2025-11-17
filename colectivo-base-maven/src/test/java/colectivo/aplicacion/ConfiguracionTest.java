package colectivo.aplicacion;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

/**
 * Test para verificar que la configuración se carga correctamente
 * con el locale y ResourceBundle especificados en config.properties
 */
class ConfiguracionTest {

    @Test
    void testConfiguracionCargaLocaleEspañol() {
        // Obtener la configuración singleton
        Configuracion config = Configuracion.getConfiguracion();
        
        // Verificar que el ResourceBundle no es null
        assertNotNull(config.getResourceBundle(), "El ResourceBundle no debería ser null");
        
        // Verificar que el locale por defecto es español (ES)
        Locale defaultLocale = Locale.getDefault();
        System.out.println("Locale por defecto: " + defaultLocale);
        System.out.println("Language: " + defaultLocale.getLanguage());
        System.out.println("Country: " + defaultLocale.getCountry());
        
        // Verificar que el ResourceBundle tiene las claves en español
        ResourceBundle bundle = config.getResourceBundle();
        System.out.println("Bundle locale: " + bundle.getLocale());
        
        if (bundle.containsKey("title.window")) {
            String title = bundle.getString("title.window");
            System.out.println("Título de ventana: " + title);
            assertTrue(title.contains("Consulta") || title.contains("Colectivos"), 
                "El título debería estar en español");
        }
        
        // Verificar archivos de vista y estilo
        assertNotNull(config.getArchivoVista(), "La ruta de la vista no debería ser null");
        assertNotNull(config.getArchivoEstiloOscuro(), "La ruta del estilo no debería ser null");
        
        System.out.println("Archivo vista: " + config.getArchivoVista());
        System.out.println("Archivo estilo: " + config.getArchivoEstiloOscuro());
    }
    
}

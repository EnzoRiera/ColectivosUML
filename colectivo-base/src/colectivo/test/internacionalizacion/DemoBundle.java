package colectivo.test.internacionalizacion;
import java.util.ResourceBundle;

import colectivo.aplicacion.Configuracion;

public class DemoBundle {
    public static void main(String[] args) {
        // Use the bundle already loaded by Configuracion (which sets Locale from config.properties)
        ResourceBundle rb = Configuracion.getConfiguracion().getResourceBundle();

        System.out.println("Locale used: " + rb.getLocale());
        System.out.println("example.key = " + rb.getString("example.key"));
    }
}
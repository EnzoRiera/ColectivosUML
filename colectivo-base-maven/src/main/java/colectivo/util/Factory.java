package colectivo.util;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fábrica simple y thread-safe que crea y cachea instancias por nombre lógico.
 *
 * <p>Los mapeos de nombres lógicos a nombres de clases de implementación se cargan
 * desde el archivo de recursos {@code "factory"} (archivo {@code factory.properties}
 * en el classpath). Las instancias se crean mediante reflexión utilizando el constructor
 * sin argumentos y se cachean en un mapa concurrente para que cada nombre produzca
 * una única instancia compartida (patrón Singleton por nombre).</p>
 *
 * <p>Thread-safety: esta clase es thread-safe. La creación de instancias utiliza
 * {@link Map#computeIfAbsent} sobre un {@link ConcurrentHashMap} para garantizar
 * que se cree una única instancia por nombre incluso bajo acceso concurrente.</p>
 *
 * @see ResourceBundle
 */
public final class Factory {
    
    private static final Logger logger = LogManager.getLogger(Factory.class);

    /**
     * Caché de instancias creadas indexadas por nombre lógico.
     */
    private static final Map<String, Object> INSTANCIAS = new ConcurrentHashMap<>();

    private Factory() { }

    /**
     * Retorna la instancia compartida asociada con el nombre lógico dado.
     * Si aún no existe una instancia, se crea leyendo el nombre de la clase desde
     * el ResourceBundle {@code factory} e instanciándola mediante reflexión.
     *
     * @param name nombre lógico definido en el ResourceBundle {@code factory}
     * @return la instancia compartida para {@code name}
     * @throws RuntimeException si falta la entrada en el ResourceBundle, la clase
     *                          no puede ser cargada, o la instancia no puede ser creada
     */
    public static Object getInstancia(String name) {
        Object obj = INSTANCIAS.computeIfAbsent(name, Factory::crearInstancia);
        if (obj != null) {
            logger.debug("Instancia lista para '" + name + "': " + obj.getClass().getName());
        } else {
            logger.debug("Instancia nula para '" + name + "'");
        }
        return obj;
    }
    
    /**
     * Retorna la instancia compartida asociada con el nombre lógico dado, casteada al tipo solicitado.
     *
     * @param <T>  tipo esperado de la instancia
     * @param name nombre lógico definido en el ResourceBundle {@code factory}
     * @param type objeto de clase que representa el tipo esperado
     * @return la instancia compartida para {@code name} casteada a {@code T}
     * @throws ClassCastException si la instancia cacheada no es asignable a {@code type}
     * @throws RuntimeException   si la creación de la instancia falla (ver {@link #getInstancia(String)})
     */
    public static <T> T getInstancia(String name, Class<T> type) {
        Object obj = getInstancia(name);
        return type.cast(obj);
    }

    /**
     * Crea una nueva instancia para el nombre lógico dado leyendo el nombre de clase
     * desde el ResourceBundle {@code factory} y creando un nuevo objeto usando su
     * constructor sin argumentos.
     *
     * <p>Todas las excepciones verificadas se envuelven en {@link RuntimeException}
     * para que los llamadores de la API pública no necesiten manejar excepciones
     * específicas de reflexión.</p>
     *
     * @param name nombre lógico definido en el ResourceBundle {@code factory}
     * @return instancia recién creada
     * @throws RuntimeException ante cualquier error (bundle/clave faltante, clase no encontrada,
     *                          constructor no accesible, fallo de instanciación, etc.)
     */
    private static Object crearInstancia(String name) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("factory");
            String className = rb.getString(name);
            logger.debug("Creando instancia para '" + name + "' -> " + className);
            Object instance = Class.forName(className).getDeclaredConstructor().newInstance();
            logger.debug("Instancia creada para '" + name + "': " + instance.getClass().getName());
            return instance;
        } catch (Exception e) {
            logger.error("Error al crear la instancia para '" + name + "'", e);
            throw new RuntimeException("Failed to create instance for: " + name, e);
        }
    }
}

// java
package colectivo.util;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Simple thread-safe factory that creates and caches instances by logical name.
 *
 * <p>Mappings from logical names to implementation class names are loaded from the
 * resource bundle named {@code "factory"} (i.e. a file named {@code factory.properties}
 * on the classpath). Instances are created reflectively using the no-argument
 * constructor and cached in a concurrent map so that each name yields a single
 * shared instance.</p>
 *
 * <p>Usage example:
 * <pre>
 * Object obj = Factory.getInstance("myService");
 * MyService svc = Factory.getInstance("myService", MyService.class);
 * </pre>
 * </p>
 *
 * Thread-safety: this class is thread-safe. Instance creation uses
 * {@link Map#computeIfAbsent} on a {@link ConcurrentHashMap} to ensure a single
 * instance is created per name even under concurrent access.
 *
 * @see ResourceBundle
 */
public final class Factory {
    
    private static final Logger logger = LogManager.getLogger(Factory.class);
    
    /**
     * Cache of created instances keyed by logical name.
     */
    private static final Map<String, Object> INSTANCIAS = new ConcurrentHashMap<>();


    /**
     * Private constructor for utility class.
     */
    private Factory() { /* utility class */ }

    /**
     * Returns the shared instance associated with the given logical name.
     * If no instance exists yet, it is created by reading the class name from the
     * {@code factory} resource bundle and instantiating it reflectively.
     *
     * @param name logical name defined in the {@code factory} resource bundle
     * @return the shared instance for {@code name}
     * @throws RuntimeException if the resource bundle entry is missing, the class
     *                          cannot be loaded, or the instance cannot be created
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
     * Returns the shared instance associated with the given logical name, cast to
     * the requested type.
     *
     * @param <T>  expected type of the instance
     * @param name logical name defined in the {@code factory} resource bundle
     * @param type class object representing the expected type
     * @return the shared instance for {@code name} cast to {@code T}
     * @throws ClassCastException if the cached instance is not assignable to {@code type}
     * @throws RuntimeException   if instance creation fails (see {@link #getInstancia(String)})
     */
    public static <T> T getInstancia(String name, Class<T> type) {
        Object obj = getInstancia(name);
        return type.cast(obj);
    }

    /**
     * Creates a new instance for the given logical name by reading the class name
     * from the {@code factory} resource bundle and creating a new object using its
     * no-argument constructor.
     *
     * <p>All checked exceptions are wrapped into a {@link RuntimeException} so callers
     * of public API do not need to handle reflection-specific exceptions.</p>
     *
     * @param name logical name defined in the {@code factory} resource bundle
     * @return newly created instance
     * @throws RuntimeException on any error (missing bundle/key, class not found,
     *                          no accessible no-arg constructor, instantiation failure, etc.)
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

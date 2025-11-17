package colectivo.test.interfaz;

import colectivo.util.Factory;

/**
 * Small application to verify that the {@link Factory} can create an instance
 * for the key {@code "INTERFAZ"}.
 *
 * <p>
 * This test application obtains an object from the {@link Factory} and prints
 * the concrete class name to standard output. Any exception thrown during
 * creation is caught and its stack trace is printed to help diagnose factory
 * configuration or runtime errors.
 * </p>
 *
 * @since 1.0
 * @see colectivo.util.Factory
 */
public class AplicacionPruebaInterfaz {
    /**
     * Entry point for the test application.
     *
     * <p>
     * The method calls {@code Factory.getInstance("INTERFAZ")} and prints the
     * resulting object's runtime class name. Exceptions are caught and their
     * stack traces are printed to aid debugging.
     * </p>
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
	    try {
	        Object obj = Factory.getInstancia("INTERFAZ");
	        System.out.println("Instancia creada: " + obj.getClass().getName());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}

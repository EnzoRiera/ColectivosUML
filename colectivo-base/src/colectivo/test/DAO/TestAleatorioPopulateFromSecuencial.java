package colectivo.test.DAO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import colectivo.dao.aleatorio.LineaAleatorioDAO;
import colectivo.dao.aleatorio.ParadaAleatorioDAO;
import colectivo.dao.aleatorio.TramoAleatorioDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Test que verifica que los DAOs aleatorios se crean y se poblán desde los DAOs
 * secuenciales cuando los archivos aleatorios no existen o están vacíos.
 *
 * Este test "hardcodea" expectativas que dependen de los archivos fuente del
 * curso (los mismos usados por las pruebas secuenciales). Comprueba sólo un
 * subconjunto representativo (parada con código 1, linea "L1I" y un tramo 88-97).
 */
public class TestAleatorioPopulateFromSecuencial {

    private static final Path LINEA_PATH = Paths.get("linea.dat");
    private static final Path PARADA_PATH = Paths.get("parada.dat");
    private static final Path TRAMO_PATH = Paths.get("tramo.dat");

    @BeforeEach
    public void setUp() throws IOException {
        // Asegurar que no existen los archivos aleatorios para forzar la población
        try {
            Files.deleteIfExists(LINEA_PATH);
        } catch (IOException e) {
            // ignore
        }
        try {
            Files.deleteIfExists(PARADA_PATH);
        } catch (IOException e) {
            // ignore
        }
        try {
            Files.deleteIfExists(TRAMO_PATH);
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    public void testAleatorioSePueblaDesdeSecuencial_ParadaYLineaYTramo() throws Exception {
        // Crear instancias de los DAOs aleatorios; los constructores deberían
        // poblar los .dat desde los secuenciales si los archivos no existen
        ParadaAleatorioDAO paradaDAO = new ParadaAleatorioDAO();
        LineaAleatorioDAO lineaDAO = new LineaAleatorioDAO();
        TramoAleatorioDAO tramoDAO = new TramoAleatorioDAO();

        // Verificar que las colecciones cargadas no son nulas ni vacías
        Map<Integer, Parada> paradas = paradaDAO.buscarTodos();
        Map<String, Linea> lineas = lineaDAO.buscarTodos();
        Map<String, Tramo> tramos = tramoDAO.buscarTodos();

        assertNotNull(paradas, "El mapa de paradas no debe ser nulo tras poblar");
        assertFalse(paradas.isEmpty(), "El mapa de paradas no debe estar vacío tras poblar");

        assertNotNull(lineas, "El mapa de líneas no debe ser nulo tras poblar");
        assertFalse(lineas.isEmpty(), "El mapa de líneas no debe estar vacío tras poblar");

        assertNotNull(tramos, "El mapa de tramos no debe ser nulo tras poblar");
        assertFalse(tramos.isEmpty(), "El mapa de tramos no debe estar vacío tras poblar");

        // Comprobaciones hardcodeadas (conocidas en los datos de ejemplo)
        // Parada con código 1 debe existir
        assertTrue(paradas.containsKey(1), "Debe existir la parada con código 1 tras poblar");

        // Línea L1I debe existir
        assertTrue(lineas.containsKey("L1I"), "Debe existir la línea L1I tras poblar");

        // Tramo 88-97 tipo 1 (colectivo) debe existir
        boolean found = false;
        for (String key : tramos.keySet()) {
            if (key.startsWith("88-97-")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Debe existir el tramo 88-97 tras poblar");
    }
}

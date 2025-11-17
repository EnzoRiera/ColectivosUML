package colectivo.test.cargadatosincremento1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import colectivo.aplicacion.Constantes;
import colectivo.datos.CargarDatos;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Unit tests for data loading utilities in {@link CargarDatos}.
 *
 * <p>
 * Uses the sample data files provided by the course to validate:
 * <ul>
 * <li>Correct loading of paradas (stops)</li>
 * <li>Correct loading of tramos (segments)</li>
 * <li>Correct loading of lineas (routes) and frecuencias</li>
 * <li>Handling of missing files and boundary cases</li>
 * <li>Bidirectional relationships between paradas, tramos and lineas</li>
 * </ul>
 * </p>
 *
 * <p>
 * Tests assume the presence of the course-provided files referenced by the
 * constants at the top of the class. Tests that expect exceptions verify that
 * invalid or missing input is handled appropriately by the loader methods.
 * </p>
 */
public class TestCargarDatos {

	// Rutas dinámicas que funcionan desde diferentes contextos de ejecución
	private static final String BASE_PATH = getBasePath();
	private static final String ARCHIVO_FRECUENCIAS = BASE_PATH + "frecuencia_PM.txt";
	private static final String ARCHIVO_LINEAS = BASE_PATH + "linea_PM.txt";
	private static final String ARCHIVO_PARADAS = BASE_PATH + "parada_PM.txt";
	private static final String ARCHIVO_TRAMOS = BASE_PATH + "tramo_PM.txt";

	private Map<String, Linea> lineas;
	private Map<Integer, Parada> paradas;
	private Map<String, Tramo> tramos;

	/**
	 * Determines the correct base path for data files.
	 * Tries multiple locations to work from different execution contexts (IDE, Maven, etc.).
	 * 
	 * @return the base path with trailing separator, or empty string if not found
	 */
	private static String getBasePath() {
		// Try common locations
		String[] possiblePaths = {
			"", // Current directory (when running from project root)
			"./", // Current directory explicit
			"../", // One level up (when running from /bin or /target)
			"../../", // Two levels up
			"colectivo-base/", // When running from workspace parent
			"../colectivo-base/", // Variant
			"../../colectivo-base/", // Another variant
		};
		
		for (String path : possiblePaths) {
			java.io.File testFile = new java.io.File(path + "parada_PM.txt");
			if (testFile.exists()) {
				System.out.println("✓ Archivos de datos encontrados en: " + testFile.getAbsolutePath());
				return path;
			}
		}
		
		// If not found, print diagnostic information
		System.err.println("⚠ ADVERTENCIA: No se encontraron los archivos de datos.");
		System.err.println("Working directory: " + new java.io.File(".").getAbsolutePath());
		System.err.println("Se intentaron las siguientes ubicaciones:");
		for (String path : possiblePaths) {
			System.err.println("  - " + new java.io.File(path + "parada_PM.txt").getAbsolutePath());
		}
		
		return "";
	}

	/**
	 * Test setup executed before each test method.
	 *
	 * <p>
	 * Initializes the maps to {@code null} to ensure each test performs its own
	 * loading and assertions starting from a clean state.
	 * </p>
	 */
	@BeforeEach
	public void setUp() {
		paradas = null;
		tramos = null;
		lineas = null;
	}

	/**
	 * Test de diagnóstico para verificar que los archivos se encuentran correctamente.
	 */
	@Test
	public void testDiagnostico_ArchivosEncontrados() {
		System.out.println("\n=== TEST DIAGNÓSTICO ===");
		System.out.println("Working Directory: " + System.getProperty("user.dir"));
		System.out.println("BASE_PATH: '" + BASE_PATH + "'");
		System.out.println("ARCHIVO_PARADAS: '" + ARCHIVO_PARADAS + "'");
		
		java.io.File f = new java.io.File(ARCHIVO_PARADAS);
		System.out.println("Archivo existe: " + f.exists());
		System.out.println("Ruta absoluta: " + f.getAbsolutePath());
		System.out.println("Puede leer: " + f.canRead());
		
		assertTrue(f.exists(), "El archivo de paradas debe existir en: " + f.getAbsolutePath());
		System.out.println("=== FIN TEST DIAGNÓSTICO ===\n");
	}

	/**
	 * Integration test that verifies full loading and referential integrity among
	 * paradas, tramos and lineas when all files are valid.
	 *
	 * <p>
	 * Condition: All input files valid. Expected: all collections loaded and each
	 * parada referenced by a line exists in the paradas map.
	 * </p>
	 *
	 * @throws IOException if reading the test files fails
	 */
	@Test
	public void testCargaCompleta_TodosLosArchivos_IntegracionExitosa() throws IOException {
		// Diagnóstico
		System.out.println("\n=== DIAGNÓSTICO DE CARGA ===");
		System.out.println("BASE_PATH: " + BASE_PATH);
		System.out.println("ARCHIVO_PARADAS: " + ARCHIVO_PARADAS);
		System.out.println("Archivo existe: " + new java.io.File(ARCHIVO_PARADAS).exists());
		System.out.println("Ruta absoluta: " + new java.io.File(ARCHIVO_PARADAS).getAbsolutePath());
		
		// Act
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);
		System.out.println("Paradas cargadas: " + paradas.size());
		
		tramos = CargarDatos.cargarTramos(ARCHIVO_TRAMOS, paradas);
		System.out.println("Tramos cargados: " + tramos.size());
		
		lineas = CargarDatos.cargarLineas(ARCHIVO_LINEAS, ARCHIVO_FRECUENCIAS, paradas);
		System.out.println("Líneas cargadas: " + lineas.size());
		System.out.println("=== FIN DIAGNÓSTICO ===\n");

		// Assert
		assertNotNull(paradas, "Las paradas deben cargarse");
		assertNotNull(tramos, "Los tramos deben cargarse");
		assertNotNull(lineas, "Las líneas deben cargarse");

		assertFalse(paradas.isEmpty(), "Debe haber paradas cargadas");
		assertFalse(tramos.isEmpty(), "Debe haber tramos cargados");
		assertFalse(lineas.isEmpty(), "Debe haber líneas cargadas");

		// Verificar integridad referencial: las paradas de las líneas existen en el
		// mapa
		for (Linea linea : lineas.values()) {
			for (Parada parada : linea.getParadas()) {
				assertTrue(paradas.containsValue(parada),
						"Cada parada de una línea debe existir en el mapa de paradas");
			}
		}
	}
	// --- New tests to verify the two requested bidirectional behaviors ---

	/**
	 * Verifies that an exception is thrown when the frequencies file does not
	 * exist.
	 *
	 * <p>
	 * Condition: Non-existent frequencies file. Expected: loader throws an
	 * exception.
	 * </p>
	 *
	 * @throws IOException if reading the paradas file fails
	 */
	@Test
	public void testCargarLineas_ArchivoFrecuenciasInexistente_LanzaExcepcion() throws IOException {
		// Arrange
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);
		String archivoInexistente = "archivo_que_no_existe.txt";

		// Act & Assert
		assertThrows(Exception.class, () -> {
			CargarDatos.cargarLineas(ARCHIVO_LINEAS, archivoInexistente, paradas);
		}, "Debe lanzar excepción cuando el archivo de frecuencias no existe");
	}

	/**
	 * Verifies that an exception is thrown when the lines file does not exist.
	 *
	 * <p>
	 * Condition: Non-existent lines file. Expected: loader throws an exception.
	 * </p>
	 *
	 * @throws IOException if reading the paradas file fails
	 */
	@Test
	public void testCargarLineas_ArchivoLineasInexistente_LanzaExcepcion() throws IOException {
		// Arrange
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);
		String archivoInexistente = "archivo_que_no_existe.txt";

		// Act & Assert
		assertThrows(Exception.class, () -> {
			CargarDatos.cargarLineas(archivoInexistente, ARCHIVO_FRECUENCIAS, paradas);
		}, "Debe lanzar excepción cuando el archivo de líneas no existe");
	}

	/**
	 * Verifies that lines and frequencies are loaded correctly from valid files.
	 *
	 * <p>
	 * Condition: Valid lineas and frecuencias files. Expected: non-null, non-empty
	 * map containing a known line with correct attributes and stops list.
	 * </p>
	 *
	 * @throws IOException if reading the test files fails
	 */
	@Test
	public void testCargarLineas_ArchivosValidos_CargaExitosa() throws IOException {
		// Arrange
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);

		// Act
		lineas = CargarDatos.cargarLineas(ARCHIVO_LINEAS, ARCHIVO_FRECUENCIAS, paradas);

		// Assert
		assertNotNull(lineas, "El mapa de líneas no debe ser nulo");
		assertFalse(lineas.isEmpty(), "El mapa de líneas no debe estar vacío");

		// Verificar que existe una línea específica (L1I según el archivo)
		assertTrue(lineas.containsKey("L1I"), "Debe existir la línea L1I");

		Linea linea = lineas.get("L1I");
		assertNotNull(linea, "La línea L1I debe existir");
		assertEquals("L1I", linea.getCodigo(), "El código debe ser L1I");
		assertNotNull(linea.getParadas(), "La línea debe tener lista de paradas");
		assertFalse(linea.getParadas().isEmpty(), "La línea debe tener paradas asignadas");
	}

	/**
	 * Verifies that stops in a line are ordered according to the input file.
	 *
	 * <p>
	 * Condition: Lines loaded correctly. Expected: the first stops of L1I match the
	 * sequence defined in the test input file.
	 * </p>
	 *
	 * @throws IOException if reading the test files fails
	 */
	@Test
	public void testCargarLineas_OrdenParadas_Correcto() throws IOException {
		// Arrange
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);

		// Act
		lineas = CargarDatos.cargarLineas(ARCHIVO_LINEAS, ARCHIVO_FRECUENCIAS, paradas);

		// Assert
		Linea lineaL1I = lineas.get("L1I");
		assertNotNull(lineaL1I, "La línea L1I debe existir");

		// Verificar que las primeras paradas están en orden según el archivo
		// L1I;Línea 1 Ida;88;97;44;43;47;58;37;74;77;25;24;5;52;14;61;35;34;89;
		assertEquals(88, lineaL1I.getParadas().get(0).getCodigo(), "Primera parada debe ser 88");
		assertEquals(97, lineaL1I.getParadas().get(1).getCodigo(), "Segunda parada debe ser 97");
		assertEquals(44, lineaL1I.getParadas().get(2).getCodigo(), "Tercera parada debe ser 44");
	}

	/**
	 * Verifies that when lines are loaded, stops maintain a bidirectional reference
	 * to the line (i.e. parada.getLineas() contains the Linea instance).
	 *
	 * <p>
	 * Condition: Lines loaded. Expected: each stop referenced by a line has the
	 * corresponding line in its list of lines.
	 * </p>
	 *
	 * @throws Exception if loading fails
	 */
	@Test
	public void testCargarLineas_ParadaTieneLinea_Bidireccional() throws Exception {
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);
		lineas = CargarDatos.cargarLineas(ARCHIVO_LINEAS, ARCHIVO_FRECUENCIAS, paradas);

		Linea lineaL1I = lineas.get("L1I");
		assertNotNull(lineaL1I, "La línea L1I debe existir");

		Parada primera = lineaL1I.getParadas().get(0);
		assertNotNull(primera, "La primera parada de L1I no debe ser nula");

		// Verificar que la parada referencia a la línea (parada.agregarLinea(lineaObj)
		// fue invocado)
		assertNotNull(primera.getLineas(), "La lista de líneas de la parada no debe ser nula");
		assertTrue(primera.getLineas().contains(lineaL1I),
				"La parada debe contener la referencia a la línea (bidireccional)");
	}

	/**
	 * Verifies that an {@link IOException} is thrown when the stops file does not
	 * exist.
	 *
	 * <p>
	 * Condition: Non-existent file. Expected: {@link IOException} propagated by the
	 * loader.
	 * </p>
	 */
	@Test
	public void testCargarParadas_ArchivoInexistente_LanzaExcepcion() {
		// Arrange
		String archivoInexistente = "archivo_que_no_existe.txt";

		// Act & Assert
		assertThrows(IOException.class, () -> {
			CargarDatos.cargarParadas(archivoInexistente);
		}, "Debe lanzar IOException cuando el archivo no existe");
	}

	/**
	 * Verifies that stops are loaded correctly from a valid file.
	 *
	 * <p>
	 * Condition: The file exists and has valid format. Expected: a non-null,
	 * non-empty map containing specific known stops with correct attributes.
	 * </p>
	 *
	 * @throws IOException if reading the test file fails
	 */
	@Test
	public void testCargarParadas_ArchivoValido_CargaExitosa() throws IOException {
		// Arrange & Act
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);

		// Assert
		assertNotNull(paradas, "El mapa de paradas no debe ser nulo");
		assertFalse(paradas.isEmpty(), "El mapa de paradas no debe estar vacío");

		// Verificar que se cargaron paradas específicas
		assertTrue(paradas.containsKey(1), "Debe existir la parada con código 1");
		assertTrue(paradas.containsKey(5), "Debe existir la parada con código 5");

		// Verificar datos de una parada específica
		Parada parada1 = paradas.get(1);
		assertNotNull(parada1, "La parada 1 debe existir");
		assertEquals(1, parada1.getCodigo(), "El código debe ser 1");
		assertEquals("1 De Marzo, 405", parada1.getDireccion(), "La dirección debe coincidir");
		assertEquals(-42.766285, parada1.getLatitud(), 0.000001, "La latitud debe coincidir");
		assertEquals(-65.040768, parada1.getLongitud(), 0.000001, "La longitud debe coincidir");
	}

	/**
	 * Verifies that all loaded stops have valid geographic coordinates and
	 * non-empty addresses.
	 *
	 * <p>
	 * Condition: Paradas loaded correctly. Expected: latitude and longitude are
	 * non-zero and address strings are present.
	 * </p>
	 *
	 * @throws IOException if reading the test file fails
	 */
	@Test
	public void testCargarParadas_CoordenadasValidas_TodasLasParadas() throws IOException {
		// Arrange & Act
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);

		// Assert
		for (Parada parada : paradas.values()) {
			assertNotEquals(0.0, parada.getLatitud(), "La latitud no debe ser 0");
			assertNotEquals(0.0, parada.getLongitud(), "La longitud no debe ser 0");
			assertNotNull(parada.getDireccion(), "La dirección no debe ser nula");
			assertFalse(parada.getDireccion().trim().isEmpty(), "La dirección no debe estar vacía");
		}
	}

	/**
	 * Verifies that an exception is thrown when the tramos file does not exist.
	 *
	 * <p>
	 * Condition: Non-existent tramos file. Expected: loader throws an exception.
	 * </p>
	 *
	 * @throws IOException if reading the paradas file fails
	 */
	@Test
	public void testCargarTramos_ArchivoInexistente_LanzaExcepcion() throws IOException {
		// Arrange
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);
		String archivoInexistente = "archivo_que_no_existe.txt";

		// Act & Assert
		assertThrows(Exception.class, () -> {
			CargarDatos.cargarTramos(archivoInexistente, paradas);
		}, "Debe lanzar excepción cuando el archivo no existe");
	}

	/**
	 * Verifies that segments are loaded correctly given valid stops and segments
	 * files.
	 *
	 * <p>
	 * Condition: Valid paradas and tramos files. Expected: non-null, non-empty map
	 * containing expected segment keys and segment attributes.
	 * </p>
	 *
	 * @throws IOException if reading the test files fails
	 */
	@Test
	public void testCargarTramos_ArchivosValidos_CargaExitosa() throws IOException {
		// Arrange
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);

		// Act
		tramos = CargarDatos.cargarTramos(ARCHIVO_TRAMOS, paradas);

		// Assert
		assertNotNull(tramos, "El mapa de tramos no debe ser nulo");
		assertFalse(tramos.isEmpty(), "El mapa de tramos no debe estar vacío");

		// Buscar el tramo 88-97 en el mapa usando su toString completo
		Tramo tramoEsperado = new Tramo(paradas.get(88), paradas.get(97), 60, 1);
		String claveEsperada = tramoEsperado.getInicio().getCodigo() + "-" + tramoEsperado.getFin().getCodigo() + "-"
				+ tramoEsperado.getTipo();

		assertTrue(tramos.containsKey(claveEsperada), "Debe existir el tramo 88-97 con clave: " + claveEsperada);

		Tramo tramo = tramos.get(claveEsperada);
		assertNotNull(tramo, "El tramo 88-97 debe existir");
		assertNotNull(tramo.getInicio(), "El tramo debe tener parada de inicio");
		assertNotNull(tramo.getFin(), "El tramo debe tener parada de fin");
		assertEquals(88, tramo.getInicio().getCodigo(), "La parada de inicio debe ser 88");
		assertEquals(97, tramo.getFin().getCodigo(), "La parada de fin debe ser 97");
		assertEquals(60, tramo.getTiempo(), "El tiempo del tramo debe ser 60");
	}

	/**
	 * Verifies that tramos of type CAMINANDO produce a corresponding inverse tramo
	 * and that paradas record walking adjacency bidirectionally.
	 *
	 * <p>
	 * Condition: Tramos loaded. Expected: for every CAMINANDO tramo there exists
	 * an inverse tramo with swapped inicio/fin and the paradas reflect the walking adjacency.
	 * </p>
	 *
	 * @throws IOException if reading the test files fails
	 */
	@Test
	public void testCargarTramos_Caminando_BidireccionalYTramoInverso() throws IOException {
		// Arrange
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);

		// Act
		tramos = CargarDatos.cargarTramos(ARCHIVO_TRAMOS, paradas);

		// Assert
		boolean foundCaminando = false;
		for (Tramo t : tramos.values()) {
			if (t.getTipo() == Constantes.CAMINANDO) {
				foundCaminando = true;

				// Crear el tramo inverso esperado usando las mismas paradas
				Tramo tramoInverso = new Tramo(t.getFin(), t.getInicio(), t.getTiempo(), t.getTipo());
				String claveInversa = tramoInverso.getInicio().getCodigo() + "-" + tramoInverso.getFin().getCodigo()
						+ "-" + tramoInverso.getTipo();

				assertTrue(tramos.containsKey(claveInversa),
						"Debe existir el tramo inverso para caminando: " + claveInversa);

				Tramo inverso = tramos.get(claveInversa);
				assertNotNull(inverso, "El tramo inverso no debe ser nulo");
				assertEquals(t.getFin(), inverso.getInicio(), "El inicio del inverso debe ser el fin del original");
				assertEquals(t.getInicio(), inverso.getFin(), "El fin del inverso debe ser el inicio del original");
				assertEquals(t.getTiempo(), inverso.getTiempo(), "El tiempo debe ser el mismo");
				assertEquals(t.getTipo(), inverso.getTipo(), "El tipo debe ser el mismo");

				// Verificar bidireccionalidad en paradas
				assertTrue(t.getInicio().getParadaCaminando().contains(t.getFin()),
						"Inicio debe contener fin en paradaCaminando");
				assertTrue(t.getFin().getParadaCaminando().contains(t.getInicio()),
						"Fin debe contener inicio en paradaCaminando");
			}
		}
		assertTrue(foundCaminando, "Debe haber al menos un tramo de tipo CAMINANDO");
	}

	/**
	 * Verifies that every loaded segment has valid start and end stops and positive
	 * time.
	 *
	 * <p>
	 * Condition: Tramos loaded correctly. Expected: each Tramo has non-null inicio
	 * and fin and tiempo &gt; 0.
	 * </p>
	 *
	 * @throws IOException if reading the test files fails
	 */
	@Test
	public void testCargarTramos_ParadasValidas_TodosLosTramos() throws IOException {
		// Arrange
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);

		// Act
		tramos = CargarDatos.cargarTramos(ARCHIVO_TRAMOS, paradas);

		// Assert
		for (Tramo tramo : tramos.values()) {
			assertNotNull(tramo.getInicio(), "Todo tramo debe tener parada de inicio");
			assertNotNull(tramo.getFin(), "Todo tramo debe tener parada de fin");
			assertTrue(tramo.getTiempo() > 0, "El tiempo del tramo debe ser positivo");
		}
	}

}
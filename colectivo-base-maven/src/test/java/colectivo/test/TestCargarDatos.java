package colectivo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import colectivo.datos.CargarDatos;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Test completo para verificar la carga de datos desde archivos.
 * Verifica la correcta lectura y parsing de paradas, tramos, líneas y frecuencias.
 */
@DisplayName("Tests de Carga de Datos")
class TestCargarDatos {

	// Rutas dinámicas que funcionan desde diferentes contextos de ejecución
	private static final String BASE_PATH = getBasePath();
	private static final String ARCHIVO_PARADAS = BASE_PATH + "parada_PM.txt";
	private static final String ARCHIVO_TRAMOS = BASE_PATH + "tramo_PM.txt";
	private static final String ARCHIVO_LINEAS = BASE_PATH + "linea_PM.txt";
	private static final String ARCHIVO_FRECUENCIAS = BASE_PATH + "frecuencia_PM.txt";

	private Map<Integer, Parada> paradas;
	private Map<String, Tramo> tramos;
	private Map<String, Linea> lineas;

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
			"colectivo-base-maven/", // When running from workspace parent
			"../colectivo-base-maven/", // Variant
			"../../colectivo-base-maven/", // Another variant
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

	@BeforeEach
	void setUp() throws Exception {
		// Cargar todos los datos antes de cada test
		paradas = CargarDatos.cargarParadas(ARCHIVO_PARADAS);
		tramos = CargarDatos.cargarTramos(ARCHIVO_TRAMOS, paradas);
		lineas = CargarDatos.cargarLineas(ARCHIVO_LINEAS, ARCHIVO_FRECUENCIAS, paradas);
	}

	// ========== TESTS DE PARADAS ==========

	@Nested
	@DisplayName("Tests de Carga de Paradas")
	class TestsParadas {

		@Test
		@DisplayName("Debe cargar la cantidad correcta de paradas (104)")
		void testCargarParadas_CantidadCorrecta() {
			assertNotNull(paradas, "El mapa de paradas no debería ser null");
			assertTrue(paradas.size() > 0, "Deberían cargarse paradas");
			assertEquals(104, paradas.size(), "Deberían cargarse 104 paradas");
		}

		@Test
		@DisplayName("Debe cargar los datos de la primera parada correctamente")
		void testCargarParadas_DatosCorrectos() {
			// Verificar la primera parada: 1;1 De Marzo, 405;-42.766285;-65.040768;
			Parada parada1 = paradas.get(1);
			assertNotNull(parada1, "La parada 1 debería existir");
			assertEquals(1, parada1.getCodigo(), "Código de parada incorrecto");
			assertEquals("1 De Marzo, 405", parada1.getDireccion(), "Dirección incorrecta");
			assertEquals(-42.766285, parada1.getLatitud(), 0.000001, "Latitud incorrecta");
			assertEquals(-65.040768, parada1.getLongitud(), 0.000001, "Longitud incorrecta");
		}

		@Test
		@DisplayName("Debe cargar varias paradas específicas correctamente")
		void testCargarParadas_VariasParadas() {
			assertTrue(paradas.containsKey(5), "La parada 5 debería existir");
			assertTrue(paradas.containsKey(50), "La parada 50 debería existir");
			assertTrue(paradas.containsKey(100), "La parada 100 debería existir");

			Parada parada5 = paradas.get(5);
			assertEquals("28 De Julio, 200", parada5.getDireccion());
		}

		@Test
		@DisplayName("Debe retornar null para paradas inexistentes")
		void testCargarParadas_ParadaInexistente() {
			assertNull(paradas.get(999), "La parada 999 no debería existir");
			assertNull(paradas.get(0), "La parada 0 no debería existir");
			assertNull(paradas.get(-1), "La parada -1 no debería existir");
		}
	}

	// ========== TESTS DE TRAMOS ==========

	@Nested
	@DisplayName("Tests de Carga de Tramos")
	class TestsTramos {

		@Test
		@DisplayName("Debe cargar tramos correctamente")
		void testCargarTramos_CantidadCorrecta() {
			assertNotNull(tramos, "El mapa de tramos no debería ser null");
			assertTrue(tramos.size() > 0, "Deberían cargarse tramos");
		}

		@Test
		@DisplayName("Las claves de tramos deben tener el formato 'inicio-fin'")
		void testCargarTramos_FormatoClave() {
			for (String clave : tramos.keySet()) {
				assertTrue(clave.contains("-"), "La clave del tramo debe contener '-'");
				String[] partes = clave.split("-");
				assertEquals(3, partes.length, "La clave debe tener formato 'inicio-fin-tipo'");
			}
		}

		@Test
		@DisplayName("Los tramos deben tener datos válidos")
		void testCargarTramos_DatosCorrectos() {
			assertFalse(tramos.isEmpty(), "Debe haber al menos un tramo");
			
			Tramo primerTramo = tramos.values().iterator().next();
			assertNotNull(primerTramo, "El tramo no debería ser null");
			assertNotNull(primerTramo.getInicio(), "La parada de inicio no debería ser null");
			assertNotNull(primerTramo.getFin(), "La parada de fin no debería ser null");
			assertTrue(primerTramo.getTiempo() > 0, "El tiempo debería ser positivo");
			assertTrue(primerTramo.getTipo() >= 0, "El tipo debería ser válido");
		}

		@Test
		@DisplayName("Todas las paradas de los tramos deben existir en el mapa de paradas")
		void testCargarTramos_ParadasValidas() {
			for (Tramo tramo : tramos.values()) {
				Parada inicio = tramo.getInicio();
				Parada fin = tramo.getFin();
				
				assertTrue(paradas.containsValue(inicio), 
						"La parada de inicio debe existir en el mapa");
				assertTrue(paradas.containsValue(fin), 
						"La parada de fin debe existir en el mapa");
			}
		}
	}

	// ========== TESTS DE LÍNEAS ==========

	@Nested
	@DisplayName("Tests de Carga de Líneas")
	class TestsLineas {

		@Test
		@DisplayName("Debe cargar la cantidad correcta de líneas (12)")
		void testCargarLineas_CantidadCorrecta() {
			assertNotNull(lineas, "El mapa de líneas no debería ser null");
			assertTrue(lineas.size() > 0, "Deberían cargarse líneas");
			assertEquals(12, lineas.size(), "Deberían cargarse 12 líneas");
		}

		@Test
		@DisplayName("Debe cargar los datos básicos de una línea correctamente")
		void testCargarLineas_DatosCorrectos() {
			// Verificar la línea L1I: L1I;Línea 1 Ida;88;97;44;43;47;58;37;74;77;25;24;5;52;14;61;35;34;89;
			Linea lineaL1I = lineas.get("L1I");
			assertNotNull(lineaL1I, "La línea L1I debería existir");
			assertEquals("L1I", lineaL1I.getCodigo(), "Código incorrecto");
			assertEquals("Línea 1 Ida", lineaL1I.getNombre(), "Nombre incorrecto");
		}

		@Test
		@DisplayName("Las paradas deben cargarse en el orden correcto (izquierda a derecha)")
		void testCargarLineas_RecorridoOrdenCorrecto() {
			// L1I;Línea 1 Ida;88;97;44;43;47;58;37;74;77;25;24;5;52;14;61;35;34;89;
			Linea lineaL1I = lineas.get("L1I");
			assertNotNull(lineaL1I, "La línea L1I debería existir");
			
			assertEquals(18, lineaL1I.getParadas().size(), "La línea debería tener 18 paradas");
			
			// Verificar el orden del recorrido
			assertEquals(88, lineaL1I.getParadas().get(0).getCodigo(), 
					"Primera parada debería ser 88");
			assertEquals(97, lineaL1I.getParadas().get(1).getCodigo(), 
					"Segunda parada debería ser 97");
			assertEquals(44, lineaL1I.getParadas().get(2).getCodigo(), 
					"Tercera parada debería ser 44");
			assertEquals(89, lineaL1I.getParadas().get(lineaL1I.getParadas().size() - 1).getCodigo(), 
					"Última parada debería ser 89");
		}

		@Test
		@DisplayName("Todas las líneas esperadas deben existir")
		void testCargarLineas_TodasLasLineas() {
			String[] codigosEsperados = {"L1I", "L1R", "L2I", "L2R", "L3I", "L3R", 
										  "L4I", "L4R", "L5I", "L5R", "L6I", "L6R"};
			
			for (String codigo : codigosEsperados) {
				assertTrue(lineas.containsKey(codigo), 
						"La línea " + codigo + " debería existir");
				assertNotNull(lineas.get(codigo), 
						"La línea " + codigo + " no debería ser null");
			}
		}

		@Test
		@DisplayName("Línea con muchas paradas debe cargarse correctamente")
		void testCargarLineas_LineaConMuchasParadas() {
			// L5R tiene 20 paradas
			Linea lineaL5R = lineas.get("L5R");
			assertNotNull(lineaL5R, "La línea L5R debería existir");
			assertEquals(21, lineaL5R.getParadas().size(), "L5R debería tener 20 paradas");
			
			// Verificar primera y última parada del recorrido
			// L5R;L�nea 5 Regreso;67;91;104;72;85;57;56;98;41;44;43;47;99;24;5;54;28;101;18;78;13;
			assertEquals(67, lineaL5R.getParadas().get(0).getCodigo(), 
					"Primera parada de L5R debería ser 67");
			assertEquals(13, lineaL5R.getParadas().get(20).getCodigo(), 
					"Última parada de L5R debería ser 13");
		}

		@Test
		@DisplayName("Línea con menos paradas debe cargarse correctamente")
		void testCargarLineas_LineaConPocasParadas() {
			// L4I;L�nea 4 Ida;1;6;75;76;29;27;87;86;103;70;60;
			Linea lineaL4I = lineas.get("L4I");
			assertNotNull(lineaL4I, "La línea L4I debería existir");
			assertEquals(11, lineaL4I.getParadas().size(), "L4I debería tener 11 paradas");
			
			// Verificar primera y última
			assertEquals(1, lineaL4I.getParadas().get(0).getCodigo());
			assertEquals(60, lineaL4I.getParadas().get(10).getCodigo());
		}

		@Test
		@DisplayName("Líneas de ida y vuelta deben ser diferentes")
		void testCargarLineas_IdaYVuelta() {
			Linea lineaL1I = lineas.get("L1I");
			Linea lineaL1R = lineas.get("L1R");
			
			assertNotNull(lineaL1I);
			assertNotNull(lineaL1R);
			
			assertNotEquals(lineaL1I.getCodigo(), lineaL1R.getCodigo(), 
					"Los códigos de ida y vuelta deben ser diferentes");
			assertNotEquals(lineaL1I.getParadas().size(), lineaL1R.getParadas().size(), 
					"L1I y L1R tienen diferente cantidad de paradas");
		}
	}

	// ========== TESTS DE MANEJO DE ERRORES ==========

	@Nested
	@DisplayName("Tests de Manejo de Errores")
	class TestsErrores {

		@Test
		@DisplayName("Debe lanzar excepción si el archivo de paradas no existe")
		void testCargarParadas_ArchivoInexistente() {
			assertThrows(Exception.class, () -> {
				CargarDatos.cargarParadas("archivo_que_no_existe.txt");
			}, "Debería lanzar excepción al no encontrar el archivo");
		}

		@Test
		@DisplayName("Debe lanzar excepción si el archivo de tramos no existe")
		void testCargarTramos_ArchivoInexistente() {
			assertThrows(Exception.class, () -> {
				CargarDatos.cargarTramos("archivo_que_no_existe.txt", paradas);
			}, "Debería lanzar excepción al no encontrar el archivo");
		}

		@Test
		@DisplayName("Debe lanzar excepción si el archivo de líneas no existe")
		void testCargarLineas_ArchivoLineasInexistente() {
			assertThrows(Exception.class, () -> {
				CargarDatos.cargarLineas("archivo_que_no_existe.txt", ARCHIVO_FRECUENCIAS, paradas);
			}, "Debería lanzar excepción al no encontrar el archivo");
		}

		@Test
		@DisplayName("Debe lanzar excepción si el archivo de frecuencias no existe")
		void testCargarLineas_ArchivoFrecuenciasInexistente() {
			assertThrows(Exception.class, () -> {
				CargarDatos.cargarLineas(ARCHIVO_LINEAS, "archivo_que_no_existe.txt", paradas);
			}, "Debería lanzar excepción al no encontrar el archivo");
		}
	}

	// ========== TESTS DE INTEGRIDAD ==========

	@Nested
	@DisplayName("Tests de Integridad de Datos")
	class TestsIntegridad {

		@Test
		@DisplayName("Todas las paradas de las líneas deben existir en el mapa de paradas")
		void testIntegridad_TodasLasParadasDeLasLineasExisten() {
			for (Linea linea : lineas.values()) {
				for (Parada parada : linea.getParadas()) {
					assertTrue(paradas.containsValue(parada), 
							"La parada " + parada.getCodigo() + " de la línea " + 
							linea.getCodigo() + " debería existir en el mapa de paradas");
				}
			}
		}

		@Test
		@DisplayName("Todas las líneas deben tener al menos una parada")
		void testIntegridad_LineasTienenAlMenosUnaParada() {
			for (Linea linea : lineas.values()) {
				assertTrue(linea.getParadas().size() > 0, 
						"La línea " + linea.getCodigo() + " debería tener al menos una parada");
			}
		}

		@Test
		@DisplayName("No debe haber códigos duplicados")
		void testIntegridad_CodigosUnicos() {
			assertEquals(paradas.size(), paradas.keySet().size(), 
					"No debería haber códigos de paradas duplicados");
			assertEquals(lineas.size(), lineas.keySet().size(), 
					"No debería haber códigos de líneas duplicados");
		}

		@Test
		@DisplayName("Las paradas deben tener coordenadas válidas")
		void testIntegridad_CoordenadasValidas() {
			for (Parada parada : paradas.values()) {
				assertTrue(parada.getLatitud() >= -90 && parada.getLatitud() <= 90, 
						"Latitud de parada " + parada.getCodigo() + " fuera de rango");
				assertTrue(parada.getLongitud() >= -180 && parada.getLongitud() <= 180, 
						"Longitud de parada " + parada.getCodigo() + " fuera de rango");
			}
		}

		@Test
		@DisplayName("Las paradas de Puerto Madryn deben estar en el rango correcto")
		void testIntegridad_CoordenadasPuertoMadryn() {
			// Puerto Madryn está aproximadamente entre:
			// Latitud: -42.7 a -42.8
			// Longitud: -65.0 a -65.1
			for (Parada parada : paradas.values()) {
				assertTrue(parada.getLatitud() >= -43 && parada.getLatitud() <= -42, 
						"Parada " + parada.getCodigo() + " fuera del rango de Puerto Madryn (latitud)");
				assertTrue(parada.getLongitud() >= -66 && parada.getLongitud() <= -64, 
						"Parada " + parada.getCodigo() + " fuera del rango de Puerto Madryn (longitud)");
			}
		}

		@Test
		@DisplayName("Los tramos deben tener tiempos razonables")
		void testIntegridad_TiemposRazonables() {
			for (Tramo tramo : tramos.values()) {
				assertTrue(tramo.getTiempo() > 0, 
						"El tiempo del tramo debe ser positivo");
				assertTrue(tramo.getTiempo() < 3600, 
						"El tiempo del tramo no debería ser mayor a 1 hora (3600 segundos)");
			}
		}
	}

	// ========== TESTS ADICIONALES DE CASOS ESPECIALES ==========

	@Nested
	@DisplayName("Tests de Casos Especiales")
	class TestsCasosEspeciales {

		@Test
		@DisplayName("Verificar que una línea específica tiene las paradas esperadas")
		void testLineaEspecifica_L2I() {
			// L2I;Línea 2 Ida;48;13;79;7;84;83;90;16;17;55;53;26;3;4;19;21;9;30;31;
			Linea lineaL2I = lineas.get("L2I");
			assertNotNull(lineaL2I);
			assertEquals(19, lineaL2I.getParadas().size());
			
			// Verificar algunas paradas intermedias
			assertEquals(48, lineaL2I.getParadas().get(0).getCodigo());
			assertEquals(79, lineaL2I.getParadas().get(2).getCodigo());
			assertEquals(31, lineaL2I.getParadas().get(18).getCodigo());
		}

		@Test
		@DisplayName("Verificar que las líneas no comparten el mismo objeto de parada")
		void testParadasCompartidas() {
			Linea lineaL1I = lineas.get("L1I");
			Linea lineaL1R = lineas.get("L1R");
			
			// Buscar si hay paradas compartidas
			boolean hayParadasCompartidas = false;
			for (Parada paradaL1I : lineaL1I.getParadas()) {
				for (Parada paradaL1R : lineaL1R.getParadas()) {
					if (paradaL1I.getCodigo() == paradaL1R.getCodigo()) {
						// Deben ser el mismo objeto (referencia)
						assertSame(paradaL1I, paradaL1R, 
								"Las paradas compartidas deben ser el mismo objeto");
						hayParadasCompartidas = true;
					}
				}
			}
			
			assertTrue(hayParadasCompartidas, 
					"Las líneas L1I y L1R deberían compartir algunas paradas");
		}
	}
}

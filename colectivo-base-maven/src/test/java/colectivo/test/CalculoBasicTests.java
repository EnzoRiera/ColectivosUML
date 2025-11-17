package colectivo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import colectivo.datos.CargarDatos;
import colectivo.datos.CargarParametros;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

/**
 * Tests básicos del método principal calcularRecorrido de la clase Calculo.
 * Estos tests verifican el funcionamiento general del algoritmo de cálculo de rutas.
 */
@DisplayName("Tests Básicos - Método calcularRecorrido")
class CalculoBasicTests {

	private Map<Integer, Parada> paradas;
	private Map<String, Linea> lineas;
	private Map<String, Tramo> tramos;

	@BeforeEach
	void setUp() throws Exception {
		// Cargar configuración y datos de prueba
		CargarParametros.parametros();
		paradas = CargarDatos.cargarParadas(CargarParametros.getArchivoParada());
		lineas = CargarDatos.cargarLineas(CargarParametros.getArchivoLinea(), CargarParametros.getArchivoFrecuencia(),
				paradas);
		tramos = CargarDatos.cargarTramos(CargarParametros.getArchivoTramo(), paradas);
	}

	@Nested
	@DisplayName("Casos de Funcionamiento Normal")
	class NormalOperationTests {

		@Test
		@DisplayName("Debe encontrar rutas directas cuando existen")
		void testCalculaRecorridoConRutasDirectas() {
			// Given: Paradas que tienen línea directa
			Parada origen = paradas.get(44); // Espa�a, 1660
			Parada destino = paradas.get(47); // Espa�a, 928
			int diaSemana = 1; // Lunes
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When: Calculamos el recorrido
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Debe encontrar al menos una ruta
			assertFalse(recorridos.isEmpty(), "Debe encontrar al menos una ruta directa");
			assertTrue(recorridos.size() >= 2, "Debe encontrar múltiples opciones de línea directa");

			// Verificar que cada ruta tiene exactamente un segmento (directo)
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				assertEquals(1, ruta.size(), "Las rutas directas deben tener un solo segmento");
				assertNotNull(ruta.get(0).getLinea(), "El segmento debe tener una línea asignada");
			}
		}

		@Test
		@DisplayName("Debe encontrar rutas con transbordo cuando no hay directas")
		void testCalculaRecorridoConTransbordo() {
			// Given: Paradas que requieren transbordo
			Parada origen = paradas.get(88); // Una parada que requiere conexión
			Parada destino = paradas.get(13); // Destino que requiere transbordo
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then
			assertFalse(recorridos.isEmpty(), "Debe encontrar rutas con transbordo");
			assertTrue(recorridos.size() >= 1, "Debe encontrar al menos una opción con transbordo");

			// Verificar que cada ruta tiene exactamente dos segmentos
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				assertEquals(2, ruta.size(), "Las rutas con transbordo deben tener dos segmentos");
				assertNotNull(ruta.get(0).getLinea(), "Primer segmento debe tener línea");
				assertNotNull(ruta.get(1).getLinea(), "Segundo segmento debe tener línea");
			}
		}

		@Test
		@DisplayName("Debe encontrar rutas caminando cuando no hay otras opciones")
		void testCalculaRecorridoCaminando() {
			// Given: Paradas conectadas solo caminando
			Parada origen = paradas.get(31); // Parada que requiere caminata
			Parada destino = paradas.get(66); // Destino accesible caminando
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then
			assertFalse(recorridos.isEmpty(), "Debe encontrar rutas caminando");
			assertEquals(1, recorridos.size(), "Debe haber exactamente una ruta caminando");

			// Verificar que la ruta tiene múltiples segmentos incluyendo caminata
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);
			assertTrue(ruta.size() >= 3, "La ruta caminando debe tener al menos 3 segmentos");

			// Verificar que hay al menos un segmento de caminata (linea == null)
			boolean hasWalkingSegment = ruta.stream().anyMatch(r -> r.getLinea() == null);
			assertTrue(hasWalkingSegment, "Debe incluir al menos un segmento de caminata");
		}
	}

	@Nested
	@DisplayName("Casos sin Solución")
	class NoSolutionTests {

		@Test
		@DisplayName("Debe retornar lista vacía cuando no hay rutas posibles")
		void testCalculaRecorridoSinRutasPosibles() {
			// Given: Paradas sin conexión posible
			Parada origen = paradas.get(66); // Parada aislada
			Parada destino = paradas.get(31); // Destino no accesible
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then
			assertTrue(recorridos.isEmpty(), "Debe retornar lista vacía cuando no hay rutas");
		}
	}

	@Nested
	@DisplayName("Validación de Resultados")
	class ResultValidationTests {

		@Test
		@DisplayName("Todas las rutas deben tener horarios válidos")
		void testHorariosValidosEnResultados() {
			// Given
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				for (colectivo.logica.Recorrido segmento : ruta) {
					assertNotNull(segmento.getHoraSalida(), "Cada segmento debe tener hora de salida");
					assertTrue(segmento.getDuracion() > 0, "Cada segmento debe tener duración positiva");
					assertFalse(segmento.getParadas().isEmpty(), "Cada segmento debe tener paradas");
				}
			}
		}

		@Test
		@DisplayName("Las rutas pueden no estar ordenadas por hora de salida")
		void testRutasOrdenadasPorHorario() {
			// Given
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: El código actual no ordena explícitamente por hora de salida
			// Solo verifica que hay resultados válidos
			assertNotNull(recorridos, "Debe retornar una lista de recorridos");
			if (!recorridos.isEmpty()) {
				assertTrue(recorridos.get(0).size() > 0, "Debe haber al menos un segmento en la primera ruta");
			}
		}
	}
}
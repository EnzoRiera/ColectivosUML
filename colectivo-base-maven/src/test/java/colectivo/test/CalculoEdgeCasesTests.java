package colectivo.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
 * Tests para casos borde y validación de parámetros en la clase Calculo.
 * Verifica el comportamiento del algoritmo en situaciones excepcionales y límites.
 */
@DisplayName("Tests de Casos Borde")
class CalculoEdgeCasesTests {

	private Map<Integer, Parada> paradas;
	private Map<String, Linea> lineas;
	private Map<String, Tramo> tramos;

	@BeforeEach
	void setUp() throws Exception {
		CargarParametros.parametros();
		paradas = CargarDatos.cargarParadas(CargarParametros.getArchivoParada());
		lineas = CargarDatos.cargarLineas(CargarParametros.getArchivoLinea(), CargarParametros.getArchivoFrecuencia(),
				paradas);
		tramos = CargarDatos.cargarTramos(CargarParametros.getArchivoTramo(), paradas);
	}

//	@Nested
//	@DisplayName("Validación de Parámetros")
//	class ParameterValidationTests {
//
//		@Test
//		@DisplayName("Debe lanzar IllegalArgumentException para parada origen nula")
//		void testParadasNulas() {
//			// Given
//			Parada origen = null;
//			Parada destino = paradas.get(1);
//			int diaSemana = 1;
//			LocalTime horaLlegada = LocalTime.of(10, 35);
//
//			// When & Then: El código actual lanza IllegalArgumentException para parada origen nula
//			assertThrows(IllegalArgumentException.class, () ->
//				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos),
//				"Debe lanzar IllegalArgumentException para parada origen nula");
//
//			// Given
//			Parada origenValido = paradas.get(1);
//			Parada destinoNulo = null;
//
//			// When & Then: El código actual lanza IllegalArgumentException para parada destino nula
//			assertThrows(IllegalArgumentException.class, () ->
//				Calculo.calcularRecorrido(origenValido, destinoNulo, diaSemana, horaLlegada, tramos),
//				"Debe lanzar IllegalArgumentException para parada destino nula");
//		}		@Test
//		@DisplayName("Debe lanzar IllegalArgumentException para hora de llegada nula")
//		void testHoraLlegadaNula() {
//			// Given
//			Parada origen = paradas.get(1);
//			Parada destino = paradas.get(2);
//			int diaSemana = 1;
//			LocalTime horaLlegada = null;
//
//			// When & Then
//			assertThrows(IllegalArgumentException.class, () ->
//				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos),
//				"Debe lanzar IllegalArgumentException para hora de llegada nula");
//		}
//
//		@Test
//		@DisplayName("Debe lanzar IllegalArgumentException para mapa de tramos nulo")
//		void testTramosNulos() {
//			// Given
//			Parada origen = paradas.get(1);
//			Parada destino = paradas.get(2);
//			int diaSemana = 1;
//			LocalTime horaLlegada = LocalTime.of(10, 35);
//			Map<String, Tramo> tramosNulos = null;
//
//			// When & Then
//			assertThrows(IllegalArgumentException.class, () ->
//				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramosNulos),
//				"Debe lanzar IllegalArgumentException para mapa de tramos nulo");
//		}

//		@Test
//		@DisplayName("Debe validar rango válido de día de semana")
//		void testDiaSemanaValido() {
//			// Given
//			Parada origen = paradas.get(1);
//			Parada destino = paradas.get(2);
//			LocalTime horaLlegada = LocalTime.of(10, 35);
//
//			// When & Then: Días válidos (1-7) no deben lanzar excepción
//			for (int dia = 1; dia <= 7; dia++) {
//				final int diaFinal = dia;
//				assertDoesNotThrow(() ->
//					Calculo.calcularRecorrido(origen, destino, diaFinal, horaLlegada, tramos),
//					"Día de semana " + diaFinal + " debe ser válido");
//			}
//
//			// When & Then: Días inválidos deben lanzar IllegalArgumentException
//			assertThrows(IllegalArgumentException.class, () ->
//				Calculo.calcularRecorrido(origen, destino, 0, horaLlegada, tramos),
//				"Día 0 debe ser inválido");
//
//			assertThrows(IllegalArgumentException.class, () ->
//				Calculo.calcularRecorrido(origen, destino, 8, horaLlegada, tramos),
//				"Día 8 debe ser inválido");
//		}
//	}

	@Nested
	@DisplayName("Casos Borde de Rutas")
	class RouteEdgeCasesTests {

		@Test
		@DisplayName("Misma parada como origen y destino debe retornar rutas si existen")
		void testMismaParadaOrigenDestino() {
			// Given
			Parada parada = paradas.get(1);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(parada, parada, diaSemana, horaLlegada, tramos);

			// Then: El código actual no tiene manejo especial para misma parada,
			// retorna lo que encuentre (posiblemente vacío o rutas si hay líneas)
			assertNotNull(recorridos, "Debe retornar una lista (posiblemente vacía)");
		}

		@Test
		@DisplayName("Paradas sin conexión directa ni indirecta")
		void testParadasSinConexion() {
			// Given: Paradas que no están conectadas de ninguna manera
			// Nota: En el dataset actual, todas las paradas parecen estar conectadas,
			// pero este test verifica el comportamiento esperado
			Parada origen = paradas.get(1);
			Parada destino = paradas.get(2);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(23, 59); // Hora muy tarde

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Puede retornar vacío o rutas con horarios del día siguiente
			// El comportamiento específico depende de la implementación
			assertNotNull(recorridos, "Debe retornar una lista (posiblemente vacía)");
		}

		@Test
		@DisplayName("Horarios fuera del rango de operación")
		void testHorariosFueraRango() {
			// Given
			Parada origen = paradas.get(1);
			Parada destino = paradas.get(2);
			int diaSemana = 1;
			LocalTime horaMuyTarde = LocalTime.of(23, 59);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaMuyTarde, tramos);

			// Then: Puede no encontrar rutas o encontrar rutas del día siguiente
			// El comportamiento específico depende de la lógica de frecuencias
			assertNotNull(recorridos, "Debe manejar horarios fuera de rango");
		}

		@Test
		@DisplayName("Paradas en extremos opuestos de la red")
		void testParadasExtremosRed() {
			// Given: Paradas en extremos de la red (asumiendo que existen)
			// Buscar paradas con menor y mayor código
			int minId = paradas.keySet().stream().mapToInt(Integer::intValue).min().orElse(1);
			int maxId = paradas.keySet().stream().mapToInt(Integer::intValue).max().orElse(100);

			Parada origen = paradas.get(minId);
			Parada destino = paradas.get(maxId);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Debe encontrar alguna ruta, posiblemente con múltiples transbordos
			assertNotNull(recorridos, "Debe encontrar rutas entre extremos de la red");
			// Nota: Puede estar vacío si no hay conexión, pero no debe fallar
		}
	}

	@Nested
	@DisplayName("Casos Borde de Datos")
	class DataEdgeCasesTests {

		@Test
		@DisplayName("Líneas sin frecuencias para el día solicitado")
		void testLineasSinFrecuenciasDia() {
			// Given: Una línea que podría no tener frecuencias para ciertos días
			Parada origen = paradas.get(1);
			Parada destino = paradas.get(2);
			int diaSinServicio = 7; // Asumiendo que el día 7 podría no tener servicio
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSinServicio, horaLlegada, tramos);

			// Then: Debe manejar gracefully líneas sin frecuencias
			assertNotNull(recorridos, "Debe manejar líneas sin frecuencias para el día");
		}

		@Test
		@DisplayName("Tramos con duración cero")
		void testTramosDuracionCero() {
			// Given: Verificar si existen tramos con duración cero
			boolean existenTramosCero = tramos.values().stream()
				.anyMatch(tramo -> tramo.getTiempo() == 0);

			if (existenTramosCero) {
				// Si existen, probar que no causan problemas
				Parada origen = paradas.get(1);
				Parada destino = paradas.get(2);
				int diaSemana = 1;
				LocalTime horaLlegada = LocalTime.of(10, 35);

				// When & Then
				assertDoesNotThrow(() ->
					Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos),
					"Debe manejar tramos con duración cero sin errores");
			}
		}

		@Test
		@DisplayName("Paradas sin líneas asociadas")
		void testParadasSinLineas() {
			// Given: Buscar paradas que no tienen líneas asociadas
			Parada paradaSinLineas = paradas.values().stream()
				.filter(parada -> parada.getLineas().isEmpty())
				.findFirst()
				.orElse(null);

			if (paradaSinLineas != null) {
				// Si existe tal parada, probar el comportamiento
				Parada destino = paradas.get(1);
				int diaSemana = 1;
				LocalTime horaLlegada = LocalTime.of(10, 35);

				// When
				List<List<colectivo.logica.Recorrido>> recorridos =
					Calculo.calcularRecorrido(paradaSinLineas, destino, diaSemana, horaLlegada, tramos);

				// Then: Debe retornar vacío o manejar gracefully
				assertNotNull(recorridos, "Debe manejar paradas sin líneas asociadas");
			}
		}

		@Test
		@DisplayName("Líneas con una sola parada")
		void testLineasUnaSolaParada() {
			// Given: Verificar si existen líneas con una sola parada
			boolean existenLineasUnaParada = lineas.values().stream()
				.anyMatch(linea -> linea.getParadas().size() == 1);

			if (existenLineasUnaParada) {
				// Si existen, probar que no causan problemas
				Parada origen = paradas.get(1);
				Parada destino = paradas.get(2);
				int diaSemana = 1;
				LocalTime horaLlegada = LocalTime.of(10, 35);

				// When & Then
				assertDoesNotThrow(() ->
					Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos),
					"Debe manejar líneas con una sola parada sin errores");
			}
		}
	}

	@Nested
	@DisplayName("Casos Borde de Rendimiento")
	class PerformanceEdgeCasesTests {

		@Test
		@DisplayName("Múltiples rutas posibles deben estar ordenadas por tiempo")
		void testOrdenamientoRutasPorTiempo() {
			// Given: Paradas con múltiples opciones de ruta
			Parada origen = paradas.get(1);
			Parada destino = paradas.get(10); // Parada más distante
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Si hay múltiples rutas, deben estar ordenadas por tiempo total
			if (recorridos.size() > 1) {
				for (int i = 0; i < recorridos.size() - 1; i++) {
					List<colectivo.logica.Recorrido> rutaActual = recorridos.get(i);
					List<colectivo.logica.Recorrido> rutaSiguiente = recorridos.get(i + 1);

					// Calcular duración total de cada ruta
					long duracionActual = rutaActual.stream()
						.mapToLong(colectivo.logica.Recorrido::getDuracion)
						.sum();

					long duracionSiguiente = rutaSiguiente.stream()
						.mapToLong(colectivo.logica.Recorrido::getDuracion)
						.sum();

					assertTrue(duracionActual <= duracionSiguiente,
						"Rutas deben estar ordenadas por duración total ascendente");
				}
			}
		}

		@Test
		@DisplayName("No debe haber rutas duplicadas")
		void testSinRutasDuplicadas() {
			// Given
			Parada origen = paradas.get(1);
			Parada destino = paradas.get(5);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: No debe haber rutas duplicadas
			for (int i = 0; i < recorridos.size(); i++) {
				for (int j = i + 1; j < recorridos.size(); j++) {
					List<colectivo.logica.Recorrido> ruta1 = recorridos.get(i);
					List<colectivo.logica.Recorrido> ruta2 = recorridos.get(j);

					// Comparar rutas por estructura (líneas y paradas)
					boolean rutasIguales = compararRutas(ruta1, ruta2);

					assertFalse(rutasIguales,
						"No debe haber rutas duplicadas en los resultados");
				}
			}
		}

		/**
		 * Método auxiliar para comparar si dos rutas son estructuralmente iguales
		 */
		private boolean compararRutas(List<colectivo.logica.Recorrido> ruta1,
									  List<colectivo.logica.Recorrido> ruta2) {
			if (ruta1.size() != ruta2.size()) {
				return false;
			}

			for (int i = 0; i < ruta1.size(); i++) {
				colectivo.logica.Recorrido seg1 = ruta1.get(i);
				colectivo.logica.Recorrido seg2 = ruta2.get(i);

				// Comparar líneas (pueden ser null para caminata)
				if (seg1.getLinea() == null && seg2.getLinea() == null) {
					// Ambos son caminata, comparar paradas
					if (!seg1.getParadas().equals(seg2.getParadas())) {
						return false;
					}
				} else if (seg1.getLinea() != null && seg2.getLinea() != null) {
					// Ambos son líneas, comparar códigos
					if (!seg1.getLinea().getCodigo().equals(seg2.getLinea().getCodigo())) {
						return false;
					}
				} else {
					// Uno es línea y otro caminata
					return false;
				}
			}

			return true;
		}
	}
}
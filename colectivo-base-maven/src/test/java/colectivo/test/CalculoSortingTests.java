package colectivo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * Tests específicos para ordenamiento de resultados en la clase Calculo.
 * Verifica que las rutas se ordenen correctamente por tiempo total.
 */
@DisplayName("Tests de Ordenamiento de Resultados")
class CalculoSortingTests {

	private Map<Integer, Parada> paradas;
	@SuppressWarnings("unused")
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

	@Nested
	@DisplayName("Ordenamiento por Tiempo Total")
	class TimeBasedSortingTests {

		@Test
		@DisplayName("Las rutas deben estar ordenadas por tiempo total ascendente")
		void testOrdenamientoPorTiempoAscendente() {
			// Given: Paradas que tienen múltiples rutas con diferentes tiempos
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> rutas = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar que hay rutas y están ordenadas por tiempo
			assertFalse(rutas.isEmpty(), "Debe encontrar al menos una ruta");

			for (int i = 0; i < rutas.size() - 1; i++) {
				long tiempoActual = rutas.get(i).stream()
					.mapToLong(colectivo.logica.Recorrido::getDuracion)
					.sum();

				long tiempoSiguiente = rutas.get(i + 1).stream()
					.mapToLong(colectivo.logica.Recorrido::getDuracion)
					.sum();

				assertTrue(tiempoActual <= tiempoSiguiente,
					"Ruta " + i + " (tiempo: " + tiempoActual + "s) debe ser <= ruta " + (i + 1) + " (tiempo: " + tiempoSiguiente + "s)");
			}
		}


		@Test
		@DisplayName("Ruta más rápida debe aparecer primero")
		void testRutaMasRapidaPrimero() {
			// Given: Paradas con múltiples opciones de ruta
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> rutas = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: La primera ruta debe ser la más rápida
			assertFalse(rutas.isEmpty(), "Debe encontrar rutas");

			long tiempoMasRapido = rutas.get(0).stream()
				.mapToLong(colectivo.logica.Recorrido::getDuracion)
				.sum();

			// Verificar que ninguna otra ruta es más rápida
			for (int i = 1; i < rutas.size(); i++) {
				long tiempoActual = rutas.get(i).stream()
					.mapToLong(colectivo.logica.Recorrido::getDuracion)
					.sum();

				assertTrue(tiempoMasRapido <= tiempoActual,
					"La primera ruta debe ser la más rápida o igual");
			}
		}
	}

	@Nested
	@DisplayName("Ordenamiento con Rutas Complejas")
	class ComplexRouteSortingTests {

		@Test
		@DisplayName("Rutas con transbordos deben ordenarse correctamente por tiempo total")
		void testOrdenamientoRutasTransbordo() {
			// Given: Paradas que requieren transbordos
			Parada origen = paradas.get(1);
			Parada destino = paradas.get(10);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> rutas = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Las rutas deben estar ordenadas por tiempo total incluyendo esperas
			if (rutas.size() > 1) {
				for (int i = 0; i < rutas.size() - 1; i++) {
					long tiempoActual = rutas.get(i).stream()
						.mapToLong(colectivo.logica.Recorrido::getDuracion)
						.sum();

					long tiempoSiguiente = rutas.get(i + 1).stream()
						.mapToLong(colectivo.logica.Recorrido::getDuracion)
						.sum();

					assertTrue(tiempoActual <= tiempoSiguiente,
						"Rutas con transbordo deben estar ordenadas por tiempo total");
				}
			}
		}

		@Test
		@DisplayName("Rutas caminando deben incluir tiempo de caminata en ordenamiento")
		void testOrdenamientoRutasCaminando() {
			// Given: Paradas que pueden requerir caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> rutas = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: El tiempo de caminata debe incluirse en el cálculo total
			for (List<colectivo.logica.Recorrido> ruta : rutas) {
				long tiempoTotal = ruta.stream()
					.mapToLong(colectivo.logica.Recorrido::getDuracion)
					.sum();

				assertTrue(tiempoTotal > 0, "Cada ruta debe tener tiempo total positivo");

				// Verificar que el tiempo incluye segmentos de caminata si existen
				boolean tieneCaminata = ruta.stream()
					.anyMatch(segmento -> segmento.getLinea() == null);

				if (tieneCaminata) {
					// Si tiene caminata, verificar que se incluye en el tiempo total
					long tiempoCaminata = ruta.stream()
						.filter(segmento -> segmento.getLinea() == null)
						.mapToLong(colectivo.logica.Recorrido::getDuracion)
						.sum();

					assertTrue(tiempoCaminata > 0, "El tiempo de caminata debe ser positivo");
					assertTrue(tiempoTotal >= tiempoCaminata, "El tiempo total debe incluir la caminata");
				}
			}
		}
	}

	@Nested
	@DisplayName("Consistencia del Ordenamiento")
	class SortingConsistencyTests {

		@Test
		@DisplayName("Ordenamiento debe ser determinístico para la misma entrada")
		void testOrdenamientoDeterministico() {
			// Given
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When: Ejecutar la misma consulta múltiples veces
			List<List<colectivo.logica.Recorrido>> rutas1 = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);
			List<List<colectivo.logica.Recorrido>> rutas2 = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);
			List<List<colectivo.logica.Recorrido>> rutas3 = Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Los resultados deben ser idénticos
			assertEquals(rutas1.size(), rutas2.size(), "Número de rutas debe ser consistente");
			assertEquals(rutas2.size(), rutas3.size(), "Número de rutas debe ser consistente");

			// Verificar que el orden es el mismo
			for (int i = 0; i < rutas1.size(); i++) {
				long tiempo1 = rutas1.get(i).stream().mapToLong(colectivo.logica.Recorrido::getDuracion).sum();
				long tiempo2 = rutas2.get(i).stream().mapToLong(colectivo.logica.Recorrido::getDuracion).sum();
				long tiempo3 = rutas3.get(i).stream().mapToLong(colectivo.logica.Recorrido::getDuracion).sum();

				assertEquals(tiempo1, tiempo2, "Tiempos deben ser idénticos en múltiples ejecuciones");
				assertEquals(tiempo2, tiempo3, "Tiempos deben ser idénticos en múltiples ejecuciones");
			}
		}

		@Test
		@DisplayName("Ordenamiento debe ser independiente del orden de procesamiento interno")
		void testOrdenamientoIndependiente() {
			// Given: La misma consulta en diferentes momentos
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;

			// When: Ejecutar con diferentes horas para simular diferentes condiciones
			List<List<colectivo.logica.Recorrido>> rutasManana = Calculo.calcularRecorrido(origen, destino, diaSemana, LocalTime.of(8, 0), tramos);
			List<List<colectivo.logica.Recorrido>> rutasTarde = Calculo.calcularRecorrido(origen, destino, diaSemana, LocalTime.of(18, 0), tramos);

			// Then: Cada conjunto debe estar ordenado internamente
			// (aunque los resultados pueden ser diferentes debido a las frecuencias disponibles)

			// Verificar ordenamiento interno de rutasManana
			for (int i = 0; i < rutasManana.size() - 1; i++) {
				long tiempoActual = rutasManana.get(i).stream().mapToLong(colectivo.logica.Recorrido::getDuracion).sum();
				long tiempoSiguiente = rutasManana.get(i + 1).stream().mapToLong(colectivo.logica.Recorrido::getDuracion).sum();
				assertTrue(tiempoActual <= tiempoSiguiente, "Rutas mañana deben estar ordenadas");
			}

			// Verificar ordenamiento interno de rutasTarde
			for (int i = 0; i < rutasTarde.size() - 1; i++) {
				long tiempoActual = rutasTarde.get(i).stream().mapToLong(colectivo.logica.Recorrido::getDuracion).sum();
				long tiempoSiguiente = rutasTarde.get(i + 1).stream().mapToLong(colectivo.logica.Recorrido::getDuracion).sum();
				assertTrue(tiempoActual <= tiempoSiguiente, "Rutas tarde deben estar ordenadas");
			}
		}
	}
}
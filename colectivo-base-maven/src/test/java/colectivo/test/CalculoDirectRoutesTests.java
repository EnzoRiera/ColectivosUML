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
 * Tests específicos para rutas directas en la clase Calculo.
 * Verifica el comportamiento del algoritmo cuando encuentra rutas sin transbordos.
 */
@DisplayName("Tests de Rutas Directas")
class CalculoDirectRoutesTests {

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

	@Nested
	@DisplayName("Rutas Directas Exitosas")
	class SuccessfulDirectRoutesTests {

		@Test
		@DisplayName("Debe encontrar ruta directa con línea L1I")
		void testRutaDirectaL1I() {
			// Given: Paradas conectadas por L1I
			Parada origen = paradas.get(44); // Espa�a, 1660
			Parada destino = paradas.get(47); // Espa�a, 928
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Debe encontrar L1I como opción
			assertFalse(recorridos.isEmpty());

			// Buscar la ruta con L1I
			colectivo.logica.Recorrido rutaL1I = recorridos.stream()
				.flatMap(List::stream)
				.filter(r -> r.getLinea() != null && "L1I".equals(r.getLinea().getCodigo()))
				.findFirst()
				.orElse(null);

			assertNotNull(rutaL1I, "Debe encontrar ruta con línea L1I");
			assertEquals(lineas.get("L1I"), rutaL1I.getLinea());
			assertEquals(LocalTime.of(10, 50), rutaL1I.getHoraSalida());
			assertEquals(180, rutaL1I.getDuracion()); // 3 minutos

			// Verificar paradas en orden correcto
			List<Parada> paradasRuta = rutaL1I.getParadas();
			assertEquals(3, paradasRuta.size());
			assertEquals(paradas.get(44), paradasRuta.get(0));
			assertEquals(paradas.get(43), paradasRuta.get(1));
			assertEquals(paradas.get(47), paradasRuta.get(2));
		}

		@Test
		@DisplayName("Debe encontrar ruta directa con línea L5R (con segundos)")
		void testRutaDirectaL5R() {
			// Given: Paradas conectadas por L5R
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Debe encontrar L5R como opción
			assertFalse(recorridos.isEmpty());

			// Buscar la ruta con L5R
			colectivo.logica.Recorrido rutaL5R = recorridos.stream()
				.flatMap(List::stream)
				.filter(r -> r.getLinea() != null && "L5R".equals(r.getLinea().getCodigo()))
				.findFirst()
				.orElse(null);

			assertNotNull(rutaL5R, "Debe encontrar ruta con línea L5R");
			assertEquals(lineas.get("L5R"), rutaL5R.getLinea());
			assertEquals(LocalTime.of(10, 47, 30), rutaL5R.getHoraSalida());
			assertEquals(180, rutaL5R.getDuracion()); // 3 minutos
		}

		@Test
		@DisplayName("Debe calcular correctamente duración de ruta directa")
		void testDuracionRutaDirecta() {
			// Given: Ruta directa con duración conocida
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Todas las rutas directas deben tener duración de 3 minutos (180 segundos)
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				assertEquals(1, ruta.size(), "Debe ser ruta directa");
				assertEquals(180, ruta.get(0).getDuracion(),
					"Duración debe ser 180 segundos (3 minutos)");
			}
		}
	}

	@Nested
	@DisplayName("Selección de Horarios")
	class ScheduleSelectionTests {

		@Test
		@DisplayName("Debe seleccionar siguiente frecuencia disponible después de hora llegada")
		void testSeleccionHorarioPosteriorALlegada() {
			// Given: Hora de llegada anterior a frecuencias disponibles
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 30); // Antes de las frecuencias

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Todas las horas de salida deben ser >= hora de llegada
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				assertTrue(ruta.get(0).getHoraSalida().isAfter(horaLlegada) ||
						   ruta.get(0).getHoraSalida().equals(horaLlegada),
					"Hora de salida debe ser posterior o igual a hora de llegada");
			}
		}

		@Test
		@DisplayName("Debe seleccionar frecuencias válidas cuando hay múltiples opciones")
		void testSeleccionFrecuenciaMasProxima() {
			// Given: Hora que permite elegir entre frecuencias
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 40); // Después de 10:35

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Debe encontrar rutas con frecuencias válidas
			assertFalse(recorridos.isEmpty(), "Debe encontrar rutas con frecuencias");

			// Verificar que todas las frecuencias son posteriores a la hora de llegada
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				for (colectivo.logica.Recorrido segmento : ruta) {
					if (segmento.getLinea() != null) { // Solo para segmentos con línea
						assertTrue(segmento.getHoraSalida().isAfter(horaLlegada) ||
								   segmento.getHoraSalida().equals(horaLlegada),
							"Hora de salida debe ser posterior o igual a hora de llegada");
					}
				}
			}
		}
	}

	@Nested
	@DisplayName("Validación de Datos")
	class DataValidationTests {

		@Test
		@DisplayName("Debe incluir todas las paradas intermedias en orden correcto")
		void testParadasIntermediasEnOrden() {
			// Given: Ruta que pasa por múltiples paradas
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar orden de paradas en cada ruta
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				colectivo.logica.Recorrido segmento = ruta.get(0);
				List<Parada> paradasSegmento = segmento.getParadas();

				// Verificar que origen y destino están en posiciones correctas
				assertEquals(origen, paradasSegmento.get(0), "Primera parada debe ser origen");
				assertEquals(destino, paradasSegmento.get(paradasSegmento.size() - 1),
					"Última parada debe ser destino");

				// Verificar que todas las paradas intermedias pertenecen a la línea
				Linea linea = segmento.getLinea();
				for (Parada parada : paradasSegmento) {
					assertTrue(linea.getParadas().contains(parada),
						"Todas las paradas deben pertenecer a la línea " + linea.getCodigo());
				}
			}
		}

		@Test
		@DisplayName("Debe validar que paradas existen en la línea")
		void testParadasExistenEnLinea() {
			// Given: Paradas que pertenecen a la línea
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Todas las paradas en cada ruta deben existir en la línea correspondiente
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				colectivo.logica.Recorrido segmento = ruta.get(0);
				Linea linea = segmento.getLinea();

				for (Parada parada : segmento.getParadas()) {
					assertTrue(linea.getParadas().contains(parada),
						"Parada " + parada.getCodigo() + " debe existir en línea " + linea.getCodigo());
				}
			}
		}
	}
}
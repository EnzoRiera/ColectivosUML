package colectivo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
 * Tests específicos para rutas con transbordos en la clase Calculo.
 * Verifica el comportamiento del algoritmo cuando necesita combinar múltiples líneas.
 */
@DisplayName("Tests de Rutas con Transbordos")
class CalculoTransbordoTests {

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
	@DisplayName("Estructura de Rutas con Transbordo")
	class TransbordoStructureTests {

		@Test
		@DisplayName("Ruta con transbordo debe tener exactamente dos segmentos")
		void testEstructuraRutaTransbordo() {
			// Given: Paradas que requieren transbordo
			Parada origen = paradas.get(88);
			Parada destino = paradas.get(13);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Todas las rutas deben tener exactamente 2 segmentos
			assertFalse(recorridos.isEmpty(), "Debe encontrar rutas con transbordo");

			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				assertEquals(2, ruta.size(),
					"Cada ruta con transbordo debe tener exactamente 2 segmentos");

				// Primer segmento
				colectivo.logica.Recorrido segmento1 = ruta.get(0);
				assertNotNull(segmento1.getLinea(), "Primer segmento debe tener línea asignada");
				assertFalse(segmento1.getParadas().isEmpty(), "Primer segmento debe tener paradas");

				// Segundo segmento
				colectivo.logica.Recorrido segmento2 = ruta.get(1);
				assertNotNull(segmento2.getLinea(), "Segundo segmento debe tener línea asignada");
				assertFalse(segmento2.getParadas().isEmpty(), "Segundo segmento debe tener paradas");

				// Las líneas deben ser diferentes
				assertNotEquals(segmento1.getLinea(), segmento2.getLinea(),
					"Los dos segmentos deben usar líneas diferentes");
			}
		}

		@Test
		@DisplayName("Debe conectar correctamente parada intermedia entre líneas")
		void testConexionParadaIntermedia() {
			// Given: Paradas que requieren transbordo específico
			Parada origen = paradas.get(88);
			Parada destino = paradas.get(13);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar conexión en parada intermedia
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				colectivo.logica.Recorrido segmento1 = ruta.get(0);
				colectivo.logica.Recorrido segmento2 = ruta.get(1);

				// La última parada del primer segmento debe ser la primera del segundo
				List<Parada> paradas1 = segmento1.getParadas();
				List<Parada> paradas2 = segmento2.getParadas();

				Parada paradaConexion = paradas1.get(paradas1.size() - 1);
				Parada paradaInicioSegundo = paradas2.get(0);

				assertEquals(paradaConexion, paradaInicioSegundo,
					"Última parada del primer segmento debe conectar con primera del segundo");

				// La parada de conexión debe estar en ambas líneas
				assertTrue(segmento1.getLinea().getParadas().contains(paradaConexion),
					"Parada de conexión debe estar en primera línea");
				assertTrue(segmento2.getLinea().getParadas().contains(paradaConexion),
					"Parada de conexión debe estar en segunda línea");
			}
		}
	}

	@Nested
	@DisplayName("Cálculo de Horarios en Transbordos")
	class TransbordoTimingTests {

		@Test
		@DisplayName("Debe calcular tiempo de espera entre transbordos")
		void testTiempoEsperaTransbordo() {
			// Given: Paradas que requieren transbordo
			Parada origen = paradas.get(88);
			Parada destino = paradas.get(13);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar que hay tiempo de espera entre segmentos
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				colectivo.logica.Recorrido segmento1 = ruta.get(0);
				colectivo.logica.Recorrido segmento2 = ruta.get(1);

				// Calcular hora de llegada al punto de transbordo
				LocalTime llegadaTransbordo = segmento1.getHoraSalida()
					.plusSeconds(segmento1.getDuracion());

				// La salida del segundo segmento debe ser >= llegada al transbordo
				assertTrue(segmento2.getHoraSalida().isAfter(llegadaTransbordo) ||
						   segmento2.getHoraSalida().equals(llegadaTransbordo),
					"Segundo segmento debe salir después de llegada al transbordo");

				// Si hay espera, debe ser razonable (menos de 30 minutos)
				if (segmento2.getHoraSalida().isAfter(llegadaTransbordo)) {
					long minutosEspera = java.time.Duration.between(llegadaTransbordo, segmento2.getHoraSalida()).toMinutes();
					assertTrue(minutosEspera < 30,
						"Tiempo de espera en transbordo debe ser razonable (< 30 min)");
				}
			}
		}

		@Test
		@DisplayName("Horarios deben ser consistentes a lo largo de la ruta")
		void testConsistenciaHorariosTransbordo() {
			// Given: Paradas que requieren transbordo
			Parada origen = paradas.get(88);
			Parada destino = paradas.get(13);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar consistencia temporal
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				colectivo.logica.Recorrido segmento1 = ruta.get(0);
				colectivo.logica.Recorrido segmento2 = ruta.get(1);

				// Hora de salida del primer segmento debe ser >= hora de llegada del usuario
				assertTrue(segmento1.getHoraSalida().isAfter(horaLlegada) ||
						   segmento1.getHoraSalida().equals(horaLlegada),
					"Primer segmento debe salir después de hora de llegada del usuario");

				// Hora de salida del segundo segmento debe ser >= llegada al transbordo
				LocalTime llegadaTransbordo = segmento1.getHoraSalida()
					.plusSeconds(segmento1.getDuracion());
				assertTrue(segmento2.getHoraSalida().isAfter(llegadaTransbordo) ||
						   segmento2.getHoraSalida().equals(llegadaTransbordo),
					"Segundo segmento debe salir después de llegada al transbordo");
			}
		}
	}

	@Nested
	@DisplayName("Selección de Rutas Óptimas")
	class OptimalRouteSelectionTests {

		@Test
		@DisplayName("Debe evitar rutas con transbordos innecesarios")
		void testEvitarTransbordosInnecesarios() {
			// Given: Paradas que tienen ruta directa disponible
			Parada origen = paradas.get(44);
			Parada destino = paradas.get(47);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When: Calculamos rutas
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Debe preferir rutas directas sobre transbordos cuando existen
			// Contar rutas directas vs transbordos
			long rutasDirectas = recorridos.stream()
				.filter(ruta -> ruta.size() == 1)
				.count();

			long rutasTransbordo = recorridos.stream()
				.filter(ruta -> ruta.size() == 2)
				.count();

			// Debe haber al menos una ruta directa
			assertTrue(rutasDirectas > 0,
				"Debe encontrar al menos una ruta directa cuando existe");

			// Las rutas directas deben ser preferidas (listadas primero)
			if (!recorridos.isEmpty()) {
				assertEquals(1, recorridos.get(0).size(),
					"Primera ruta debe ser directa (sin transbordo)");
			}
		}

		@Test
		@DisplayName("Debe deduplicar rutas equivalentes")
		void testDeduplicacionRutasEquivalentes() {
			// Given: Paradas que podrían generar rutas duplicadas
			Parada origen = paradas.get(88);
			Parada destino = paradas.get(13);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: No debe haber rutas duplicadas
			// Verificar que no hay dos rutas con exactamente las mismas líneas y paradas
			for (int i = 0; i < recorridos.size(); i++) {
				for (int j = i + 1; j < recorridos.size(); j++) {
					List<colectivo.logica.Recorrido> ruta1 = recorridos.get(i);
					List<colectivo.logica.Recorrido> ruta2 = recorridos.get(j);

					// Si tienen el mismo número de segmentos, verificar que no son idénticas
					if (ruta1.size() == ruta2.size()) {
						boolean rutasIdenticas = true;

						for (int k = 0; k < ruta1.size(); k++) {
							colectivo.logica.Recorrido seg1 = ruta1.get(k);
							colectivo.logica.Recorrido seg2 = ruta2.get(k);

							if (!seg1.getLinea().equals(seg2.getLinea()) ||
								!seg1.getParadas().equals(seg2.getParadas())) {
								rutasIdenticas = false;
								break;
							}
						}

						assertFalse(rutasIdenticas,
							"No debe haber rutas duplicadas con mismas líneas y paradas");
					}
				}
			}
		}
	}

	@Nested
	@DisplayName("Validación de Datos en Transbordos")
	class TransbordoDataValidationTests {

		@Test
		@DisplayName("Todas las paradas deben existir en sus respectivas líneas")
		void testParadasValidasEnTransbordo() {
			// Given: Paradas que requieren transbordo
			Parada origen = paradas.get(88);
			Parada destino = paradas.get(13);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Todas las paradas deben existir en sus líneas correspondientes
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				for (colectivo.logica.Recorrido segmento : ruta) {
					Linea linea = segmento.getLinea();
					for (Parada parada : segmento.getParadas()) {
						assertTrue(linea.getParadas().contains(parada),
							"Parada " + parada.getCodigo() + " debe existir en línea " + linea.getCodigo());
					}
				}
			}
		}

		@Test
		@DisplayName("Duraciones deben ser positivas y consistentes")
		void testDuracionesConsistentesEnTransbordo() {
			// Given: Paradas que requieren transbordo
			Parada origen = paradas.get(88);
			Parada destino = paradas.get(13);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Todas las duraciones deben ser positivas
			for (List<colectivo.logica.Recorrido> ruta : recorridos) {
				for (colectivo.logica.Recorrido segmento : ruta) {
					assertTrue(segmento.getDuracion() > 0,
						"Duración debe ser positiva para segmento en línea " + segmento.getLinea().getCodigo());
				}

				// La duración total debe ser la suma de las duraciones individuales
				// más el tiempo de espera en transbordo
				long duracionTotal = ruta.stream()
					.mapToLong(colectivo.logica.Recorrido::getDuracion)
					.sum();

				// Calcular duración total incluyendo esperas
				LocalTime salidaPrimerSegmento = ruta.get(0).getHoraSalida();
				LocalTime llegadaUltimoSegmento = ruta.get(ruta.size() - 1).getHoraSalida()
					.plusSeconds(ruta.get(ruta.size() - 1).getDuracion());

				long duracionCalculada = java.time.Duration.between(salidaPrimerSegmento, llegadaUltimoSegmento).getSeconds();

				assertTrue(duracionCalculada >= duracionTotal,
					"Duración total calculada debe ser >= suma de duraciones individuales");
			}
		}
	}
}
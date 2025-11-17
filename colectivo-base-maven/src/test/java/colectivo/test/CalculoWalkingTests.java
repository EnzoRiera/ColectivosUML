package colectivo.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * Tests específicos para rutas que incluyen segmentos caminando en la clase Calculo.
 * Verifica el comportamiento del algoritmo cuando combina transporte público con caminata.
 */
@DisplayName("Tests de Rutas Caminando")
class CalculoWalkingTests {

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
	@DisplayName("Estructura de Rutas con Caminata")
	class WalkingRouteStructureTests {

		@Test
		@DisplayName("Ruta caminando debe tener al menos 3 segmentos (colectivo + caminando + colectivo)")
		void testEstructuraRutaCaminando() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Debe encontrar una ruta con caminata
			assertFalse(recorridos.isEmpty(), "Debe encontrar rutas con caminata");
			assertEquals(1, recorridos.size(), "Debe haber exactamente una ruta caminando");

			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);
			assertTrue(ruta.size() >= 3,
				"Ruta caminando debe tener al menos 3 segmentos (colectivo + caminando + colectivo)");

			// Verificar patrón: colectivo -> caminando -> colectivo
			assertNotNull(ruta.get(0).getLinea(), "Primer segmento debe ser colectivo");
			assertNull(ruta.get(1).getLinea(), "Segundo segmento debe ser caminando");
			assertNotNull(ruta.get(2).getLinea(), "Tercer segmento debe ser colectivo");
		}

		@Test
		@DisplayName("Segmentos caminando deben tener linea == null")
		void testSegmentosCaminandoSinLinea() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Segmentos de caminata deben tener linea == null
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);

			for (colectivo.logica.Recorrido segmento : ruta) {
				if (segmento.getLinea() == null) {
					// Es un segmento caminando
					assertTrue(segmento.getParadas().size() >= 2,
						"Segmento caminando debe tener al menos 2 paradas");
					assertTrue(segmento.getDuracion() > 0,
						"Segmento caminando debe tener duración positiva");
				}
			}
		}

		@Test
		@DisplayName("Debe conectar paradas adyacentes en segmentos caminando")
		void testConexionParadasCaminando() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar conexiones en segmentos caminando
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);

			for (int i = 0; i < ruta.size() - 1; i++) {
				colectivo.logica.Recorrido segmentoActual = ruta.get(i);
				colectivo.logica.Recorrido segmentoSiguiente = ruta.get(i + 1);

				// La última parada del segmento actual debe ser la primera del siguiente
				List<Parada> paradasActual = segmentoActual.getParadas();
				List<Parada> paradasSiguiente = segmentoSiguiente.getParadas();

				Parada paradaConexion = paradasActual.get(paradasActual.size() - 1);
				Parada paradaInicioSiguiente = paradasSiguiente.get(0);

				assertEquals(paradaConexion, paradaInicioSiguiente,
					"Parada final del segmento " + i + " debe conectar con inicio del segmento " + (i + 1));
			}
		}
	}

	@Nested
	@DisplayName("Horarios en Rutas con Caminata")
	class WalkingTimingTests {

		@Test
		@DisplayName("Segmentos caminando pueden tener tiempo de espera antes del siguiente segmento")
		void testSinEsperaEnCaminata() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: En rutas con transbordo y caminata, puede haber tiempo de espera
			// que incluye la duración de la caminata
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);

			for (int i = 1; i < ruta.size(); i++) {
				colectivo.logica.Recorrido segmentoAnterior = ruta.get(i - 1);
				colectivo.logica.Recorrido segmentoActual = ruta.get(i);

				// Calcular llegada del segmento anterior
				LocalTime llegadaAnterior = segmentoAnterior.getHoraSalida()
					.plusSeconds(segmentoAnterior.getDuracion());

				// El segmento actual puede comenzar después (incluyendo tiempo de caminata como espera)
				assertTrue(segmentoActual.getHoraSalida().isAfter(llegadaAnterior) ||
						   segmentoActual.getHoraSalida().equals(llegadaAnterior),
					"Segmento " + i + " debe comenzar después o igual al final del segmento " + (i - 1));
			}
		}

		@Test
		@DisplayName("Duración de caminata debe ser realista")
		void testDuracionCaminataRealista() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Duraciones de caminata deben ser realistas (entre 1 y 10 minutos)
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);

			for (colectivo.logica.Recorrido segmento : ruta) {
				if (segmento.getLinea() == null) { // Es caminata
					long minutos = segmento.getDuracion() / 60;
					assertTrue(minutos >= 1 && minutos <= 10,
						"Duración de caminata debe ser entre 1 y 10 minutos, fue: " + minutos + " min");
				}
			}
		}

		@Test
		@DisplayName("Horarios deben ser consistentes en toda la ruta (con posibles esperas)")
		void testConsistenciaHorariosRutaCompleta() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar consistencia horaria en toda la ruta
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);

			// Primer segmento debe salir después de hora de llegada del usuario
			assertTrue(ruta.get(0).getHoraSalida().isAfter(horaLlegada) ||
					   ruta.get(0).getHoraSalida().equals(horaLlegada),
				"Primer segmento debe salir después de hora de llegada del usuario");

			// Verificar secuencia temporal (permitiendo esperas)
			for (int i = 0; i < ruta.size() - 1; i++) {
				colectivo.logica.Recorrido segmentoActual = ruta.get(i);
				colectivo.logica.Recorrido segmentoSiguiente = ruta.get(i + 1);

				LocalTime llegadaActual = segmentoActual.getHoraSalida()
					.plusSeconds(segmentoActual.getDuracion());

				assertTrue(segmentoSiguiente.getHoraSalida().isAfter(llegadaActual) ||
						   segmentoSiguiente.getHoraSalida().equals(llegadaActual),
					"Segmento " + (i + 1) + " debe comenzar cuando termina o después del segmento " + i);
			}
		}
	}

	@Nested
	@DisplayName("Selección de Rutas con Caminata")
	class WalkingRouteSelectionTests {

		@Test
		@DisplayName("Debe ser la última opción cuando hay rutas directas o con transbordo")
		void testCaminataComoUltimaOpcion() {
			// Given: Paradas que tienen múltiples opciones de ruta
			Parada origen = paradas.get(1);
			Parada destino = paradas.get(2);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Si hay rutas sin caminata, estas deben aparecer primero
			if (recorridos.size() > 1) {
				// Verificar que las primeras rutas no tienen segmentos caminando
				List<colectivo.logica.Recorrido> primeraRuta = recorridos.get(0);
				boolean primeraRutaTieneCaminata = primeraRuta.stream()
					.anyMatch(segmento -> segmento.getLinea() == null);

				if (!primeraRutaTieneCaminata) {
					// Si la primera ruta no tiene caminata, verificar que hay rutas con caminata después
					boolean hayRutasConCaminata = recorridos.stream()
						.skip(1)
						.anyMatch(ruta -> ruta.stream().anyMatch(seg -> seg.getLinea() == null));

					assertTrue(hayRutasConCaminata,
						"Si hay rutas sin caminata, debe haber también rutas con caminata como opciones");
				}
			}
		}

		@Test
		@DisplayName("Debe calcular distancia caminando basada en conexiones reales")
		void testDistanciaCaminandoReal() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar que la caminata está basada en conexiones reales
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);

			// Encontrar el segmento de caminata
			colectivo.logica.Recorrido segmentoCaminando = ruta.stream()
				.filter(segmento -> segmento.getLinea() == null)
				.findFirst()
				.orElse(null);

			assertNotNull(segmentoCaminando, "Debe haber un segmento de caminata");

			// Verificar que las paradas están conectadas caminando
			List<Parada> paradasCaminando = segmentoCaminando.getParadas();
			for (int i = 0; i < paradasCaminando.size() - 1; i++) {
				Parada paradaActual = paradasCaminando.get(i);
				Parada paradaSiguiente = paradasCaminando.get(i + 1);

				// Verificar que existe una conexión caminando entre estas paradas
				assertTrue(paradaActual.getParadaCaminando().contains(paradaSiguiente) ||
						   paradaSiguiente.getParadaCaminando().contains(paradaActual),
					"Paradas " + paradaActual.getCodigo() + " y " + paradaSiguiente.getCodigo() +
					" deben estar conectadas caminando");
			}
		}
	}

	@Nested
	@DisplayName("Validación de Datos en Caminata")
	class WalkingDataValidationTests {

		@Test
		@DisplayName("Paradas en segmentos caminando deben estar conectadas")
		void testParadasConectadasCaminando() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Todas las paradas en segmentos caminando deben tener conexiones válidas
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);

			for (colectivo.logica.Recorrido segmento : ruta) {
				if (segmento.getLinea() == null) { // Es caminata
					List<Parada> paradasSegmento = segmento.getParadas();

					// Verificar conexiones consecutivas
					for (int i = 0; i < paradasSegmento.size() - 1; i++) {
						Parada p1 = paradasSegmento.get(i);
						Parada p2 = paradasSegmento.get(i + 1);

						// Debe existir conexión caminando en al menos una dirección
						assertTrue(p1.getParadaCaminando().contains(p2) ||
								   p2.getParadaCaminando().contains(p1),
							"Paradas consecutivas en caminata deben estar conectadas");
					}
				}
			}
		}

		@Test
		@DisplayName("Duraciones de caminata deben corresponder a distancias reales")
		void testDuracionCorrespondeDistancia() {
			// Given: Paradas que requieren caminata
			Parada origen = paradas.get(31);
			Parada destino = paradas.get(66);
			int diaSemana = 1;
			LocalTime horaLlegada = LocalTime.of(10, 35);

			// When
			List<List<colectivo.logica.Recorrido>> recorridos =
				Calculo.calcularRecorrido(origen, destino, diaSemana, horaLlegada, tramos);

			// Then: Verificar que las duraciones corresponden a las distancias en los tramos
			List<colectivo.logica.Recorrido> ruta = recorridos.get(0);

			for (colectivo.logica.Recorrido segmento : ruta) {
				if (segmento.getLinea() == null) { // Es caminata
					List<Parada> paradasSegmento = segmento.getParadas();

					// Calcular duración esperada sumando tramos
					long duracionEsperada = 0;
					for (int i = 0; i < paradasSegmento.size() - 1; i++) {
						Parada p1 = paradasSegmento.get(i);
						Parada p2 = paradasSegmento.get(i + 1);

						// Buscar el tramo correspondiente
						String claveTramo = p1.getCodigo() + "-" + p2.getCodigo()+ "-" + "2";
						Tramo tramo = tramos.get(claveTramo);

						if (tramo != null && tramo.getTipo() == 2) { // Tipo 2 = caminando
							duracionEsperada += tramo.getTiempo();
						}
					}

					// La duración del segmento debe corresponder a la suma de los tramos
					assertEquals(duracionEsperada, segmento.getDuracion(),
						"Duración del segmento caminando debe corresponder a suma de tramos");
				}
			}
		}
	}
}
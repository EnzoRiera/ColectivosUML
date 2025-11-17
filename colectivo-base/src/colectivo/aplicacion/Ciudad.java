package colectivo.aplicacion;

import java.util.Map;
import java.util.TreeMap;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.servicio.LineaService;
import colectivo.servicio.LineaServiceImpl;
import colectivo.servicio.ParadaService;
import colectivo.servicio.ParadaServiceImpl;
import colectivo.servicio.TramoService;
import colectivo.servicio.TramoServiceImpl;

// Ciudad Con Patron Singleton (si se modifica el patron podriamos tener varias ciudades)
public class Ciudad {

	// Instancia Singleton -> Bandera
	private static Ciudad ciudad = null;
	private Coordinador coordinador;
	private String nombre;
	private Map<String, Linea> lineas;
	private Map<Integer, Parada> paradas;
	private Map<String, Tramo> tramos;
	private LineaService lineaService;
	private ParadaService paradaService;
	private TramoService tramoService;

	public static Ciudad getCiudad() throws Exception {
		if (ciudad == null) {
			ciudad = new Ciudad();
		}
		return ciudad;
	}

	private Ciudad() throws Exception {
		super();

		// 1. Crear UNA SOLA instancia de ParadaService
		paradaService = new ParadaServiceImpl();

		// 2. Cargar paradas PRIMERO
		paradas = new TreeMap<Integer, Parada>(paradaService.buscarTodos());

		// 3. Crear servicios de Linea y Tramo que usen las mismas paradas
		lineaService = new LineaServiceImpl();
		tramoService = new TramoServiceImpl();

		// 4. Cargar lineas y tramos
		lineas = new TreeMap<String, Linea>(lineaService.buscarTodos());
		tramos = new TreeMap<String, Tramo>(tramoService.buscarTodos());

	}

	// Atributos No Seteados en el cosntructor
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setCoordinador(Coordinador coordinador) {
		this.coordinador = coordinador;
	}

	// Getters de las estructuras de datos
	public Map<String, Linea> getLineas() {
		return lineas;
	}

	public Map<Integer, Parada> getParadas() {
		return paradas;
	}

	public Map<String, Tramo> getTramos() {
		return tramos;
	}
}

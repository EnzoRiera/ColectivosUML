package colectivo.aplicacion;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import colectivo.interfaz.Formateador;
import colectivo.logica.Calculo;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;
import colectivo.servicio.InterfazService;
import colectivo.servicio.InterfazServiceImpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Coordinador {

    private static final Logger logger = LogManager.getLogger(Coordinador.class);

	private Calculo calculo;


	private Ciudad ciudad;


	private Configuracion configuracion;


	private InterfazService interfazService;


	List<List<Recorrido>> recorridosSolucion;


	public List<List<Recorrido>> buscarRecorridos(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada) {
	    // Validaciones con log
	    if (paradaOrigen == null) {
	        logger.error("buscarRecorridos: paradaOrigen es null");
	        throw new NullPointerException("paradaOrigen no puede ser null");
	    }
	    if (paradaDestino == null) {
	        logger.error("buscarRecorridos: paradaDestino es null");
	        throw new NullPointerException("paradaDestino no puede ser null");
	    }
	    if (horaLlegaParada == null) {
	        logger.error("buscarRecorridos: horaLlegaParada es null");
	        throw new NullPointerException("horaLlegaParada no puede ser null");
	    }
	    if (ciudad == null) {
	        logger.error("buscarRecorridos: Ciudad no inicializada");
	        throw new IllegalStateException("Ciudad no está inicializada");
	    }

		recorridosSolucion = Calculo.calcularRecorrido(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada,
				mapearTramos());
		return recorridosSolucion;
	}

	public Calculo getCalculo() {
		return calculo;
	}

	public Ciudad getCiudad() {
		return ciudad;
	}

	public Configuracion getConfiguracion() {
		return configuracion;
	}

	public InterfazService getInterfaz() {
		return interfazService;
	}

	public ResourceBundle getResourceBundle() {
		if (configuracion == null) {
			logger.error("getResourceBundle: Configuracion no inicializada, devolviendo null");
			return null;
		}
		return configuracion.getResourceBundle();
	}

    public void iniciarConsulta() {
        if (interfazService == null) {
            logger.error("iniciarConsulta: Interfaz no inicializada");
            throw new NullPointerException("Interfaz not initialized");
        }
        interfazService.iniciarInterfaz();
    }

	public Map<String, Linea> mapearLineas() {
		if (ciudad == null) {
			logger.error("mapearLineas: Ciudad no inicializada");
			throw new NullPointerException("Ciudad not initialized");
		}
		return ciudad.getLineas();
	}

	public Map<Integer, Parada> mapearParadas() {
		if (ciudad == null) {
			logger.error("mapearParadas: Ciudad no inicializada");
			throw new NullPointerException("Ciudad not initialized");
		}
		return ciudad.getParadas();
	}

	public Map<String, Tramo> mapearTramos() {
		if (ciudad == null) {
			logger.error("mapearTramos: Ciudad no inicializada");
			throw new NullPointerException("Ciudad not initialized");
		}
		return ciudad.getTramos();
	}

	public void setInterfaz(InterfazService interfazService) {
		this.interfazService = interfazService;
	}

	public Coordinador() {
		// Instantiate components Singletons?
	}

	public void inicializarAplicacion() {

		// Inject configuration into the coordinator and vice versa
		configuracion = Configuracion.getConfiguracion();
		configuracion.setCoordinador(this);
		try {
			ciudad = Ciudad.getCiudad();
			ciudad.setCoordinador(this);
		} catch (Exception e) {
		    logger.error("Error al inicializar ciudad", e);
		    throw new RuntimeException("No se pudo inicializar la ciudad", e);
		} // Singleton

		calculo = new Calculo();
		calculo.setCoordinador(this);

		// Creo la Interfaz via Service->(Factory)
		// 1. Crear UNA SOLA instancia de ParadaService
		interfazService = new InterfazServiceImpl();

		this.setInterfaz(interfazService);

		// Inject coordinator into other components
		interfazService.setCoordinador(this);

		// Inject ResourceBundle into Formateador for internationalization
		Formateador.setResourceBundle(configuracion.getResourceBundle());

	}

}

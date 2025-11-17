package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Coordinador;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Calculo {

    private static final Logger logger = LogManager.getLogger(Calculo.class);

	private Coordinador coordinador;

    public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
                                                          LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

//    	
//    	
//    	
//        if (paradaOrigen == null || paradaDestino == null || horaLlegaParada == null) {
//            logger.error("paradaOrigen, paradaDestino y horaLlegaParada no pueden ser null");
//            throw new IllegalArgumentException("paradaOrigen, paradaDestino y horaLlegaParada no pueden ser null");
//        }
//        if (tramos == null) {
//            logger.error("tramos no puede ser null");
//            throw new IllegalArgumentException("tramos no puede ser null");
//        }
//        if (diaSemana < 1 || diaSemana > 7) {
//            logger.error("diaSemana fuera de rango [1..7]: " + diaSemana);
//            throw new IllegalArgumentException("diaSemana debe estar entre 1 y 7");
//        }
        
        logger.info("Iniciando cálculo de recorrido desde " + paradaOrigen.getDireccion() +
				" hasta " + paradaDestino.getDireccion() +
				" para el día " + diaSemana +
				" llegando a las " + horaLlegaParada);

        try {
            Map<Parada, List<Tramo>> conexionesParadas = CalculosAuxiliares.conexionesParadas(tramos);

            List<EstrategiaBusqueda> estrategias = List.of(
                    new BusquedaDirecta(),
                    new BusquedaConTransbordo(),
                    new BusquedaCaminando()
            );

            for (EstrategiaBusqueda estrategia : estrategias) {
                List<List<Recorrido>> soluciones = estrategia.buscar(paradaOrigen, paradaDestino, diaSemana,
                        horaLlegaParada, conexionesParadas, tramos);

                if (!soluciones.isEmpty()) {
                	
//                	logger.info(soluciones);
                	
                	return soluciones;
                	                         
                }
                	
            }

            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error inesperado durante el cálculo de recorridos", e);
            return new ArrayList<>();
        }
    }

	public void setCoordinador(Coordinador coordinador) {
        if (coordinador == null) {
            logger.error("coordinador no puede ser null");
            throw new IllegalArgumentException("coordinador no puede ser null");
        }
        this.coordinador = coordinador;
    }
}
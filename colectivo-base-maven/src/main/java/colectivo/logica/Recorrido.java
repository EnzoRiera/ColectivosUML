package colectivo.logica;

import java.time.LocalTime;
import java.util.List;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;

/**
 * Representa un recorrido específico en una línea de transporte.
 * Un recorrido está formado por una línea, una secuencia de paradas por las que pasa,
 * una hora de salida y la duración total del trayecto en minutos.
 * Se utiliza para modelar una solución de viaje que puede ser parte de una ruta más compleja.
 */
public class Recorrido {

	private Linea linea;
	private List<Parada> paradas;
	private LocalTime horaSalida;
	private int duracion;

	public Recorrido(Linea linea, List<Parada> paradas, LocalTime horaSalida, int duracion) {
		super();
		this.linea = linea;
		this.paradas = paradas;
		this.horaSalida = horaSalida;
		this.duracion = duracion;
	}

	public Linea getLinea() {
		return linea;
	}

	public void setLinea(Linea linea) {
		this.linea = linea;
	}

	public List<Parada> getParadas() {
		return paradas;
	}

	public void setParadas(List<Parada> paradas) {
		this.paradas = paradas;
	}

	public LocalTime getHoraSalida() {
		return horaSalida;
	}

	public void setHoraSalida(LocalTime horaSalida) {
		this.horaSalida = horaSalida;
	}

	public int getDuracion() {
		return duracion;
	}

	public void setDuracion(int duracion) {
		this.duracion = duracion;
	}

}

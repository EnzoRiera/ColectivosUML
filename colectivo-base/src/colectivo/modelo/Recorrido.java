package colectivo.modelo;

import java.time.LocalTime;
import java.util.List;

/**
 * Represents a scheduled run or trip ("Recorrido") of a transit line.
 *
 * <p>This model class encapsulates the association of a {@link Linea} with an
 * ordered list of stops ({@code paradas}), the scheduled departure time
 * ({@code horaSalida}) and the total duration in minutes ({@code duracion}).
 * It is a lightweight DTO used by application logic to represent a specific
 * traversal of a line at a given time.</p>
 *
 * <p>Equality and other behaviors are not overridden here; this is the original
 * version provided by the course (cátedra) and has not been modified.</p>
 *
 * @author POO-2025
 * @version 1.0
 * @since 1.0
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

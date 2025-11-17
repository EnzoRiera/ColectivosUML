package colectivo.modelo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transit line ("Linea") in the transit domain.
 *
 * <p>This model class stores identifying and descriptive information about a transit
 * line: a unique code ({@code codigo}), a human-readable name ({@code nombre}),
 * the ordered list of stops that the line serves ({@code paradas}), and the scheduled
 * frequencies ({@code frecuencias}). The helper method {@link #getHorasFrecuencia(int)}
 * is provided to obtain the frequency times for a specific day of the week
 * (1 = Monday, ..., 7 = Sunday) for business logic needs.</p>
 *
 * <p>Equality and hash code computations are based solely on the line code
 * ({@code codigo}). This implementation is based on the original version provided
 * by the course (cátedra) and includes the additional {@code getHorasFrecuencia}
 * method used by application logic.</p>
 *
 * @author Poo-2025
 * @version 1.0
 * @since 1.0
 */
public class Linea {

	private String codigo;
	private String nombre;
	private List<Parada> paradas;
	private List<Frecuencia> frecuencias;

	public Linea() {
		this.paradas = new ArrayList<Parada>();
		this.frecuencias = new ArrayList<Frecuencia>();
	}

	public Linea(String codigo, String nombre) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.paradas = new ArrayList<Parada>();
		this.frecuencias = new ArrayList<Frecuencia>();
	}

	// Agrega una parada a la línea y también agrega esta línea a la parada
	public void agregarParada(Parada parada) {
		paradas.add(parada);
		parada.agregarLinea(this);
	}

	public void agregarFrecuencia(int diaSemana, LocalTime hora) {
		frecuencias.add(new Frecuencia(diaSemana, hora));
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Parada> getParadas() {
		return paradas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Linea other = (Linea) obj;
		if (codigo == null) {
			if (other.codigo != null)
				return false;
		} else if (!codigo.equals(other.codigo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Linea [codigo=" + codigo + ", nombre=" + nombre + "]";
	}

	/**
	 * Returns the list of frequency times for a specific day of the week.
	 *
	 * <p>
	 * The parameter `diaSemana` follows the convention 1 = Monday, ..., 7 = Sunday.
	 * The method collects and returns all {@link java.time.LocalTime} instances
	 * registered for the requested day.
	 * </p>
	 *
	 * @param diaSemana day of the week (1=Monday, ..., 7=Sunday)
	 * @return a list of {@link java.time.LocalTime} objects for frequencies on that day
	 */
	public List<LocalTime> getHorasFrecuencia(int diaSemana) {
		List<LocalTime> horas = new ArrayList<>();
		for (Frecuencia frecuencia : frecuencias) {
			if (frecuencia.getDiaSemana() == diaSemana) {
				horas.add(frecuencia.getHora());
			}
		}
		return horas;
	}

	private class Frecuencia {

		private int diaSemana;
		private LocalTime hora;

		public Frecuencia(int diaSemana, LocalTime hora) {
			super();
			this.diaSemana = diaSemana;
			this.hora = hora;
		}

		public int getDiaSemana() {
			return diaSemana;
		}

		public void setDiaSemana(int diaSemana) {
			this.diaSemana = diaSemana;
		}

		public LocalTime getHora() {
			return hora;
		}

		public void setHora(LocalTime hora) {
			this.hora = hora;
		}

		@Override
		public String toString() {
			return "Frecuencia [diaSemana=" + diaSemana + ", hora=" + hora + "]";
		}

	}
}

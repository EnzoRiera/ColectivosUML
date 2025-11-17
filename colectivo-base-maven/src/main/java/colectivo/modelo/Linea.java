package colectivo.modelo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una línea de transporte público (colectivo/autobús).
 * Una línea tiene un código identificador, un nombre, una secuencia ordenada de paradas
 * por las que pasa, y frecuencias que indican los horarios de salida según el día de la semana.
 * Las paradas se agregan en orden secuencial formando el recorrido de la línea.
 */
public class Linea {

	private String codigo;
	private String nombre;
	private final List<Parada> paradas;
	private final List<Frecuencia> frecuencias;

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
	 * Obtiene todas las horas de frecuencia (horarios de salida) de esta línea para un día específico.
	 * Filtra las frecuencias según el día de la semana y retorna solo las horas correspondientes.
	 *
	 * @param diaSemana el día de la semana (1=lunes, 2=martes, ..., 7=domingo)
	 * @return lista de horarios (LocalTime) en los que sale esta línea en el día especificado
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

	/**
	 * Clase interna que representa una frecuencia de salida de la línea.
	 * Asocia un día de la semana con una hora específica en la que la línea realiza una salida.
	 * Se utiliza para modelar los horarios del transporte según el día.
	 */
	private static class Frecuencia {

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

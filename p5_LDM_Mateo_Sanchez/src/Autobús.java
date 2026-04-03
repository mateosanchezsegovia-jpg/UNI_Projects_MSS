public class Autobús {
	private String matricula;
	private int capacidad;

	public String getMatricula() {
		return matricula;		
	}

	public void setMatricula(String aMatricula) {
		this.matricula = aMatricula;
	}

	public int getCapacidad() {
		return capacidad; // Devolvemos el valor
	}

	public void setCapacidad(int aCapacidad) {
		this.capacidad = aCapacidad;
	}
	
	@Override
	public String toString() {
	    return "Autobus [Matricula=" + matricula + ", Capacidad=" + capacidad + "]";
	}
}
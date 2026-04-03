
public class Carta {
	
	private int valor;
	private String letra;
	
	public Carta(int valor, String letra) {
		this.valor=valor;
		this.letra=letra;
	}

	public String getIdentificador() {
		
		return (valor + letra);
	}
	
	public boolean sePuedeAplicarSobre(Carta otraCarta) {
		boolean a=false;
		Carta b=otraCarta;
		if(this.valor==b.valor||b.letra.equals(letra))
			a=true;
		return a;
	}
	}



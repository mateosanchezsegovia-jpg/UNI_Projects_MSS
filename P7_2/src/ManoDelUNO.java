import java.util.*;

public class ManoDelUNO {
	
	private Set <Carta> mano;

	
	public ManoDelUNO() {
		mano = new HashSet<Carta>();
	}
	
	
	public boolean estáVacía() {

		return mano.isEmpty();
	}
	
	
	public boolean agregarCarta(Carta carta) {

		return mano.add(carta);
			
	}
	
	public Carta extraercartaApilableSobre(Carta cartaSobreLaQueHayQueApilar) {
		Carta a;
		Carta res=null;
		Iterator<Carta> it = mano.iterator();
		
		while (it.hasNext()) {
			a = it.next();
			if (a.sePuedeAplicarSobre(cartaSobreLaQueHayQueApilar)) {
				res=a;
				it.remove();
			}
			
		}
		return res;
		
	}
	
	public String getMano() {
		String a="";
		
		for(Carta c : mano) {
			
				a=a+" "+c.getIdentificador();
				
		}
		
		if(estáVacía()) {
			a="Sin cartas";
		}
		
		
		return a;
	}

}

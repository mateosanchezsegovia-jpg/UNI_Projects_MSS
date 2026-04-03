
public class JugadorDelUNO {
	private final String nombre;
	private ManoDelUNO mano;
	
	public JugadorDelUNO (String nombre) {
		this.nombre = nombre;
		mano = new ManoDelUNO();
	}
	
	public String getNombre () {
		return nombre;
	}
	
	public boolean sinCartasEnLaMano () {
		return mano.estáVacía();
	}


public void cogeCartas(PilaDeCartas cartasParaCoger,int numeroDeCartasACoger) {
	int numCartasRobadas =0;
	Boolean aux;
	while (cartasParaCoger.hayCartasDisponibles() && numCartasRobadas<numeroDeCartasACoger) {
		aux = mano.agregarCarta(cartasParaCoger.extraerCartaParteSuperior());
		if (aux){	
			numCartasRobadas++;
		}
	}
}
	
	
	public void juega (PilaDeCartas cartasParaCoger,
			PilaDeCartas cartasTiradas) {
		//1.- Obtener la refencia de la ultima carta tirada
		Carta ultimaCartaTirada = cartasTiradas.verCartaSuperior();
		//System.out.println("\tHay que sacar carta para un "+ultimaCartaTirada.getIdentificador());
		//System.out.println("\tMi mano es "+this.mano.getMano());
		//2.- Comprobar si el jugador tiene carta para tirar
		Carta cartaParaTirar = mano.extraercartaApilableSobre(ultimaCartaTirada);
		
		//3.- Si el jugador no tiene en su mano una carta para tirar.
		Carta cartaRobada = null;
		while (cartaParaTirar == null || mano.getMano().contains(cartaRobada.getIdentificador())) {
			//System.out.println("\tNo tengo carta válida en la mano, cojo carta...");
			cogeCartas(cartasParaCoger, 1);
			cartaRobada = cartasParaCoger.extraerCartaParteSuperior();
			mano.agregarCarta(cartaRobada);
			cartaParaTirar = mano.extraercartaApilableSobre(ultimaCartaTirada);
			if (cartaParaTirar !=null) {
				//System.out.println("\tTengo una carta válida en la mano "+ cartaParaTirar.getIdentificador());
				cartasTiradas.agregarCarta(cartaParaTirar);
			} else {
				//System.out.println("\t¡Tampoco puedo ugar tras coger una carta!");
			}
		//}else {
			
			//System.out.println("\tTengo una carta válida en la mano "+ cartaParaTirar.getIdentificador());
			//cartasTiradas.agregarCarta(cartaParaTirar);
		}
		
		
	}
	
}

/*public void juega (PilaDeCartas cartasParaCoger, PilaDeCartas cartasTiradas) {
	Carta cartaBocaArriba =  cartasTiradas.verCartaSuperior();
	System.out.println("\tHay que sacar carta para un "+cartaBocaArriba.getIdentificador());
	Carta cartaATirar = mano.extraercartaApilableSobre(cartaBocaArriba);
	System.out.println("\tMi mano es "+this.mano.getMano());
	//&& cartasParaCoger.hayCartasDisponibles()
	if (cartaATirar == null ) {
		System.out.println("\tNo tengo carta válida en la mano, cojo carta...");
		cogeCartas (cartasParaCoger, 1);
		cartaATirar = mano.extraercartaApilableSobre(cartaBocaArriba);
		if (cartaATirar !=null) {
			System.out.println("\tTengo una carta válida en la mano "+ cartaATirar.getIdentificador());
			cartasTiradas.agregarCarta(cartaATirar);
		}
	}else if (cartaATirar !=null){
		System.out.println("\tTengo una carta válida en la mano "+ cartaATirar.getIdentificador());
		cartasTiradas.agregarCarta(cartaATirar);
	}

}
****/

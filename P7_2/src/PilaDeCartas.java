import java.util.*;
public class PilaDeCartas {
	
	private List<Carta>pila;
	

	public PilaDeCartas() {
		pila = new ArrayList<Carta>();
	}

public boolean hayCartasDisponibles() {
	return pila.size()>0;
}
public void agregarCarta(Carta a) {
	pila.add(a);
	
}
public Carta extraerCartaParteSuperior() {
	
	return pila.remove(pila.size()-1);
}
public Carta verCartaSuperior() {
	return pila.get(pila.size()-1);
}
	

public void barajar (){
	
	int index;
	Carta aux; 
    Random random = new Random();
 
    for (int i = pila.size() - 1; i > 0; i--){
        index = random.nextInt(i + 1);//Genera un numero aleatorio 
        if (index != i){
        	aux = pila.get(i); //carta4
            pila.add(i, pila.get(index));
            pila.add(index, aux);
        }
    }	
}
}
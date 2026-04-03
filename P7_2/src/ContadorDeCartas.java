
import java.util.HashMap;
import java.util.Map;

/**
 * Esta clase contiene el m�todo main que el alumno debe rellenar para
 * contabilizar las apariciones de cada carta en el array 'cartas' utilizando un
 * mapa para su posterior representaci�n como histograma en la salida j�ndar.
 *
 * @author DTE.
 * @version 1.0
 */
/**
 * @author mateosanchezsegovia
 *
 */
/**
 * @author mateosanchezsegovia
 *
 */
/**
 * @author mateosanchezsegovia
 *
 */
/**
 * @author mateosanchezsegovia
 *
 */
/**
 * @author mateosanchezsegovia
 *
 */
/**
 * @author mateosanchezsegovia
 *
 */
/**
 * @author mateosanchezsegovia
 *
 */
public class ContadorDeCartas {

	private ContadorDeCartas() {}

	/**
	 * Este m�todo implementa un contador de cartas y muestra en la terminal de
	 * texto un histograma con la distribuci�n correspondiente.
	 *
	 * @param args Argumentos recibidos en la l�nea de mandatos (no usados).
	 */
	public static void main(String[] args) {
		Carta[] cartas = UtilidadesPractica7.cartasAlAzarDistribucionNormal(100000);
		Map<String, Integer> mapa = new HashMap<String, Integer>();// Rellenar
		int n=0;
		for (Carta carta : cartas) {
			if (!mapa.containsKey(carta.getIdentificador()) ){
				mapa.put(carta.getIdentificador(), 1);
			}else {
				n=mapa.get(carta.getIdentificador());
				n++;
				mapa.put(carta.getIdentificador(), n);
			}
		}
		
		// Contabilizar cada carta usando el mapa

		UtilidadesPractica7.representarApariciones(mapa);
	}

}



import java.util.Map;
import java.util.Random;

/**
 * Esta clase contiene m�todos necesarios para la realizaci�n de la pr�ctica 7.
 *
 * @author DTE.
 * @version 1.0
 */
public final class UtilidadesPractica7 {
	private static Carta[] baraja = null;

	private UtilidadesPractica7() {
	}

	/**
	 * Devuelve un array de tama�o seleccionable de cartas de UNO con cartas
	 * elegidas al azar con <a href=
	 * "https://es.wikipedia.org/wiki/Distribuci%C3%B3n_uniforme_discreta">distribuci�n
	 * uniforme</a>.
	 *
	 * @param numeroDeCartas N�mero de cartas que se desea obtener.
	 * @return Array de 'numeroDeCartas' cartas de UNO con cartas elegidas al azar
	 */
	public static Carta[] cartasAlAzarDistribucionUniforme(int numeroDeCartas) {
		crearBaraja();
		final Carta[] cartas = new Carta[numeroDeCartas];
		Random r = new Random(System.currentTimeMillis());
		for (int i = 0; i < cartas.length; i++)
			cartas[i] = baraja[r.nextInt(baraja.length)];
		return cartas;
	}

	/**
	 * Devuelve un array de tama�o seleccionable de cartas de UNO con cartas
	 * elegidas al azar con
	 * <a href="https://es.wikipedia.org/wiki/Distribuci%C3%B3n_normal">distribuci�n
	 * normal</a>.
	 *
	 * @param numeroDeCartas N�mero de cartas que se desea obtener.
	 * @return Array de 'numeroDeCartas' cartas de UNO con cartas elegidas al azar
	 */
	public static Carta[] cartasAlAzarDistribucionNormal(int numeroDeCartas) {
		crearBaraja();
		final Carta[] cartas = new Carta[numeroDeCartas];
		Random r = new Random(System.currentTimeMillis());
		final int longitud = baraja.length;
		for (int i = 0; i < cartas.length; i++) {
			final double n = r.nextGaussian() * longitud / 7 + longitud / 2;
			int idx = (int) n;
			if (idx < 0)
				idx = 0;
			else if (idx >= longitud)
				idx = longitud - 1;
			cartas[i] = baraja[idx];
		}
		return cartas;
	}

	private static void crearBaraja() {
		if (baraja == null) {
			int i = 0;
			baraja = new Carta[36];
			for (int valor : new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
				for (String car : new String[] { "A", "B", "C", "D" })
					baraja[i++] = new Carta(valor, car);
		}
	}

	/**
	 * Este m�todo dibuja un histograma de las apariciones de cada carta en la
	 * salida est�ndar.
	 *
	 * @param mapa Mapa en que a cada clave textual correspondiente con el
	 *             identificador de una carta se asocia un contador de apariciones
	 *             de esa carta. Si un identificador de carta no aparece como clave
	 *             en el mapa, se consideran que ha aparecido 0 veces.
	 */
	public static void representarApariciones(Map<String, Integer> mapa) {
		final int NUMCOLS = 80;
		int max = 0;
		for (Integer num : mapa.values())
			if (num > max)
				max = num;
		final int MAXLENGTHNUM = ("" + max).length();
		final int MAXBAR = NUMCOLS - ("XX (" + max + ") ").length();
		for (Carta carta : baraja) {
			final String id = carta.getIdentificador();
			final Integer numero = mapa.get(id);
			int contador;
			if (numero == null)
				contador = 0;
			else
				contador = numero;
			System.out.print(id + " (");
			final String fmt = "%" + MAXLENGTHNUM + "d) ";
			System.out.printf(fmt, contador);
			for (int k = 0; k < MAXBAR * contador / max; k++)
				System.out.print("#");
			System.out.println();
		}
	}
}

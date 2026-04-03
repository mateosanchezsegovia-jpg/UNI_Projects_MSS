
/* Este archivo fuente tiene codificaci�n ISO-8859-1 */

/**
 * Esta clase implementa el juego simplificado del UNO. Sirve para probar todas las clases creadas por el alumno en la pr�ctica.
 * @author DTE
 * @version 1.0
 *
 */
public class JuegoDelUno {
	
	/**
	 * <p>Este m�todo implementa el juego del UNO. Su comportamiento se puede configurar usando la l�nea de mandatos.</p>
	 * Estos son los pasos que se siguen:
	 * <ol>
	 * <li>Instanciar la pila de cartas para que los jugadores puedan coger cartas de ella para sus manos.</li>
	 * <li>A�adir tantas barajas completas a la pila de cartas como se haya solicitado en la l�nea de mandatos. Una baraja tiene cartas con valores del 1 al 9 y letras asociadas de la "A" a la "D" (36 cartas diferentes en total).</li>
	 * <li>Para garantizar la aleatoriedad al extraer cartas de la "pila de cartas para coger", la pila se debe barajar.</li>
	 * <li>Instanciar la "pila de cartas tiradas". La carta inicial de esta pila se deber� extraer de la "pila de cartas para coger".</li>
	 * <li>Instanciar tantos jugadores como se haya solicitado en la l�nea de mandatos. Todo jugador debe ser capaz de albergar en su mano la totalidad de las cartas disponibles. Cada jugador coger� de la "pila de cartas para coger"
	 * el n�mero inicial de cartas en mano requerido.</li>
	 * <li>El juego otorga en orden, turno a cada jugador tantas veces como sea necesario, hasta que un jugador gane o
	 * hasta que no queden cartas en la "pila de cartas para coger". Cuando le toca el turno a un jugador, este juega usando las cartas de su mano y las de la "pila de cartas para coger" intentando apilarlas sobre la "pila de cartas tiradas".</li>
	 * </ol>
	 * <p>El juego termina cuando un jugador se queda sin cartas (gana) o cuando ya no quedan cartas en la "pila de cartas para coger" para seguir jugando.</p>
	 * <p>La l�nea de mandatos debe aceptar una invocaci�n de la aplicaci�n con los siguientes par�metros:</p>
	 * <p><b>&lt;n�mero de barajas a usar&gt; &lt;n�mero de jugadores&gt; &lt;n�mero inicial de cartas en mano&gt;</b></p>
	 * @param args Argumentos recibidos en la l�nea de mandatos.
	 */
	public static void main(String[] args) {
		if(args.length != 3)
			System.out.println("Par�metros: <n�mero de barajas a usar> <n�mero de jugadores> <n�mero inicial de cartas en mano>");
		else {
			final int numBarajas = Integer.parseInt(args[0]);
			final int numJugadores = Integer.parseInt(args[1]);
			final int numInicialDeCartasEnMano = Integer.parseInt(args[2]);
			jugarPartidaDeUno(numBarajas, numJugadores, numInicialDeCartasEnMano);
		}
	}
	
	private static void jugarPartidaDeUno(int numBarajas, int numJugadores, int numInicialDeCartasEnMano) {
		final int[] VALORES = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		final String[] LETRAS = {"A", "B", "C", "D"};
		final int numeroDeCartasInicialEnPilaParaCoger = numBarajas * VALORES.length * LETRAS.length;
		
		// Preparar la pila de cartas para coger. Tantas barajas completas como se haya pedido.
		PilaDeCartas cartasParaCoger = new PilaDeCartas();
		for(int numB = 0; numB < numBarajas; numB++)
			for(int valor : VALORES )
				for(String letra : LETRAS)
					cartasParaCoger.agregarCarta(new Carta(valor, letra));

		// Barajar la pila
		cartasParaCoger.barajar();
		
		// Preparar pila de cartas ya tiradas. Una carta boca arriba al principio sacada de la pila para coger.
		PilaDeCartas cartasTiradas = new PilaDeCartas();
		cartasTiradas.agregarCarta(cartasParaCoger.extraerCartaParteSuperior());
		
		// Preparar los jugadores de la partida
		JugadorDelUNO[] jugadores = new JugadorDelUNO[numJugadores];
		for(int i = 0; i < numJugadores; i++) {
			jugadores[i] = new JugadorDelUNO("Jugador " + (i+1));
			jugadores[i].cogeCartas(cartasParaCoger, numInicialDeCartasEnMano); // El jugador coge las cartas iniciales de la pila adecuada
		}
		
		// Secuenciaci�n del juego
		System.out.println("Juego del UNO");
		System.out.println("N�mero de jugadores: " + jugadores.length);
		System.out.println("Pila de cartas para coger con " + numeroDeCartasInicialEnPilaParaCoger + " cartas.");
		System.out.println("N�mero inicial de cartas en mano: " + numInicialDeCartasEnMano + " cartas.\n");
		int indiceDeTurno = 0;
		JugadorDelUNO jugador;
		do {
			jugador = jugadores[indiceDeTurno];
			System.out.println("Turno de: " + jugador.getNombre());
			jugador.juega(cartasParaCoger, cartasTiradas);  // juega
			indiceDeTurno = (indiceDeTurno + 1) % numJugadores;
		} while(!jugador.sinCartasEnLaMano() && cartasParaCoger.hayCartasDisponibles());
		if(jugador.sinCartasEnLaMano())
			System.out.println("El ganador es: " + jugador.getNombre());
		else
			System.out.println("Se han acabado las cartas. No hay ganador.");
	}
}

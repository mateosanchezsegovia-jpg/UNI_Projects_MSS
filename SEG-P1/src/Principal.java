/**Fichero: Principal.java
 * Clase para comprobar el funcionamiento de las otras clases del paquete.
 * Asignatura: SEG
 * @author Profesores de la asignatura
 * @version 1.0
 */

/*
 * OBJETIVO:
 * Orquestar la ejecución de la práctica, gestionando la interacción con el usuario 
 * mediante menús por consola y enrutando las peticiones a las clases criptográficas correspondientes.
 * * PUNTOS CLAVE DE LA IMPLEMENTACIÓN:
 * - Gestión de Entrada: Uso de la clase Scanner con limpieza explícita del buffer (sc.nextLine())
 * tras la lectura de enteros para evitar saltos de línea huérfanos.
 * - Lógica Booleana: Traducción de las respuestas del usuario (S/N) a valores booleanos (esPrivada) 
 * para determinar qué tipo de clave debe instanciar el motor asimétrico.
 * - Orquestación: Separación de responsabilidades. Esta clase solo recoge rutas y decisiones;
 * la lógica criptográfica reside exclusivamente en las clases Simetrica y Asimetrica.
 */


import java.util.Scanner;

import cifrado.asimetrica.Asimetrica;
import cifrado.simetrica.Simetrica;

public class Principal {

	public static void main (String [ ] args) {
		int menu1;
		int menu2;
		Scanner sc = new Scanner(System.in);
		/* completar declaracion de variables e instanciación de objetos */
		// Instanciación de los objetos para acceder a sus métodos
		Simetrica simetrica = new Simetrica();
		Asimetrica asimetrica = new Asimetrica();
		
		// Variables auxiliares para leer nombres de ficheros
		String ficheroClave, ficheroEntrada, ficheroSalida, ficheroFirma, ficheroClavePub, ficheroClavePriv;
		String respuestaPrivada;
		boolean esPrivada;
		
		do {
			// Menú principal general
			System.out.println("¿Qué tipo de criptografía desea utilizar?");
			System.out.println("1. Simétrico.");
			System.out.println("2. Asimétrico.");
			System.out.println("3. Salir.");
			menu1 = sc.nextInt();
			sc.nextLine(); //Limpiar el buffer
			
			switch(menu1){
				case 1:
					do{
						// Menú de criptografía simétrica
						System.out.println("Elija una opción para CRIPTOGRAFIA SIMÉTRICA:");
						System.out.println("0. Volver al menú anterior.");
						System.out.println("1. Generar clave.");
						System.out.println("2. Cifrado.");
						System.out.println("3. Descifrado.");
						menu2 = sc.nextInt();
						sc.nextLine(); // Limpiar el buffer
				
						switch(menu2){
							case 1://Generar clave simétrica
								/*completar acciones*/
								System.out.print("Introduzca el nombre del fichero para guardar la clave simétrica: ");
								ficheroClave = sc.nextLine();
								simetrica.generarClave(ficheroClave);
							break;
							case 2://Cifrado simétrico
								/*completar acciones*/
								System.out.print("Fichero que contiene la clave: ");
                                ficheroClave = sc.nextLine();
                               	System.out.print("Fichero a cifrar: ");
                               	ficheroEntrada = sc.nextLine();
                               	System.out.print("Fichero destino (cifrado): ");
                               	ficheroSalida = sc.nextLine();
                               	simetrica.cifrar(ficheroClave, ficheroEntrada, ficheroSalida);
							break;
							case 3://Descifrado simétrico
								/*completar acciones*/
								System.out.print("Fichero que contiene la clave: ");
								ficheroClave = sc.nextLine();
								System.out.print("Fichero a descifrar: ");
								ficheroEntrada = sc.nextLine();
								System.out.print("Fichero destino (descifrado): ");
								ficheroSalida = sc.nextLine();
								simetrica.descifrar(ficheroClave, ficheroEntrada, ficheroSalida);
							break;
						}
					} while(menu2 != 0);
				break;
				case 2:
					do{
						// Menú de criptografía asimétrica
						System.out.println("Elija una opción para CRIPTOGRAFIA ASIMÉTRICA:");
						System.out.println("0. Volver al menú anterior.");
						System.out.println("1. Generar clave.");
						System.out.println("2. Cifrado.");
						System.out.println("3. Descifrado.");
						System.out.println("4. Firmar digitalmente.");
						System.out.println("5. Verificar firma digital.");
						menu2 = sc.nextInt();
						sc.nextLine(); // Limpiar el buffer
				
						switch(menu2){
							case 1:// Generar pareja de claves asimétricas
								/*completar acciones*/
								System.out.print("Nombre fichero para clave pública: ");
								ficheroClavePub = sc.nextLine();
								System.out.print("Nombre fichero para clave privada: ");
                                ficheroClavePriv = sc.nextLine();
                                asimetrica.generarClaves(ficheroClavePub, ficheroClavePriv);
							break;
							case 2:// Cifrado asimétrico
								/*completar acciones*/
								System.out.print("¿Va a cifrar con la clave privada? (S/N): ");	// Es necesario leer si se usa la privada o la pública
								respuestaPrivada = sc.nextLine();
								esPrivada = respuestaPrivada.equalsIgnoreCase("S");
								
								System.out.print("Fichero a cifrar: ");
								ficheroEntrada = sc.nextLine();
								System.out.print("Fichero con la clave: ");
								ficheroClave = sc.nextLine();
								System.out.print("Fichero destino (cifrado): ");
								ficheroSalida = sc.nextLine();
								asimetrica.cifrar(esPrivada, ficheroClave, ficheroEntrada, ficheroSalida);
							break;
							case 3:// Descifrado asimétrico
								/*completar acciones*/
								System.out.print("¿Va a descifrar con la clave privada? (S/N): ");// Es necesario leer si se usa la privada o la pública
								respuestaPrivada = sc.nextLine();
								esPrivada = respuestaPrivada.equalsIgnoreCase("S");
								
								System.out.print("Fichero a descifrar: ");
								ficheroEntrada = sc.nextLine();
								System.out.print("Fichero con la clave: ");
								ficheroClave = sc.nextLine();
								System.out.print("Fichero destino (en claro): ");
								ficheroSalida = sc.nextLine();
								asimetrica.descifrar(esPrivada, ficheroClave, ficheroEntrada, ficheroSalida);
							break;
							case 4:// Firmar digitalmente
								/*completar acciones*/
								System.out.print("Fichero a firmar: ");
                                ficheroEntrada = sc.nextLine();
                               	System.out.print("Fichero con la clave privada: ");
                               	ficheroClave = sc.nextLine();
                               	System.out.print("Fichero destino para la firma: ");
                               	ficheroFirma = sc.nextLine();
                               	asimetrica.firmar(ficheroClave, ficheroEntrada, ficheroFirma);							
                            break;
							case 5:// Verificar firma digital
								/*completar acciones*/
								System.out.print("Fichero original: ");
                               	ficheroEntrada = sc.nextLine();
                               	System.out.print("Fichero con la firma a verificar: ");
                               	ficheroFirma = sc.nextLine();
                               	System.out.print("Fichero con la clave pública: ");
                                ficheroClave = sc.nextLine();
                                asimetrica.verificarFirma(ficheroClave, ficheroEntrada, ficheroFirma);
							break;
						}
					} while(menu2 != 0);
				break;
			}			
		} while(menu1 != 3);
		sc.close();
		System.out.println("Programa finalizado.");
	}
}
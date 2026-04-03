/**Fichero: Simetrica.java
 * Asignatura: SEG
 * @author Mateo Sanchez Segoiva
 * @version 1.0
 */

/*
 * OBJETIVO:
 * Implementar un criptosistema de clave pública y firma digital usando Bouncy Castle Lightweight.
 * * DATOS TÉCNICOS DEL CRIPTOSISTEMA:
 * - Algoritmo: RSA.
 * - Tamaño de clave: 1024 bits. Generada con RSAKeyPairGenerator y almacenada en Hexadecimal
 * guardando por separado el módulo (Modulus) y el exponente (Exponent).
 * - Envoltorio (Encoding): PKCS1Encoding sobre un RSAEngine.
 * * PUNTOS CLAVE DE LA IMPLEMENTACIÓN:
 * - Tamaños de bloque de lectura: Para una clave de 1024 bits (128 bytes) y un relleno PKCS#1 (11 bytes),
 * el tamaño MÁXIMO del texto en claro a leer es de 117 bytes (128 - 11). El tamaño de lectura
 * del texto cifrado siempre es de 128 bytes.
 * - Firma Digital (Autenticación e Integridad): 
 * 1. Se genera un Hash del fichero en claro mediante SHA-1 (SHA1Digest) usando update() y doFinal().
 * 2. Se cifra ese Hash resultante con la CLAVE PRIVADA del emisor.
 * - Verificación de Firma: 
 * 1. Se descifra la firma con la CLAVE PÚBLICA obteniendo el Hash original.
 * 2. Se recalcula el Hash del fichero en claro recibido.
 * 3. Se comparan ambos arrays de bytes (Arrays.equals). Si coinciden, la firma es auténtica.
 */


package cifrado.asimetrica;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.util.encoders.Hex;

public class Asimetrica {

	// Genera una pareja de claves RSA de 1024 bits y las guarda en dos ficheros.
	
	public void generarClaves(String ficheroClavePublica, String ficheroClavePrivada) {
		
		PrintWriter ficheroPrivada = null;
		PrintWriter ficheroPublica = null;
		
		try {
			// Generación parámetros para inicializar el generador de claves
			RSAKeyGenerationParameters parametros = new RSAKeyGenerationParameters(
			BigInteger.valueOf(17), new SecureRandom(), 1024, 10);

			// Instanciar el generador de claves
			RSAKeyPairGenerator generadorClaves = new RSAKeyPairGenerator();

			// Inicializarlo
			generadorClaves.init(parametros);

			// Generar claves
			AsymmetricCipherKeyPair pareja = generadorClaves.generateKeyPair();

			// Obtener clave privada y pública
			RSAKeyParameters cpublica = (RSAKeyParameters) pareja.getPublic();
			RSAPrivateCrtKeyParameters cprivada = (RSAPrivateCrtKeyParameters) pareja.getPrivate();

			// Guardar cada clave en un fichero (Clave Privada)
			ficheroPrivada = new PrintWriter(new FileWriter(ficheroClavePrivada));
			ficheroPrivada.print(new String(Hex.encode(cprivada.getModulus().toByteArray())) + "\r\n");
			ficheroPrivada.print(new String(Hex.encode(cprivada.getExponent().toByteArray())));

			// Guardar cada clave en un fichero (Clave Pública)
			ficheroPublica = new PrintWriter(new FileWriter(ficheroClavePublica));
			ficheroPublica.print(new String(Hex.encode(cpublica.getModulus().toByteArray())) + "\r\n");
			ficheroPublica.print(new String(Hex.encode(cpublica.getExponent().toByteArray())));

			// Generar las claves en formato PEM usando la clase externa 
			GuardarFormatoPEM guardadorPEM = new GuardarFormatoPEM();
			guardadorPEM.guardarClavesPEM(cpublica, cprivada);
			
			System.out.println("Pareja de claves generada correctamente.");

		} catch (Exception e) {
			System.err.println("Error al generar las claves: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Cierre seguro de flujos 
			if (ficheroPrivada != null) {
				try { 
					ficheroPrivada.close(); 
					} catch (Exception e) { 
						e.printStackTrace(); 
						}
			}
			if (ficheroPublica != null) {
				try { 
					ficheroPublica.close(); 
					} catch (Exception e) { 
						e.printStackTrace(); 
						}
			}
		}
	}

	// Método para cifrar (recibe un booleano para saber si usa clave privada o pública)
	public void cifrar(boolean esClavePrivada, String ficheroClave, String ficheroEntrada, String ficheroSalida) {
		
		BufferedReader lectorClave = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		
		try {
			// Lectura modulo y exponente
			lectorClave = new BufferedReader(new FileReader(ficheroClave));
			BigInteger modulo = new BigInteger(Hex.decode(lectorClave.readLine()));
			BigInteger exponente = new BigInteger(Hex.decode(lectorClave.readLine()));

			// Parámetros para el método init de cifrador
			RSAKeyParameters parametros = new RSAKeyParameters(esClavePrivada, modulo, exponente);

			// Instanciar cifrador
			AsymmetricBlockCipher cifrador = new PKCS1Encoding(new RSAEngine());

			// Inicializarlo para CIFRAR (true)
			cifrador.init(true, parametros);

			// Leer bloques del fichero a cifrar e ir cifrando
			in = new BufferedInputStream(new FileInputStream(ficheroEntrada));
			out = new BufferedOutputStream(new FileOutputStream(ficheroSalida));

			// Para una clave de 1024 bits, el bloque de entrada máximo a cifrar es 117 octetos (128 - 11 de relleno)
			int longitudEntrada = 117; 
			byte[] datosLeidos = new byte[longitudEntrada];
			int leidos;

			while ((leidos = in.read(datosLeidos, 0, longitudEntrada)) != -1) {
				// Cifrar bloque
				byte[] datosCifrados = cifrador.processBlock(datosLeidos, 0, leidos);
				out.write(datosCifrados);
			}

			System.out.println("Cifrado asimétrico completado con éxito.");

		} catch (Exception e) {
			System.err.println("Error durante el cifrado asimétrico: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Cierre seguro de flujos 
			try { 
				if (lectorClave != null) lectorClave.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
			try { 
				if (in != null) in.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
			try { 
				if (out != null) out.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
		}
	}

	// Método para descifrar (recibe un booleano para saber si usa clave privada o pública)
	public void descifrar(boolean esClavePrivada, String ficheroClave, String ficheroEntrada, String ficheroSalida) {
		
		BufferedReader lectorClave = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		
		try {
			// Lectura modulo y exponente
			lectorClave = new BufferedReader(new FileReader(ficheroClave));
			BigInteger modulo = new BigInteger(Hex.decode(lectorClave.readLine()));
			BigInteger exponente = new BigInteger(Hex.decode(lectorClave.readLine()));

			// Parámetros para el método init de descifrador
			RSAKeyParameters parametros = new RSAKeyParameters(esClavePrivada, modulo, exponente);

			// Instanciar descifrador
			AsymmetricBlockCipher cifrador = new PKCS1Encoding(new RSAEngine());

			// Inicializarlo para DESCIFRAR (false)
			cifrador.init(false, parametros);

			// Leer bloques del fichero a descifrar e ir descifrando
			in = new BufferedInputStream(new FileInputStream(ficheroEntrada));
			out = new BufferedOutputStream(new FileOutputStream(ficheroSalida));

			// Para descifrar una clave de 1024 bits, el bloque de lectura siempre es 128 octetos
			int longitudEntrada = 128; 
			byte[] datosLeidos = new byte[longitudEntrada];
			int leidos;

			while ((leidos = in.read(datosLeidos, 0, longitudEntrada)) != -1) {
				// Descifrar bloque
				byte[] datosDescifrados = cifrador.processBlock(datosLeidos, 0, leidos);
				out.write(datosDescifrados);
			}

			System.out.println("Descifrado asimétrico completado con éxito.");

		} catch (Exception e) {
			System.err.println("Error durante el descifrado asimétrico: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Cierre seguro de flujos 
			try { 
				if (lectorClave != null) lectorClave.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
			try { 
				if (in != null) in.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
			try { 
				if (out != null) out.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
		}
	}

	// Firma digital de un fichero
	public void firmar(String ficheroClavePrivada, String ficheroEntrada, String ficheroFirma) {
		
		BufferedInputStream in = null;
		FileOutputStream outResumen = null;
		
		try {
			// Instanciar la clase para generar el resumen
			Digest resumen = new SHA1Digest();

			in = new BufferedInputStream(new FileInputStream(ficheroEntrada));
			byte[] bufferLeido = new byte[64]; // Tamaño de lectura estándar
			int leidos;

			// Bucle de lectura de bloques del fichero
			while ((leidos = in.read(bufferLeido)) != -1) {
				// Método update: actualiza el resumen
				resumen.update(bufferLeido, 0, leidos);
			}

			// Generar el resumen final (fuera del bucle)
			byte[] valorHash = new byte[resumen.getDigestSize()];
			resumen.doFinal(valorHash, 0);

			// Escribir el resumen en un fichero intermedio
			String ficheroIntermedio = "resumen_intermedio.dat";
			outResumen = new FileOutputStream(ficheroIntermedio);
			outResumen.write(valorHash);

			// Cifrar el fichero que contiene el resumen invocando al método cifrar (con la clave privada)
			cifrar(true, ficheroClavePrivada, ficheroIntermedio, ficheroFirma);
			
			System.out.println("Fichero firmado correctamente. Firma guardada en: " + ficheroFirma);

		} catch (Exception e) {
			System.err.println("Error durante la firma: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Cierre seguro de flujos 
			try { 
				if (in != null) in.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
			try { 
				if (outResumen != null) outResumen.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
		}
	}

	// Verificar firma digital
	public boolean verificarFirma(String ficheroClavePublica, String ficheroEntrada, String ficheroFirma) {
		boolean verificado = false;
		BufferedInputStream inClaro = null;
		BufferedInputStream inDescifrado = null;
		
		try {
			String ficheroResumenDescifrado = "resumen_descifrado.dat";

			// Descifrar el fichero de la firma invocando al método descifrar (con la clave pública)
			descifrar(false, ficheroClavePublica, ficheroFirma, ficheroResumenDescifrado);

			// Generar el resumen del fichero en claro exactamente igual que al firmar
			Digest resumen = new SHA1Digest();
			inClaro = new BufferedInputStream(new FileInputStream(ficheroEntrada));
			byte[] bufferLeido = new byte[64];
			int leidos;

			while ((leidos = inClaro.read(bufferLeido)) != -1) {
				resumen.update(bufferLeido, 0, leidos);
			}

			byte[] valorHashGenerado = new byte[resumen.getDigestSize()];
			resumen.doFinal(valorHashGenerado, 0);

			// Leer el contenido del fichero descifrado y verificar si es igual al resumen generado
			inDescifrado = new BufferedInputStream(new FileInputStream(ficheroResumenDescifrado));
			byte[] valorHashDescifrado = new byte[resumen.getDigestSize()];
			inDescifrado.read(valorHashDescifrado);

			verificado = Arrays.equals(valorHashGenerado, valorHashDescifrado);

			if (verificado) {
				System.out.println("VERIFICACIÓN EXITOSA: La firma es válida y auténtica.");
			} else {
				System.out.println("VERIFICACIÓN FALLIDA: La firma NO es válida.");
			}

		} catch (Exception e) {
			System.err.println("Error durante la verificación: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Cierre seguro de flujos 
			try { 
				if (inClaro != null) inClaro.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
			try { 
				if (inDescifrado != null) inDescifrado.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
					}
		}
		
		return verificado;
	}
}
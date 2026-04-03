/**Fichero: Simetrica.java
 * Asignatura: SEG
 * @author Mateo Sanchez Segoiva
 * @version 1.0
 */

/*
 * OBJETIVO:
 * Implementar un criptosistema de clave secreta utilizando la API Bouncy Castle Lightweight.
 * * DATOS TÉCNICOS DEL CRIPTOSISTEMA:
 * - Algoritmo: Blowfish.
 * - Tamaño de clave: 448 bits. Generada con CipherKeyGenerator y almacenada en Hexadecimal (Hex.encode).
 * - Modo de operación: CBC (Cipher Block Chaining).
 * - Relleno (Padding): PKCS#7.
 * - Motor Bouncy Castle: PaddedBufferedBlockCipher(new CBCBlockCipher(new BlowfishEngine()), new PKCS7Padding()).
 * * PUNTOS CLAVE DE LA IMPLEMENTACIÓN:
 * - Procesamiento perezoso: Uso de 'processBytes' para cifrar/descifrar los bloques en tránsito 
 * y 'doFinal' exclusivamente para el último bloque, encargado de aplicar o retirar el padding.
 * - Tamaño de bloque algorítmico: Blowfish procesa bloques de 64 bits (8 bytes), independiente 
 * del tamaño del array de lectura del BufferedInputStream.
 * - Cierre seguro: Uso de bloques try-catch-finally para asegurar la liberación de recursos (close) 
 * en los flujos de lectura y escritura (I/O).
 */

package cifrado.simetrica;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.SecureRandom;

import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

public class Simetrica {

	// Genera una clave simétrica Blowfish de 448 bits y la guarda en formato hexadecimal.

	public void generarClave(String nombreFichero) {
		// Declarar la variable FUERA del try para que el finally pueda verla
				FileOutputStream salida = null;
				
		try {
			// Crear objeto generador
			CipherKeyGenerator genClave = new CipherKeyGenerator();

			// Inicializar objeto generador indicando fuente de aleatoriedad y tamaño de 448 bits
			genClave.init(new KeyGenerationParameters(new SecureRandom(), 448));

			// Generar clave, que devuelve un array de bytes
			byte[] clave = genClave.generateKey();

			// Convertir clave a Hexadecimal para poder visualizarla con un editor de texto
			byte[] claveHex = Hex.encode(clave);

			// Almacenar clave en fichero
			salida = new FileOutputStream(nombreFichero);
			salida.write(claveHex);

			System.out.println("Clave simétrica generada y guardada correctamente en: " + nombreFichero);

		} catch (Exception e) {
			System.err.println("Error al generar la clave: " + e.getMessage());
			e.printStackTrace();
		}finally {
			// Cierre seguro del flujo
			if (salida != null) {
				try {
					salida.close();
				} catch (Exception e) {
					e.printStackTrace();
					}
			}
		}
	}

	// Cifra un fichero utilizando el algoritmo Blowfish en modo CBC con relleno PKCS#7.
	
	public void cifrar(String ficheroClave, String ficheroEntrada, String ficheroSalida) {
		
		BufferedReader br = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		
		try {
			// Leer clave y decodificar de Hex a binario
			br = new BufferedReader(new FileReader(ficheroClave));
			String claveHex = br.readLine();
			byte[] claveBin = Hex.decode(claveHex);
	        	
			// Generar parámetros y cargar la clave binaria
			KeyParameter params = new KeyParameter(claveBin);
	        	
			// Crear motor de cifrado con los datos del enunciado (CBC, Blowfish, PKCS#7)
			PaddedBufferedBlockCipher cifrador = new PaddedBufferedBlockCipher(new CBCBlockCipher(new BlowfishEngine()), new PKCS7Padding());
			
			// Iniciar motor de cifrado para CIFRAR(true)
			cifrador.init(true, params);
	        	
			// Crear flujos E/S ficheros
			in = new BufferedInputStream(new FileInputStream(ficheroEntrada));
			out = new BufferedOutputStream(new FileOutputStream(ficheroSalida));
	        	
			// Crear arrays de bytes para E/S
			// NOTA: 'longitud' es el tamaño de lectura del buffer en bytes.
			// El tamaño de bloque real del algoritmo Blowfish siempre es de 64 bits (8 bytes) .
			int longitud = 64; //Tamaño de bloque en bytes
			byte[] almacen = new byte[longitud];
			
			// Se reserva tamaño suficiente en el buffer de salida basándose en la estimación del motor
			byte[] datosProcesados = new byte[cifrador.getOutputSize(longitud)];

			// BUCLE DE LECTURA, CIFRADO Y ESCRITURA de bloques de datos
			int leidos;
			while ((leidos = in.read(almacen, 0, longitud)) != -1) {
				// processBytes cifra cada bloque leído
				int procesados = cifrador.processBytes(almacen, 0, leidos, datosProcesados, 0);
				if (procesados > 0) {
					out.write(datosProcesados, 0, procesados);
				}
			}
			
			// Cifrar el último bloque (doFinal) para aplicar el padding
			int procesadosFinal = cifrador.doFinal(datosProcesados, 0);
			if (procesadosFinal > 0) {
				out.write(datosProcesados, 0, procesadosFinal);
			}
            
			System.out.println("Cifrado completado con éxito. Resultado en: " + ficheroSalida);

		} catch (Exception e) {
			System.err.println("Error durante el cifrado: " + e.getMessage());
			e.printStackTrace();
		}finally {
			// Cierre seguro de los flujos
			try { 
				if (br != null) br.close(); 
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
				}catch (Exception e) { 
					e.printStackTrace(); 
					}
		}
}
	
	// El pseudocódigo para el descifrado es similar al del cifrado, excepto en la inicialización
	
	public void descifrar(String ficheroClave, String ficheroEntrada, String ficheroSalida) {
		
		BufferedReader br = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		
		try {
			// Leer clave y decodificar de Hex a binario
			br = new BufferedReader(new FileReader(ficheroClave));
			String claveHex = br.readLine();
			byte[] claveBin = Hex.decode(claveHex);
	        	
			// Generar parámetros y cargar clave
			KeyParameter params = new KeyParameter(claveBin);
	        	
			// Crear motor de cifrado con los datos del enunciado (CBC, Blowfish, PKCS#7)
			PaddedBufferedBlockCipher cifrador = new PaddedBufferedBlockCipher(new CBCBlockCipher(new BlowfishEngine()), new PKCS7Padding());
			
			// Iniciar motor de cifrado para DESCIFRAR(false)
			cifrador.init(false, params);
	        	
			// Crear flujos E/S ficheros
			in = new BufferedInputStream(new FileInputStream(ficheroEntrada));
			out = new BufferedOutputStream(new FileOutputStream(ficheroSalida));
	        	
			// Crear arrays de bytes para E/S
			int longitud = 64; //Tamaño de bloque en bytes
			byte[] almacen = new byte[longitud];
			
			// Se reserva tamaño suficiente en el buffer de salida basándose en la estimación del motor
			byte[] datosProcesados = new byte[cifrador.getOutputSize(longitud)];

			// BUCLE DE LECTURA, DESCIFRADO Y ESCRITURA de bloques de datos
			int leidos;
			while ((leidos = in.read(almacen, 0, longitud)) != -1) {
				// processBytes cifra cada bloque leído
				int procesados = cifrador.processBytes(almacen, 0, leidos, datosProcesados, 0);
				if (procesados > 0) {
					out.write(datosProcesados, 0, procesados);
				}
			}
			
			// Descifrar el último bloque (doFinal) quitando el padding
			int procesadosFinal = cifrador.doFinal(datosProcesados, 0);
			if (procesadosFinal > 0) {
				out.write(datosProcesados, 0, procesadosFinal);
			}
            
			System.out.println("Descifrado completado con éxito. Resultado en: " + ficheroSalida);

		} catch (Exception e) {
			System.err.println("Error durante el descrifrado: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Cierre seguro de los flujos
			try { 
				if (br != null) br.close(); 
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
				}catch (Exception e) { 
					e.printStackTrace(); 
					}
		}
	}

}

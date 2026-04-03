"""
=============================================================================
PROYECTO NEXAFLOW: MÓDULO DE EXTRACCIÓN Y EMISIÓN V2X (CAPAS 1, 2 Y 3)
=============================================================================
Descripción:
Este script actúa como el núcleo físico y de comunicaciones de la intersección. 
Conecta Python con el simulador Eclipse SUMO mediante la API TraCI y realiza 
tres funciones principales simultáneas en cada paso de simulación:

1. CAPA FÍSICA Y CINEMÁTICA (TraCI): 
   Extrae en tiempo real la posición, velocidad, ángulo y métricas medioambientales 
   (HBEFA3: CO2 y combustible) de cada vehículo presente en la red.
   
2. EXPORTACIÓN DE MÉTRICAS (Análisis Post-Simulación): 
   Guarda de forma estructurada toda la telemetría extraída en un archivo 
   local ('telemetria_nexaflow.csv') para la evaluación posterior de KPIs.

3. EMULACIÓN DE COMUNICACIONES V2X (ZeroMQ): 
   Actúa como un nodo Publicador (PUB). Empaqueta los datos clave de cada vehículo 
   en formato JSON y los emite de forma asíncrona (sin broker) a través del 
   puerto TCP 5555, simulando la latencia ultra-baja del estándar C-V2X.

Dependencias necesarias:
- traci (Eclipse SUMO)
- pyzmq (pip install pyzmq)

Instrucciones de uso:
1. Asegurar que 'cruce.net.xml' y 'rutas.rou.xml' están en el mismo directorio.
2. Ejecutar este script. Abrirá SUMO y comenzará a emitir datos.
3. (Opcional) Ejecutar en paralelo el script del RSU receptor para leer los paquetes.
=============================================================================
"""
import os
import sys
import time
import csv
import json
import zmq

# --- CONFIGURACIÓN UNIVERSAL DE RUTAS ---
if 'SUMO_HOME' not in os.environ:
    print("ERROR: Por favor, configura la variable de entorno SUMO_HOME en tu sistema.")
    sys.exit("SUMO_HOME no encontrada")
sys.path.append(os.path.join(os.environ['SUMO_HOME'], 'tools'))

import traci

# 1. Configuración de rutas
carpeta_actual = os.path.dirname(os.path.abspath(__file__))
ruta_red = os.path.join(carpeta_actual, "cruce.net.xml")
ruta_rutas = os.path.join(carpeta_actual, "rutas.rou.xml")
ruta_csv = os.path.join(carpeta_actual, "telemetria_nexaflow.csv")

sumo_cmd = ["sumo-gui", "-n", ruta_red, "-r", ruta_rutas, "--start", "-Q", "--step-length", "0.1"]

# 2. Configuración de ZeroMQ (Capa 3 - Publicador V2X)
context = zmq.Context()
socket = context.socket(zmq.PUB)
# Enlazamos el socket al puerto 5555. El asterisco indica que acepta conexiones locales.
socket.bind("tcp://*:5555") 

# [NUEVO] Umbral de seguridad para el sistema anti-colisión
UMBRAL_TTC_CRITICO = 2.5 

print("Iniciando conexión V2X con extracción de métricas y emisor ZeroMQ (Puerto 5555)...")

try:
    traci.start(sumo_cmd)
    traci.gui.setSchema("View #0", "real world")
    traci.gui.setZoom("View #0", 350) # Prueba con 300, si se ve lejos pon 800    
    lista_semaforos = traci.trafficlight.getIDList()
    if not lista_semaforos:
        print("Error: No se detectó ningún semáforo.")
        sys.exit()
    id_semaforo = lista_semaforos[0]

    paso = 0
    
    with open(ruta_csv, mode='w', newline='') as archivo_csv:
        escritor_csv = csv.writer(archivo_csv)
        # [MODIFICADO] Se añade la columna TTC_s al archivo CSV
        escritor_csv.writerow(["Paso", "Semaforo_ID", "Estado_Luces", "Coche_ID", "Pos_X", "Pos_Y", "Velocidad_kmh", "Angulo_Grados", "CO2_mg_s", "Combustible_ml_s", "TTC_s"])

        # 3. Bucle principal
        # [MODIFICADO] Se añade el límite de 1000 pasos para evitar bucles infinitos si hay un choque
        while traci.simulation.getMinExpectedNumber() > 0 and paso < 1000:
            traci.simulationStep()
            
            estado_luces = traci.trafficlight.getRedYellowGreenState(id_semaforo)
            coches_en_mapa = traci.vehicle.getIDList()
            
            if coches_en_mapa:
                for coche in coches_en_mapa:
                    pos_x, pos_y = traci.vehicle.getPosition(coche)
                    # [MODIFICADO] Separamos la velocidad en m/s para la fórmula del TTC y en km/h para exportar
                    velocidad_ms = traci.vehicle.getSpeed(coche)
                    velocidad_kmh = velocidad_ms * 3.6
                    angulo = traci.vehicle.getAngle(coche)
                    co2 = traci.vehicle.getCO2Emission(coche)
                    combustible = traci.vehicle.getFuelConsumption(coche)
                    
                    # ========================================================
                    # [NUEVO] FASE 3: LÓGICA DE FRENADA DE EMERGENCIA (TTC)
                    # ========================================================
                    ttc_actual = 999.0 # Valor seguro inicial
                    
                    # Escanea hasta 50 metros hacia adelante en el mismo carril
                    lider_info = traci.vehicle.getLeader(coche, 50.0)
                    
                    if lider_info:
                        lider_id, distancia = lider_info
                        v_lider_ms = traci.vehicle.getSpeed(lider_id)
                        v_relativa = velocidad_ms - v_lider_ms
                        
                        # Si nos acercamos al coche de delante (v_relativa positiva)
                        if v_relativa > 0.1:
                            ttc_actual = distancia / v_relativa
                            
                            if ttc_actual < UMBRAL_TTC_CRITICO:
                                # Clava los frenos (0 m/s) y pone el coche en rojo
                                traci.vehicle.slowDown(coche, 0.0, 2.0)                                
                                traci.vehicle.setColor(coche, (255, 0, 0))
                                print(f"[ALERTA] Frenada de emergencia! Coche: {coche} | TTC: {ttc_actual:.2f}s")
                            else:
                                # Restaura el control si el TTC vuelve a ser seguro
                                traci.vehicle.setSpeed(coche, -1)
                                traci.vehicle.setColor(coche, (255, 255, 255))
                        else:
                            # [NUEVO] Restaura el control si ya no nos acercamos al líder (ej: estamos parados)
                            traci.vehicle.setSpeed(coche, -1)
                            traci.vehicle.setColor(coche, (255, 255, 255))
                    else:
                        # [NUEVO] Restaura el control si el líder ha desaparecido (ej: giró en la calle)
                        traci.vehicle.setSpeed(coche, -1)
                        traci.vehicle.setColor(coche, (255, 255, 255))
                    # ========================================================

                    # Escritura local en CSV
                    # [MODIFICADO] Añadimos el TTC al guardado CSV
                    escritor_csv.writerow([paso, id_semaforo, estado_luces, coche, round(pos_x, 2), round(pos_y, 2), round(velocidad_kmh, 2), round(angulo, 2), round(co2, 2), round(combustible, 2), round(ttc_actual, 2)])
                    
                    # Creación del Payload (Paquete de datos de red)
                    # [MODIFICADO] Añadimos el TTC al paquete de datos de red
                    payload = {
                        "paso": paso,
                        "coche_id": coche,
                        "velocidad_kmh": round(velocidad_kmh, 2),
                        "co2": round(co2, 2),
                        "ttc": round(ttc_actual, 2)
                    }

                    # Emisión asíncrona por ZeroMQ con el "topic" telemetria
                    socket.send_string(f"telemetria {json.dumps(payload)}")
            else:
                # [MODIFICADO] Añadimos un valor 999.0 al TTC si no hay coches
                escritor_csv.writerow([paso, id_semaforo, estado_luces, "Ninguno", 0, 0, 0, 0, 0, 0, 999.0])
            
            # Comentario en consola simplificado para no saturar
            print(f"Paso {paso} | Datos emitidos por ZMQ y guardados en CSV.")
            
            time.sleep(0.015)  # Pequeña pausa para evitar saturar la CPU
            paso += 1

except Exception as e:
    print(f"Error en simulación: {e}")

finally:
    # 4. Cierre seguro de SUMO y de los puertos de red
    print(f"\nSimulación finalizada. Cerrando emisor ZMQ...")
    sys.stdout.flush() 
    try:
        traci.close()
        socket.close()
        context.term()
    except:
        pass
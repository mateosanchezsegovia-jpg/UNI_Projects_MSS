"""
=============================================================================
PROYECTO NEXAFLOW: MÓDULO DE ENTRENAMIENTO Y PUENTE V2X-DQN (CAPAS 1 A 4)
=============================================================================
Descripción:
Este script funciona como el entorno de extracción y actuación continua para 
el entrenamiento del Agente DQN. Conecta el simulador SUMO con la lógica de 
control (IA) estableciendo un flujo de comunicación bidireccional mediante ZeroMQ.

Funciones clave:
1. CONSCIENCIA DE FASE (V2): Extrae el estado actual del semáforo junto con 
   las métricas globales de la intersección (Velocidad media, CO2 total y TTC).
2. FLUJO BIDIRECCIONAL V2X: Emite la telemetría por el puerto 5555 (PUB) y 
   escucha simultáneamente órdenes de control de la IA por el puerto 5556 
   (SUB) para alterar las fases del semáforo dinámicamente.
3. PROTOCOLO DE SINCRONIZACIÓN: Implementa un sistema de 'Handshake' para 
   garantizar que el Agente está conectado antes de arrancar la simulación, y 
   cuenta con un bucle infinito para permitir múltiples episodios de 
   entrenamiento ininterrumpidos.

Dependencias:
- traci (Eclipse SUMO)
- pyzmq
- numpy

Instrucciones:
Ejecutar este script como puente físico. Debe correr en paralelo con el script 
del entorno Gymnasium/DQN que tomará las decisiones enviando órdenes al 5556.
=============================================================================
"""
import os
import sys
import zmq
import json
import numpy as np

# --- CONFIGURACIÓN UNIVERSAL DE RUTAS ---
if 'SUMO_HOME' not in os.environ:
    print("ERROR: Por favor, configura la variable de entorno SUMO_HOME en tu sistema apuntando a Plexe-SUMO.")
    sys.exit("SUMO_HOME no encontrada")
sys.path.append(os.path.join(os.environ['SUMO_HOME'], 'tools'))

import traci

# Función principal de ingesta de datos: crea el "vector de estado" para la IA
def obtener_metricas_interseccion(id_tl):
    # Obtiene todos los vehículos activos en el paso actual
    ids = traci.vehicle.getIDList()
    fase = traci.trafficlight.getPhase(id_tl) # <--- OBTENEMOS LA FASE ACTUAL
    
    # Si la intersección está vacía, devuelve un estado neutral
    if not ids:
        return 0.0, 0.0, 999.0, fase
    
    total_vel = 0.0
    total_co2 = 0.0
    min_ttc = 999.0 # Inicializamos el Time-To-Collision con un valor alto y seguro
    
    # Itera sobre cada vehículo para sumar sus métricas globales
    for v_id in ids:
        try:
            # Extracción de cinemática y emisiones
            v_ms = traci.vehicle.getSpeed(v_id)
            total_vel += (v_ms * 3.6) # Conversión a km/h
            total_co2 += traci.vehicle.getCO2Emission(v_id)
            
            # Lógica de detección del vehículo líder (hasta 150m de distancia)
            leader_data = traci.vehicle.getLeader(v_id, 150)
            if leader_data:
                l_id, dist = leader_data 
                v_lider = traci.vehicle.getSpeed(l_id)
                rel_v = v_ms - v_lider
                
                # Si nos estamos acercando al líder, calculamos en cuántos segundos chocaríamos
                if rel_v > 0:
                    ttc_v = dist / rel_v
                    # Guardamos el TTC más crítico (el más bajo) de toda la intersección
                    min_ttc = min(min_ttc, ttc_v)
            else:
                # Si no hay líder, se devuelve el control de velocidad al vehículo
                traci.vehicle.setSpeed(v_id, -1)
        except: continue
            
    # Calcula el promedio de velocidad de todos los coches
    avg_vel = total_vel / len(ids)
    
    # Devuelve el estado empaquetado y redondeado para el agente DQN
    return round(avg_vel, 2), round(total_co2, 2), round(min_ttc, 2), fase

# --- ZMQ ---
context = zmq.Context()

# Socket PUB (Publicador): Emite el estado de la intersección por el puerto 5555
sock_pub = context.socket(zmq.PUB)
sock_pub.bind("tcp://*:5555")

# Socket SUB (Suscriptor): Escucha las decisiones de la IA por el puerto 5556
sock_sub = context.socket(zmq.SUB)
sock_sub.connect("tcp://localhost:5556")
# Solo filtra y procesa los mensajes que comiencen con la palabra "orden"
sock_sub.setsockopt_string(zmq.SUBSCRIBE, "orden")

# Aseguramos que los XML se busquen en la carpeta del script
carpeta_actual = os.path.dirname(os.path.abspath(__file__))
ruta_red = os.path.join(carpeta_actual, "cruce.net.xml")
ruta_rutas = os.path.join(carpeta_actual, "rutas.rou.xml")

# Comando de ejecución usando 'sumo' (sin GUI para entrenar mucho más rápido)
sumo_cmd = ["sumo", "-n", ruta_red, "-r", ruta_rutas, "--step-length", "0.1", "--start"]
id_tl = "J4"

while True: # Bucle de reinicio infinito para entrenamiento largo (Múltiples episodios)
    traci.start(sumo_cmd)
    print("[*] NEXAFLOW: Esperando Handshake...")
    
    # PROTOCOLO DE SINCRONIZACIÓN: 
    # El simulador se pausa aquí hasta que la IA envía una señal de vida, 
    # evitando que la simulación corra a ciegas sin el controlador conectado.
    while True:
        try:
            # Intenta recibir un mensaje sin bloquear la ejecución (NOBLOCK)
            msg = sock_sub.recv_string(flags=zmq.NOBLOCK)
            break
        except zmq.Again: continue

    try:
        # Bucle principal de simulación (1 episodio = hasta que se vacíen los coches)
        while traci.simulation.getMinExpectedNumber() > 0:
            traci.simulationStep()
            
            # 1. ESCUCHA DE ACCIONES (Capa 4 -> Capa 1):
            try:
                # Comprueba si ha llegado una orden desde la red neuronal
                msg_orden = sock_sub.recv_string(flags=zmq.NOBLOCK)
                
                # Si la IA envía un "1", se avanza a la siguiente fase del semáforo
                if msg_orden.split(' ')[1] == "1":
                    # Suma 1 a la fase actual y usa módulo 4 para volver a 0 si llega al final del ciclo
                    traci.trafficlight.setPhase(id_tl, (traci.trafficlight.getPhase(id_tl) + 1) % 4)
            # Si no hay orden en este tick, simplemente se ignora y el semáforo sigue igual
            except zmq.Again: pass
            
            # 2. EXTRACCIÓN DE ESTADO:
            # Calcula las métricas del instante actual
            v, c, t, f = obtener_metricas_interseccion(id_tl)
            
            # 3. EMISIÓN DE TELEMETRÍA (Capa 2 -> Capa 3):
            # Empaqueta las métricas en JSON
            payload = {"velocidad_kmh": v, "co2": c, "ttc": t, "fase": f} # <--- ENVIAMOS FASE
            # Envía el estado al Agente DQN con el tópico "telemetria"
            sock_pub.send_string(f"telemetria {json.dumps(payload)}")
            
    except Exception as e: print(f"Reiniciando... {e}")
    
    # Cierre de instancia para limpiar memoria antes de empezar el siguiente episodio
    finally: traci.close()
"""
=============================================================================
PROYECTO NEXAFLOW: MÓDULO DE EVALUACIÓN VISUAL V2X (CAPAS 1, 2 Y 3)
=============================================================================
Descripción:
Este script es una variante del módulo de entrenamiento, diseñada específicamente 
para la auditoría visual y la validación del modelo. En lugar de correr en 
segundo plano lo más rápido posible, levanta la interfaz gráfica de SUMO.

Funciones clave:
1. RALENTIZACIÓN DEL ENTORNO: Introduce un retardo deliberado (--delay 50) 
   para permitir la observación humana del comportamiento de los "platoons" y 
   las decisiones del semáforo.
2. EXTRACCIÓN PARA KPI: Sigue capturando la telemetría (Velocidad, CO2, TTC, Fase) 
   y emitiéndola por ZMQ, pero esta vez con el objetivo de generar logs de 
   rendimiento en lugar de retroalimentar un entrenamiento.

Dependencias:
- traci (Eclipse SUMO)
- pyzmq
- numpy

Instrucciones:
Ejecutar primero este script. Se quedará esperando el "Handshake". Luego, 
ejecutar el script 'rsu_gym_eval.py' para que la IA tome el control y comience 
la simulación visual.
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

# Función de extracción de estado de la intersección
def obtener_metricas_interseccion(id_tl):
    ids = traci.vehicle.getIDList()
    fase = traci.trafficlight.getPhase(id_tl)
    if not ids: return 0.0, 0.0, 999.0, fase
    
    total_vel, total_co2, min_ttc = 0.0, 0.0, 999.0
    for v_id in ids:
        try:
            v_ms = traci.vehicle.getSpeed(v_id)
            total_vel += (v_ms * 3.6)
            total_co2 += traci.vehicle.getCO2Emission(v_id)
            leader_data = traci.vehicle.getLeader(v_id, 150)
            if leader_data:
                l_id, dist = leader_data 
                rel_v = v_ms - traci.vehicle.getSpeed(l_id)
                if rel_v > 0: min_ttc = min(min_ttc, dist / rel_v)
        except: continue
    return round(total_vel/len(ids), 2), round(total_co2, 2), round(min_ttc, 2), fase

# --- ZMQ ---
context = zmq.Context()
# Configuración del emisor V2X (Puerto 5555) y receptor de órdenes (Puerto 5556)
sock_pub = context.socket(zmq.PUB); sock_pub.bind("tcp://*:5555")
sock_sub = context.socket(zmq.SUB); sock_sub.connect("tcp://localhost:5556")
sock_sub.setsockopt_string(zmq.SUBSCRIBE, "orden")

# Aseguramos rutas dinámicas
carpeta_actual = os.path.dirname(os.path.abspath(__file__))
ruta_red = os.path.join(carpeta_actual, "cruce.net.xml")
ruta_rutas = os.path.join(carpeta_actual, "rutas.rou.xml")

# --- SUMO-GUI (MODO VISUAL) ---
# He añadido --delay 50 (ms) para que la animación sea fluida pero humana
sumo_cmd = ["sumo-gui", "-n", ruta_red, "-r", ruta_rutas, "--step-length", "0.1", "--start", "--delay", "50"]
id_tl = "J4"

print("[*] MODO EVALUACIÓN: Iniciando SUMO-GUI...")
traci.start(sumo_cmd)
traci.gui.setSchema("View #0", "real world")

# Handshake: Espera activa hasta que el entorno evaluador inicie
while True:
    try:
        if "orden" in sock_sub.recv_string(flags=zmq.NOBLOCK): break
    except zmq.Again: continue

print("[!] Agente conectado. ¡Mira la ventana de SUMO!")

try:
    # Bucle de simulación visual de 1 único episodio
    while traci.simulation.getMinExpectedNumber() > 0:
        traci.simulationStep()
        
        # Recepción de la inferencia (decisión del modelo) e inyección en SUMO
        try:
            msg = sock_sub.recv_string(flags=zmq.NOBLOCK)
            if msg.split(' ')[1] == "1":
                traci.trafficlight.setPhase(id_tl, (traci.trafficlight.getPhase(id_tl) + 1) % 4)
        except zmq.Again: pass
        
        # Emisión de métricas para ser registradas
        v, c, t, f = obtener_metricas_interseccion(id_tl)
        sock_pub.send_string(f"telemetria {json.dumps({'velocidad_kmh': v, 'co2': c, 'ttc': t, 'fase': f})}")
finally:
    traci.close()
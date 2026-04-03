"""
=============================================================================
PROYECTO NEXAFLOW: SCRIPT BASE DE SIMULACIÓN Y CONEXIÓN TRACI (CAPAS 1 Y 2)
=============================================================================
Descripción:
Este script representa una iteración inicial (Fase 1) de la arquitectura.
Actúa como la base de la simulación, conectando Python con el simulador 
Eclipse SUMO mediante la API TraCI, realizando las siguientes funciones:

1. CAPA FÍSICA Y GENERACIÓN DEL ESCENARIO (Capa 1): 
   Carga la red de tráfico ('cruce.net.xml') y las rutas ('rutas.rou.xml') 
   para inicializar el entorno de SUMO donde se calculará el movimiento.
   
2. CAPTURA DE DATOS BÁSICA (Capa 2): 
   La interfaz TraCI extrae en tiempo real la información del simulador. 
   En esta versión, el script lee la variable de estado de velocidad 
   del primer vehículo detectado para verificar la correcta extracción.

Dependencias necesarias:
- traci (Eclipse SUMO)
- os, sys, time (Librerías estándar de Python)

Instrucciones de uso:
1. Asegurar que 'cruce.net.xml' y 'rutas.rou.xml' están en el mismo directorio.
2. Ejecutar este script. Abrirá la interfaz gráfica SUMO-GUI.
3. Monitorizar la consola para observar la extracción en tiempo real de la 
   velocidad (km/h) del vehículo en seguimiento.
=============================================================================
"""
import os
import sys
import time

# --- CONFIGURACIÓN UNIVERSAL DE RUTAS ---
if 'SUMO_HOME' not in os.environ:
    print("ERROR: Por favor, configura la variable de entorno SUMO_HOME en tu sistema.")
    sys.exit("SUMO_HOME no encontrada")
sys.path.append(os.path.join(os.environ['SUMO_HOME'], 'tools'))

import traci

# 1. Obtenemos las rutas
carpeta_actual = os.path.dirname(os.path.abspath(__file__))
ruta_red = os.path.join(carpeta_actual, "cruce.net.xml")
ruta_rutas = os.path.join(carpeta_actual, "rutas.rou.xml")

# Añadido "-Q" para forzar el cierre automático de la ventana al terminar
sumo_cmd = ["sumo-gui", "-n", ruta_red, "-r", ruta_rutas, "--start", "-Q"]

print("Iniciando conexión V2X con NexaFlow...")

# 2. Bloque de ejecución segura
try:
    traci.start(sumo_cmd)
    traci.gui.setSchema("View #0", "real world")

    paso = 0
    # 3. Bucle principal dinámico
    while traci.simulation.getMinExpectedNumber() > 0:
        traci.simulationStep()
        
        coches_en_mapa = traci.vehicle.getIDList()
        
        if coches_en_mapa:
            primer_coche = coches_en_mapa[0]
            velocidad_ms = traci.vehicle.getSpeed(primer_coche)
            velocidad_kmh = velocidad_ms * 3.6
            print(f"Paso {paso} | Vehículo: {primer_coche} | Velocidad: {velocidad_kmh:.1f} km/h")
        
        time.sleep(0.1)
        paso += 1

except Exception as e:
    # Captura cualquier error general durante la ejecución
    pass 

finally:
    # 4. Cierre garantizado
    # 4. Cierre garantizado
    # Imprimimos el mensaje ANTES de cerrar la conexión
    print("\nSimulación finalizada: No quedan más vehículos en la red.")
    sys.stdout.flush() 
    
    try:
        traci.close()
    except:
        pass
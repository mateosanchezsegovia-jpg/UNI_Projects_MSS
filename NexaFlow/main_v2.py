"""
=============================================================================
PROYECTO NEXAFLOW: CONSCIENCIA DE FASE Y EXTRACCIÓN SPaT (CAPA 1 Y 2)
=============================================================================
Descripción:
Este script avanza hacia la versión V2 de la arquitectura, introduciendo 
la "Consciencia de Fase" para el sistema. Conecta Python con el simulador 
Eclipse SUMO mediante la API TraCI para realizar la siguiente función clave:

1. CAPTURA DE DATOS DE INFRAESTRUCTURA (Capa 2): 
   Detecta dinámicamente los controladores de tráfico de la red y extrae 
   en cada tick su estado exacto (fase actual de luces rojo-ámbar-verde).
   
2. PREPARACIÓN PARA PROTOCOLOS V2X: 
   Esta extracción es el pilar para emular el comportamiento de la RSU. 
   El estado del controlador físico se utilizará posteriormente para generar 
   y emitir mensajes estándar SPaT hacia el Agente DQN.

Dependencias necesarias:
- traci (Eclipse SUMO)
- os, sys, time

Instrucciones de uso:
1. Asegurar que 'cruce.net.xml' y 'rutas.rou.xml' están en el directorio raíz.
2. Ejecutar este script. Abrirá la interfaz y detectará semáforos automáticamente.
3. Observar la consola para verificar la captura en tiempo real de las fases.
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

# 1. Configuración de rutas absolutas para garantizar la lectura de los archivos XML del escenario
carpeta_actual = os.path.dirname(os.path.abspath(__file__))
ruta_red = os.path.join(carpeta_actual, "cruce.net.xml")
ruta_rutas = os.path.join(carpeta_actual, "rutas.rou.xml")

# Comando de ejecución del motor físico SUMO con carga automática de la red y rutas
sumo_cmd = ["sumo-gui", "-n", ruta_red, "-r", ruta_rutas, "--start"]

print("Iniciando conexión TraCI con NexaFlow...")

try:
    # 2. Inicialización de la Capa 2: Se establece la comunicación entre el script y el simulador
    traci.start(sumo_cmd)
    
    # 3. Mapeo de la infraestructura física: Extracción automática de los controladores disponibles
    lista_semaforos = traci.trafficlight.getIDList()
    
    # Sistema de seguridad en caso de cargar una red sin intersecciones semaforizadas
    if not lista_semaforos:
        print("Error: No se ha detectado ningún semáforo en el mapa de la simulación.")
        sys.exit()
        
    # Asignación del nodo principal de actuación (RSU objetivo)
    id_semaforo = lista_semaforos[0]
    print(f"Semáforo detectado con ID: {id_semaforo}")

    paso = 0
    
    # 4. Bucle principal de ejecución síncrona
    while traci.simulation.getMinExpectedNumber() > 0:
        
        # Ejecución cinemática del motor físico (1 tick)
        traci.simulationStep() 
        
        # Extracción del estado actual del actuador. 
        # Este dato es la base para la futura generación del mensaje SPaT y la "Consciencia de Fase" del DQN
        estado_luces = traci.trafficlight.getRedYellowGreenState(id_semaforo)
        
        # Salida por consola de la telemetría de la infraestructura
        print(f"Paso {paso} | Fase actual: {estado_luces}")
        
        # Retardo artificial introducido para facilitar la monitorización humana en el entorno de desarrollo
        time.sleep(0.2) 
        paso += 1

except Exception as e:
    # Control de excepciones para evitar cierres abruptos por errores de lectura
    print(f"Error durante la simulación: {e}")

finally:
    # 5. Cierre seguro de las comunicaciones y liberación de puertos
    print("\nCerrando conexión TraCI...")
    sys.stdout.flush() 
    
    try:
        traci.close()
    except:
        pass
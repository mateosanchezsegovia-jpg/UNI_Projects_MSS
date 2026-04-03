"""
=============================================================================
PROYECTO NEXAFLOW: AUDITORÍA Y REGISTRO DE DATOS (FASES 4 Y 5)
=============================================================================
Descripción:
Este script se encarga de ejecutar la IA entrenada en modo de inferencia pura 
(sin aprendizaje) o de lanzar el escenario "Baseline" estático para poder 
compararlos. 

Funciones clave:
1. CARGA DE MODELOS: Permite seleccionar y cargar pesos neuronales guardados 
   previamente (ej. el agente de 60k o 90k pasos).
2. AUTOMATIZACIÓN DE ARCHIVOS: Actúa como el recolector de datos del sistema. 
   En cada paso, guarda la telemetría del entorno y la exporta al finalizar a 
   un archivo CSV estructurado para su posterior análisis.

Dependencias:
- stable_baselines3
- pandas
- rsu_gymnasium (El entorno creado anteriormente)

Instrucciones:
Ejecutar tras haber iniciado 'nexaFlow_eval.py'. Descomentar la prueba que 
se desee ejecutar en el bloque final del código.
=============================================================================
"""
import gymnasium as gym
import numpy as np
import zmq
import json
import pandas as pd
from stable_baselines3 import DQN
from rsu_gymnasium import NexaFlowEnv

def ejecutar_auditoria(nombre_modelo=None, pasos=1000):
    """
    Si nombre_modelo es None, corre el 'Baseline' (Semáforo fijo).
    Si tiene nombre, carga la IA.
    """
    # Se instancia el entorno base de la intersección
    env = NexaFlowEnv()
    id_prueba = nombre_modelo if nombre_modelo else "Baseline_Fijo"
    
    print(f"\n[🔬] INICIANDO PRUEBA: {id_prueba}")
    
    # Carga de modelo o modo manual
    model = None
    if nombre_modelo:
        # Carga el cerebro del agente DQN para predecir acciones
        model = DQN.load(nombre_modelo, env=env)
        print(f"[*] Modelo {nombre_modelo} cargado correctamente.")
    else:
        print("[*] Ejecutando Semáforo Estático (Sin IA).")

    obs, _ = env.reset()
    log_data = []

    try:
        # Bucle de ejecución de la prueba
        for i in range(pasos):
            # Decisión: IA (predicción) o Nada (0)
            if model:
                # 'deterministic=True' asegura que la IA tome la mejor decisión aprendida (sin exploración aleatoria)
                action, _ = model.predict(obs, deterministic=True)
            else:
                action = 0 # El semáforo sigue su ciclo de cruce.net.xml
            
            # Se aplica la acción y se extrae el nuevo estado
            obs, reward, terminated, truncated, info = env.step(action)
            
            # Guardamos métricas brutas en memoria (RAM)
            log_data.append({
                'paso': i,
                'velocidad': obs[0],
                'co2': obs[1],
                'fase': obs[2],
                'ttc': info.get('ttc', 999)
            })
            
            # Imprime por consola cada 100 pasos para dar fe de vida
            if i % 100 == 0: print(f"  > Progreso: {i}/{pasos} pasos")
            
            # Corta el bucle si el entorno reporta que no quedan vehículos
            if terminated or truncated: break

    finally:
        env.close()

    # Guardar resultados en el disco duro (Fase 5 del proyecto)
    df = pd.DataFrame(log_data)
    nombre_archivo = f"results_{id_prueba}.csv"
    df.to_csv(nombre_archivo, index=False)
    print(f"[✔️] Datos guardados en: {nombre_archivo}")

if __name__ == "__main__":
    # 1. Prueba el Baseline (Semáforo normal)
    # ejecutar_auditoria(nombre_modelo=None)
    
    # 2. Prueba el Agente de 60k 
    # ejecutar_auditoria(nombre_modelo="agente_nexaflow_v2_1")
    
    # 3. Prueba el Agente de 90k 
    ejecutar_auditoria(nombre_modelo="agente_nexaflow_v2_2")
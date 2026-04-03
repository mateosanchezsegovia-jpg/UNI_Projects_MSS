"""
=============================================================================
PROYECTO NEXAFLOW: ENTORNO GYMNASIUM Y AGENTE DQN (CAPA 4)
=============================================================================
Descripción:
Este script representa la Inteligencia Artificial del sistema (Inteligencia en 
el Borde o Edge Computing). Envuelve la comunicación de red en un entorno 
estándar de Gymnasium para entrenar un modelo de Aprendizaje por Refuerzo (DQN).

Funciones clave:
1. TRADUCCIÓN DE ESTADOS (Capa 4): Ingesta los mensajes JSON por el puerto 5555 
   (SUB) y los convierte en un vector matemático observable por la red neuronal.
2. FUNCIÓN DE RECOMPENSA (Estrategia ECO): Evalúa las acciones pasadas basándose 
   en una fórmula que premia la velocidad sostenida y castiga las emisiones de CO2.
3. FILTRO DE ESTABILIDAD (V2.1): Intercepta las decisiones del agente y las 
   bloqueea si intenta cambiar de fase demasiado rápido, asegurando un mínimo de 
   5 segundos por fase para mantener la seguridad vial.
4. EMISIÓN DE ÓRDENES (Capa 4 -> Capa 3): Publica las decisiones definitivas 
   (Mantener o Cambiar) por el puerto 5556 (PUB) para que el script puente las 
   inyecte en el simulador.

Dependencias necesarias:
- gymnasium
- stable_baselines3
- pyzmq
- numpy

Instrucciones de uso:
Ejecutar este script como el programa principal de entrenamiento. En otra 
terminal, el script puente ('nexaFlow_train.py') debe estar ejecutándose 
para que el ciclo de datos (Handshake) se complete y comience la simulación.
=============================================================================
"""
import gymnasium as gym
from gymnasium import spaces
import numpy as np
import zmq
import json
from stable_baselines3 import DQN

class NexaFlowEnv(gym.Env):
    """
    Entorno RSU NexaFlow V2.1: Incluye consciencia de fase y 
    filtro de estabilidad para evitar cambios de fase ultra-rápidos.
    """
    def __init__(self):
        super(NexaFlowEnv, self).__init__()
        
        # 1. MDP: Espacio de Acción (Discreto). El agente solo puede tomar dos decisiones:
        # 0: Mantener la fase semafórica actual
        # 1: Cambiar a la siguiente fase del ciclo
        self.action_space = spaces.Discrete(2)
        
        # 2. OBSERVACIÓN: Espacio de estado continuo. 
        # Define los límites [mínimo, máximo] de las 3 variables que lee la IA:
        # [Velocidad Media (0 a 120 km/h), CO2 Total (0 a 100k mg), Fase Actual (0 a 4)]
        self.observation_space = spaces.Box(
            low=np.array([0.0, 0.0, 0.0], dtype=np.float32), 
            high=np.array([120.0, 100000.0, 4.0], dtype=np.float32),
            dtype=np.float32
        )
        
        # 3. RED V2X (ZeroMQ) - Configuración de sockets bidireccionales
        self.context = zmq.Context()
        
        # Socket para ESCUCHAR (Ingesta de estado del cruce)
        self.socket_sub = self.context.socket(zmq.SUB)
        self.socket_sub.setsockopt(zmq.RCVTIMEO, 5000) # Timeout 5s para evitar cuelgues si SUMO se cierra
        self.socket_sub.connect("tcp://localhost:5555")
        self.socket_sub.setsockopt_string(zmq.SUBSCRIBE, "telemetria")
        
        # Socket para HABLAR (Envío de decisiones al controlador físico)
        self.socket_pub = self.context.socket(zmq.PUB)
        self.socket_pub.bind("tcp://*:5556")
        
        # 4. PARÁMETROS DE CONTROL DE TRÁFICO
        self.count = 0 # Contador global del episodio
        self.pasos_en_fase_actual = 0 # Temporizador de la fase activa
        self.min_pasos_fase = 50  # Umbral del filtro de estabilidad: 50 pasos * 0.1s/paso = 5 segundos físicos
        
        print("[*] Entorno RSU inicializado con Filtro de Estabilidad (5s).")

    def _get_obs_and_ttc(self):
        """Recupera datos de la Capa 3 mediante ZMQ, los limpia y devuelve (observacion, ttc)."""
        try:
            # Escucha el paquete JSON que llega desde el puente TraCI
            msg = self.socket_sub.recv_string()
            data = json.loads(msg.split(' ', 1)[1])
            
            # Construye el array matemático exacto que la red neuronal DQN espera procesar
            obs = np.array([
                data.get("velocidad_kmh", 0.0),
                data.get("co2", 0.0),
                float(data.get("fase", 0.0))
            ], dtype=np.float32)
            
            # Extrae el TTC independientemente porque se usa para penalizar, no como observación directa
            return obs, data.get("ttc", 999.0)
        except Exception:
            # En caso de error de red, devolvemos un estado neutro seguro para evitar que la IA crashee
            return np.array([0.0, 0.0, 0.0], dtype=np.float32), 999.0

    def reset(self, seed=None, options=None):
        """Reinicia el entorno al comenzar un nuevo episodio de entrenamiento."""
        super().reset(seed=seed)
        self.count = 0
        self.pasos_en_fase_actual = 0
        
        # Despertar al simulador (Protocolo Handshake inicial)
        self.socket_pub.send_string("orden 0")
        
        # Ingesta el primer estado para arrancar el proceso de decisión de Markov
        obs, _ = self._get_obs_and_ttc()
        return obs, {}

    def step(self, action):
        """
        Ciclo principal de la IA: Recibe una acción elegida por la red neuronal, 
        la filtra por seguridad, la ejecuta y calcula la recompensa obtenida.
        """
        # --- FILTRO DE ESTABILIDAD (Capa de Seguridad Lógica) ---
        # Solo permitimos el cambio si la IA lo pide (action == 1) Y ha pasado el tiempo mínimo de seguridad
        accion_final = 0
        if action == 1:
            if self.pasos_en_fase_actual >= self.min_pasos_fase:
                accion_final = 1 # Cambio aprobado
                self.pasos_en_fase_actual = 0 # Reiniciamos el temporizador de fase
            else:
                accion_final = 0 # Bloqueamos el cambio por ser demasiado pronto (ignora a la IA)
                self.pasos_en_fase_actual += 1
        else:
            # Si la IA decide mantener la fase (action == 0), simplemente sumamos tiempo
            self.pasos_en_fase_actual += 1
            accion_final = 0

        # Enviar orden definitiva a la Capa 3 (Simulador)
        self.socket_pub.send_string(f"orden {accion_final}")
        
        # Obtener el nuevo estado del tráfico generado como consecuencia de nuestra acción
        obs, ttc = self._get_obs_and_ttc()
        
        # --- CÁLCULO DE RECOMPENSA (NexaFlow ECO) ---
        # R = (Velocidad / 10) - (CO2 / 10000)
        # Prioriza que los coches crucen a velocidad constante minimizando detenciones y humo
        reward = (obs[0] / 10.0) - (obs[1] / 10000.0)
        
        # Castigo severo si el actuador ha puesto en riesgo a los vehículos (TTC bajo)
        if ttc < 3.0: 
            reward -= 5.0 # Penalización por riesgo de colisión
            
        # Penalización extra si la IA intenta cambiar fase antes de tiempo (opcional)
        # if action == 1 and accion_final == 0: reward -= 0.1 

        self.count += 1
        
        # Finalizar episodio tras 1000 pasos (100 segundos simulados) para iniciar otro ciclo
        truncated = self.count >= 1000
        terminated = False
        
        return obs, reward, terminated, truncated, {"ttc": ttc}

    def close(self):
        """Apagado seguro y liberación de la red local."""
        self.socket_pub.close()
        self.socket_sub.close()
        self.context.term()

# ==========================================
# MOTOR DE ENTRENAMIENTO DQN
# ==========================================
if __name__ == "__main__":
    # Instanciamos el entorno definido arriba
    entorno = NexaFlowEnv()
    
    # Configuramos el modelo de Aprendizaje Profundo (DQN)
    # MlpPolicy: Utiliza una red neuronal densa estándar
    # buffer_size: Capacidad de la IA para recordar "experiencias" pasadas
    # learning_rate: Qué tan rápido ajusta la red sus pesos neuronales en cada paso
    modelo = DQN(
        "MlpPolicy", 
        entorno, 
        verbose=1, 
        buffer_size=20000, 
        learning_rate=1e-3,
        device="cpu" # En Mac M1/M2/M3 puedes probar "mps" para aceleración por hardware
    )
    
    print("\n" + "="*50)
    print("INICIANDO ENTRENAMIENTO NEXAFLOW V2.1 (CON FILTRO)")
    print("="*50 + "\n")
    
    try:
        # Iniciamos el bucle de entrenamiento. 60k pasos permiten sobrepasar la fase 
        # de "Caos Inicial" y entrar en la de "Despertar Logístico" de tu arquitectura.
        modelo.learn(total_timesteps=60000)
        
        # Guardamos el cerebro entrenado en un archivo para su uso posterior en inferencia
        modelo.save("agente_nexaflow_v2_1")
        print("\n[!] Entrenamiento completado. Modelo guardado.")
    except KeyboardInterrupt:
        # Permite cancelar el entrenamiento a medias presionando Ctrl+C sin perder el progreso
        print("\n[!] Entrenamiento pausado manualmente. Guardando...")
        modelo.save("agente_interrumpido")
    finally:
        entorno.close()
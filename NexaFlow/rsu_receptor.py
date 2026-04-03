"""
=============================================================================
PROYECTO NEXAFLOW: NODO RECEPTOR RSU (CAPA 3 - COMUNICACIONES V2X)
=============================================================================
Descripción:
Este script emula la lógica centralizada de la Unidad de Carretera (Road-Side Unit).
Actúa como un nodo Suscriptor (SUB) mediante ZeroMQ, escuchando en tiempo real
la telemetría emitida por los vehículos en el simulador SUMO.

Funciones clave:
1. Conexión asíncrona: Se conecta al puerto TCP 5555 sin requerir un handshake
   complejo (arquitectura brokerless).
2. Filtrado de tópicos: Solo procesa los mensajes etiquetados con la palabra 
   clave "telemetria", ignorando ruido de red irrelevante.
3. Deserialización: Convierte el payload en formato JSON nuevamente a un 
   diccionario de Python para su futuro uso en el algoritmo DQN.

Dependencias:
- pyzmq (pip install pyzmq)

Instrucciones:
Ejecutar en una terminal separada MIENTRAS el script principal (emisor)
está corriendo la simulación de SUMO.
=============================================================================
"""

import zmq
import json
import sys

# 1. Configuración del socket ZeroMQ (Capa 3 - Receptor RSU)
context = zmq.Context()
socket = context.socket(zmq.SUB)

# Nos conectamos al puerto local donde el simulador está emitiendo
puerto = "5555"
socket.connect(f"tcp://localhost:{puerto}")

# 2. Suscripción a tópicos específicos
# Le decimos al socket que solo escuche los mensajes que empiecen por "telemetria"
topico = "telemetria"
socket.setsockopt_string(zmq.SUBSCRIBE, topico)

print(f"[*] RSU NexaFlow iniciado.")
print(f"[*] Escuchando telemetría V2X en tcp://localhost:{puerto}...")
print("-" * 50)

try:
    # 3. Bucle de escucha infinita (Memoria central del RSU)
    while True:
        # Recibimos el mensaje completo (bloquea hasta que llega un paquete)
        mensaje = socket.recv_string()
        
        # Separamos la palabra clave (tópico) de los datos reales (payload)
        # El 1 indica que solo queremos separar por el primer espacio
        topico_recibido, payload_json = mensaje.split(' ', 1)
        
        # Convertimos el texto JSON a un diccionario de Python
        datos = json.loads(payload_json)
        
        # Extraemos las variables para mostrarlas por pantalla
        paso = datos.get("paso")
        coche = datos.get("coche_id")
        velocidad = datos.get("velocidad_kmh")
        co2 = datos.get("co2")
        
        print(f"[Paso {paso}] Vehículo: {coche} | Velocidad: {velocidad} km/h | Emisiones CO2: {co2} mg/s")

except KeyboardInterrupt:
    # Captura "Ctrl+C" para cerrar el script limpiamente
    print("\n[*] Interrupción manual. Apagando RSU...")

finally:
    # 4. Cierre seguro de puertos
    socket.close()
    context.term()
    sys.exit()
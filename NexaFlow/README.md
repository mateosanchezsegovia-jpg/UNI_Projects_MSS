# **NexaFLOW** 🚦🧠
Sistema avanzado de control semafórico inteligente basado en Deep Reinforcement Learning (DQN) y comunicaciones V2X (ZeroMQ) sobre el motor físico Eclipse SUMO y Plexe.

Este proyecto implementa una arquitectura de Edge Computing capaz de sincronizar el tráfico en tiempo real, logrando un 100% de fluidez vehicular y reduciendo las emisiones de CO2 mediante estrategias Eco-Driving.

## 🚀 *1. GUÍA RÁPIDA DE GIT - NexaFlow*
Sigue este orden exacto para que el proyecto y los archivos estén siempre sincronizados entre todos los miembros del equipo.

### 1️⃣ Clonar y crear el proyecto (Solo la primera vez)

Abre la terminal en tu carpeta de la universidad y escribe:

```bash
git clone [URL_DEL_REPOSITORIO]
cd [NOMBRE_DEL_REPO]
```

---

### 2️⃣ Uso de las ramas

Trabajamos por separado para no romper el código común.

#### 🔹 Moverte a tu rama

```bash
git checkout tu-nombre
```

#### 🔹 Si la rama no existe aún

```bash
git checkout -b tu-nombre
```

#### 🔹 Comprobar en qué rama estás

```bash
git status
```

---


### 3️⃣ Sincronizar y actualizar (Pull y Push)

#### 📥 TRAER cambios (PULL)

Si un compañero ha subido algo a `main` y lo necesitas en tu proyecto:

```bash
git pull origin main
```

#### 📤 SUBIR tus cambios (PUSH)

Cuando tu código funcione:

##### 1️⃣ Preparar archivos

```bash
git add .
```

##### 2️⃣ Guardar cambios

```bash
git commit -m "Explicacion de lo que has hecho"
```

##### 3️⃣ Subir cambios

```bash
git push origin tu-nombre
```

---

### ⚠️ NOTA IMPORTANTE

Cualquier archivo nuevo que crees debe estar dentro de la carpeta del repositorio.

Si no está dentro, no se subirá cuando hagas `push` y tus compañeros no lo recibirán al hacer `pull`.

---

## ⚙️ *2. Requisitos e Instalación*

### 0️⃣ Instalación del Software Base

Antes de clonar el código, tu ordenador debe tener instalados dos programas fundamentales:

1. **Python 3.8 o superior:** Descárgalo desde [python.org](https://www.python.org/downloads/).
2. **Plexe-SUMO:** Este proyecto no usa el SUMO normal, sino **Plexe** (una versión modificada que permite simular *platooning* o convoyes de vehículos).
    📌 Nota de compatibilidad vital: > Este proyecto ha sido desarrollado utilizando la rama main del repositorio oficial de Plexe-SUMO (Build v1_1_0+34047-03aa6ed4fd4 de Feb 2024).

    Para garantizar que el entorno de Inteligencia Artificial (y las físicas de Platooning) funcionen correctamente, debes clonar e instalar Plexe directamente desde el repositorio oficial de su creador:
    ```bash
    git clone https://github.com/michele-segata/plexe-sumo.git
    ```
    ⚠️ ATENCIÓN: NO instaléis la versión estándar de Eclipse SUMO desde su web, ya que carece de los modelos cinemáticos V2X necesarios para este proyecto.

(Asegúrate de apuntar a la misma ruta de Plexe en la variable SUMO_HOME del Paso 3)

---

### 1️⃣ Configuración Vital: Variable SUMO_HOME

Todos los scripts necesitan saber dónde está instalado Plexe-SUMO en tu ordenador. Debes configurar esta variable antes de ejecutar cualquier archivo. 

#### 🍏 En Mac / 🐧 Linux (Terminal):

```bash
export SUMO_HOME="/ruta/absoluta/a/tu/carpeta/plexe-sumo"
```

(Recomendación: Añade la línea anterior al final de tu archivo ~/.zshrc o ~/.bashrc para que se guarde para siempre).

#### 🪟 En Windows (PowerShell / CMD):

En PowerShell:

```bash
$env:SUMO_HOME="C:\ruta\absoluta\a\tu\carpeta\plexe-sumo"
```

En CMD:

```bash
set SUMO_HOME=C:\ruta\absoluta\a\tu\carpeta\plexe-sumo
```

---

### 2️⃣ Instalar Dependencias de Python

Abre la terminal en la carpeta del proyecto. Se recomienda usar un entorno virtual (`venv`) para no crear conflictos con otros proyectos:

#### 1. Crear el entorno virtual:

```bash
python -m venv venv
```
#### 2. Activar el entorno virtual:

En Mac/Linux: 

```bash
source venv/bin/activate
```

En Windows (CMD/PowerShell): 
```bash
venv\Scripts\activate
```

#### 3. Instalar las librerías:

```bash
pip install -r requirements.txt
```

---

## 🚀 *3. Guía de Uso de NexaFLOW*

La arquitectura requiere ejecutar simultáneamente la Capa Física (SUMO) y la Capa Lógica (IA) en dos terminales distintas para establecer la comunicación V2X.

---

### Opción A: Evaluación Visual y Auditoría (SUMO-GUI)

Para ver a la IA controlando el tráfico en tiempo real y generar los datos de la simulación:

#### 1️⃣ Abre la Terminal 1 y lanza el entorno visual: 

```bash
python nexaFlow_eval.py
```

#### 2️⃣ Abre la Terminal 2 y lanza el evaluador: 

```bash
python rsu_gym_eval.py
```

(Nota: Edita el archivo rsu_gym_eval.py al final del código para elegir qué agente evaluar).

---

### Opción B: Generar Informe Comparativo (KPIs)

Una vez evaluados distintos modelos (ej. Semáforo fijo vs IA), se habrán generado varios archivos .csv. Para compararlos:

#### En la terminal ejecuta: 

```bash
python nexa_cmp.py
```

(Esto imprimirá la tabla comparativa de velocidades y CO2 en pantalla y guardará el REPORTE_FINAL).

---

### Opción C: Entrenamiento de un nuevo modelo (Sin Interfaz)

Para entrenar un cerebro artificial desde cero más rápido:

#### 1️⃣ Abre la Terminal 1: 

```bash
python nexaFlow_train.py
```

#### 2️⃣ Abre la Terminal 2: 

```bash
python rsu_gymnasium.py
```

---

## 📂 *4. Estructura del Proyecto*

- cruce.net.xml / rutas.rou.xml: Topología de la intersección y demanda de tráfico.
- main_v*.py: Scripts de las fases iniciales (Extracción TraCI básica).
- nexaFlow_*.py: Nodos emisores ZeroMQ. Actúan como puente entre el simulador y la IA.
- rsu_gym*.py: Entorno y scripts del Agente DQN de Inteligencia Artificial.
- nexa_cmp.py: Herramienta de Data Science para generar la comparativa final.
- agente_nexaflow_v2_2.zip: Pesos de la red neuronal del modelo definitivo (Perfil ECO - 90k pasos).

---

## 👥 *5. Autores y Equipo de Desarrollo*

Este proyecto ha sido diseñado y desarrollado colaborativamente para [Nombre de la Asignatura / Universidad / Trabajo Final].

* **Mateo Sánchez Segovia** - *Ingeniería V2X y Modelado de IA (DQN)* - [Perfil de GitHub](https://github.com/mateosanchezsegovia-jpg)
* **[Nombre del Compañero 1]** - *[Rol o contribución principal, ej: Diseño del entorno SUMO / Análisis de Datos]* - [Perfil de GitHub](https://github.com/usuario-comp1)
* **[Nombre del Compañero 2]** - *[Rol o contribución principal]* - [Perfil de GitHub](https://github.com/usuario-comp2)

---
*NexaFLOW © 2026 - Innovando en la movilidad urbana del futuro.*
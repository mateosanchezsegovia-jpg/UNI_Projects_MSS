"""
=============================================================================
PROYECTO NEXAFLOW: CÁLCULO DE KPIS Y TABLA COMPARATIVA (FASE 5)
=============================================================================
Descripción:
Este script final automatiza el análisis técnico de las simulaciones. Busca 
todos los archivos de resultados generados por el script de auditoría y calcula 
los Indicadores Clave de Rendimiento (KPIs) del proyecto.

Funciones clave:
1. AGREGACIÓN: Lee masivamente los archivos CSV generados ('results_*.csv').
2. CÁLCULO DE KPIS: Obtiene las medias y sumatorias totales (Velocidad Media, 
   Emisiones Totales de CO2, Sucesos de Peligro TTC, Porcentaje de Fluidez).
3. EXPORTACIÓN: Imprime una tabla comparativa en formato Markdown en la consola 
   y guarda un archivo maestro con el reporte final.

Dependencias:
- pandas
- tabulate (pip install tabulate)

Instrucciones:
Ejecutar este script tras haber corrido varias auditorías (ej. Baseline vs IA).
El script generará automáticamente el veredicto técnico final.
=============================================================================
"""
import pandas as pd
import glob
import os

def generar_comparativa():
    print("\n" + "="*50)
    print("📊 INFORME COMPARATIVO NEXAFLOW")
    print("="*50)

    # Aseguramos que busque los CSV en el mismo directorio del script
     # Busca todos los CSV en el directorio que empiecen por "results_"
    carpeta_actual = os.path.dirname(os.path.abspath(__file__))
    ruta_busqueda = os.path.join(carpeta_actual, "results_*.csv")
    archivos = glob.glob(ruta_busqueda)
   
    resumen_final = []

    for archivo in archivos:
        df = pd.read_csv(archivo)
        
        # Limpia el nombre del archivo para usarlo como etiqueta de la configuración
        nombre = archivo.replace("results_", "").replace(".csv", "")
        
        # Cálculo de los KPIs del proyecto (Sostenibilidad, Rendimiento y Seguridad)
        resumen_final.append({
            "Configuración": nombre,
            "Vel. Media (km/h)": round(df['velocidad'].mean(), 2),
            "CO2 Total (mg)": round(df['co2'].sum(), 0),
            "Peligro (TTC < 3s)": len(df[df['ttc'] < 3.0]),
            "Fluidez (%)": round((len(df[df['velocidad'] > 10]) / len(df)) * 100, 1)
        })

    if not resumen_final:
        print("[!] No se encontraron archivos CSV de resultados.")
        return

    # Crear tabla comparativa mediante DataFrame
    df_final = pd.DataFrame(resumen_final)
    
    # Ordenar los resultados priorizando la velocidad media más alta
    df_final = df_final.sort_values(by="Vel. Media (km/h)", ascending=False)
    
    # Imprimir en consola en formato visual (Markdown)
    print(df_final.to_markdown(index=False)) # Requiere 'pip install tabulate'
    
    # Guardar reporte final consolidado para documentación externa
    df_final.to_csv("REPORTE_FINAL_NEXAFLOW.csv", index=False)
    print("\n[💾] Resumen final guardado en 'REPORTE_FINAL_NEXAFLOW.csv'")

if __name__ == "__main__":
    generar_comparativa()
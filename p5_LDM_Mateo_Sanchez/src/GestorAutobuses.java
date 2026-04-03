import java.util.ArrayList; // Importante: añade esto arriba del todo si falta

public class GestorAutobuses {
    // 1. Creamos la lista para guardar los buses en memoria
    private ArrayList<Autobús> lista = new ArrayList<>();

    public void crearAutobus(String matricula, int capacidad) {
        // Lógica real: crear el objeto y guardarlo
        Autobús nuevo = new Autobús(); // O new Autobus(matricula, capacidad) si tienes ese constructor
        nuevo.setMatricula(matricula);
        nuevo.setCapacidad(capacidad);
        
        lista.add(nuevo);
        System.out.println("--> SISTEMA: Autobús " + matricula + " guardado correctamente.");
    }

    public ArrayList<Autobús> getLista() {
        return lista;
    }
}
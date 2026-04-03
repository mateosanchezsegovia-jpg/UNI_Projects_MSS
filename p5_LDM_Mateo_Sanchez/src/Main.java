public class Main {
    public static void main(String[] args) {
        // 1. Inicializar el sistema
        GestorAutobuses miGestor = new GestorAutobuses();
        Pantalla_GestionFlota miPantalla = new Pantalla_GestionFlota(miGestor);
        
        // 2. Probar la creación de un autobús
        miPantalla.mostrarFormulario();
        
        // 3. Comprobar que se ha guardado
        System.out.println("\n--- COMPROBACIÓN FINAL ---");
        System.out.println("Lista de autobuses en el sistema:");
        System.out.println(miGestor.getLista());
    }
}
import java.util.Scanner; // Importante: añade esto arriba

public class Pantalla_GestionFlota {
    private GestorAutobuses gestor;

    // El constructor recibe el gestor para poder llamarlo
    public Pantalla_GestionFlota(GestorAutobuses gestor) {
        this.gestor = gestor;
    }

    public void mostrarFormulario() {
        Scanner teclado = new Scanner(System.in);
        
        System.out.println("\n=== PANTALLA DE GESTIÓN DE FLOTA ===");
        System.out.print("Introduce la Matrícula: ");
        String mat = teclado.nextLine();
        
        System.out.print("Introduce la Capacidad: ");
        int cap = teclado.nextInt();
        
        // Llamada al gestor (Igual que en tu diagrama)
        gestor.crearAutobus(mat, cap);
    }
}
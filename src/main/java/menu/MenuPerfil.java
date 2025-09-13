package menu;

import modelo.*;
import servicios.VerificarInput;
import gestores.GestorIntercambio;

public class MenuPerfil {
    private final VerificarInput input;
    private final Usuario usuarioActual;
    private final GestorIntercambio gestor;

    public MenuPerfil(VerificarInput input, GestorIntercambio gestor, Usuario usuarioActual) {
        this.input = input;
        this.gestor = gestor;
        this.usuarioActual = usuarioActual;
    }

    public void ejecutarMenu() {
        while (true) {
            System.out.println("\n--- Perfil de Usuario ---");
            mostrarDatos();
            System.out.println("1) Modificar nombre");
            System.out.println("2) Modificar email");
            System.out.println("3) Cambiar contraseña");

            // Opciones específicas para Estudiantes. Se ampliara posteriormente.
            if (usuarioActual instanceof Estudiante) {
                System.out.println("4) Modificar carrera");
            }

            System.out.println("0) Volver");
            int sel = input.leerEntero("Opción: ", -1);

            switch (sel) {
                case 1:
                    usuarioActual.setNombreCompleto(input.leerLinea("Nuevo nombre: "));
                    System.out.println("Nombre actualizado.");
                    break;
                case 2:
                    usuarioActual.setEmail(input.leerLinea("Nuevo email: "));
                    System.out.println("Email actualizado.");
                    break;
                case 3:
                    usuarioActual.setPass(input.leerLinea("Nueva contraseña: "));
                    System.out.println("Contraseña actualizada.");
                    break;
                case 4:
                    if (usuarioActual instanceof Estudiante) {
                        Estudiante est = (Estudiante) usuarioActual;
                        est.setCarrera(input.leerLinea("Nueva carrera: "));
                        System.out.println("Carrera actualizada.");
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private void mostrarDatos() {
        System.out.println("Nombre: " + usuarioActual.getNombreCompleto());
        System.out.println("Email: " + usuarioActual.getEmail());
        System.out.println("Rol: " + usuarioActual.getRol());
        if (usuarioActual instanceof Estudiante) {
            Estudiante est = (Estudiante) usuarioActual;
            System.out.println("Carrera: " + est.getCarrera());
            System.out.println("Promedio: " + est.getPromedio());
            System.out.println("Semestres: " + est.getSemestresCursados());
        }
    }
}
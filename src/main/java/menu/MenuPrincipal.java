package menu;

import gestores.GestorIntercambio;
import gestores.ResultadoLogin;
import modelo.Usuario;
import servicios.VerificarInput;
/*
public class MenuPrincipal {

    private final VerificarInput input;
    private final GestorIntercambio gestor;

    public MenuPrincipal(VerificarInput input, GestorIntercambio gestor) {
        this.input = input;
        this.gestor = gestor;
    }

    public void iniciar() {
        while (true) {
            System.out.println("\n=== Sistema de Gestión de Intercambio ===");
            System.out.println("1) Iniciar sesión");
            System.out.println("2) Registrar estudiante");
            System.out.println("0) Salir");
            int op = input.leerEntero("Opción: ", -1);
            switch (op) {
                case 1:
                    iniciarSesion();
                    break;
                case 2:
                    registrarEstudiante();
                    break;
                case 0:
                    System.out.println("Hasta luego.");
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private void iniciarSesion() {
        System.out.println("\nInicie sesión en su cuenta");
        String rut = input.leerLinea("RUT: ");
        String pass = input.leerLinea("Contraseña: ");

        ResultadoLogin resultado = gestor.iniciarSesion(rut, pass);

        if (resultado.isExito()) {
            System.out.println("\nBienvenido, " + resultado.getUsuario().getNombreCompleto());
            redirigirUsuario(resultado.getUsuario());
        } else {
            System.out.println(resultado.getMensaje());
        }
    }

    private void registrarEstudiante() {
        System.out.println("\n--- Registro de nuevo estudiante ---");

        String rut = input.leerLinea("RUT: ").trim();
        if (!validarRut(rut, false)) {
            System.out.println("RUT inválido. Debe ser de 9 digitos sin puntos ni guión.");
            return;
        }

        String nombre   = input.leerLinea("Nombre completo: ");
        String email    = input.leerLinea("Email: ");
        String pass     = input.leerLinea("Contraseña: ");
        String carrera  = input.leerLinea("Carrera: ");
        double promedio = input.leerDouble("Promedio (1.0-7.0): ", 0.0);
        int semestres   = input.leerEntero("Semestres cursados: ", 0);

        gestor.registrarEstudiante(rut, nombre, email, pass, carrera, semestres, promedio);
    }

    private void redirigirUsuario(Usuario usuario) {
        // Creamos una única instancia de cada menú
        MenuPostulaciones menuPostulaciones = new MenuPostulaciones(input, gestor, usuario);
        MenuConvenios menuConvenios = new MenuConvenios(input, gestor, usuario, menuPostulaciones);
        MenuPerfil menuPerfil = new MenuPerfil(input, gestor, usuario); // Creamos la nueva instancia

        // Pasamos todas las instancias al constructor de MenuFunciones
        MenuFunciones menuFunciones = new MenuFunciones(input, gestor, usuario, menuConvenios, menuPostulaciones, menuPerfil);

        menuFunciones.ejecutarMenu();
        gestor.cerrarSesion();
    }

    private static boolean validarRut(String rut) {
        if (rut == null) return false;
        rut = rut.trim().toUpperCase();
        return rut.matches("^[0-9]{8}[0-9K]$");
    }

    private static boolean validarRut(String rut, boolean validarDV) {
        return validarRut(rut);
    }
}

 */
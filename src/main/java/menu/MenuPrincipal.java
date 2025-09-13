package menu;

import enums.Rol;
import gestores.GestorIntercambio;
import gestores.ResultadoLogin;
import modelo.Estudiante;
import modelo.Usuario;
import servicios.VerificarInput;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    private Connection connect() throws SQLException {
        // La URL de conexión a tu base de datos SQLite
        String url = "jdbc:sqlite:db/miBase.db";
        // Asegúrate de que la ruta sea correcta
        return DriverManager.getConnection(url);
    }

    public void probarSQL() throws SQLException {
        crearTablas();

        // Inserta un usuario de prueba si la tabla está vacía
        if (getTodosLosUsuarios().isEmpty()) {
            Usuario usuarioPrueba = new Usuario("12345678-9", "Juan Pérez", "juan@mail.cl", "pass123", Rol.ESTUDIANTE);
            insertarUsuario(usuarioPrueba);
            System.out.println("Usuario de prueba insertado.");
        }

        System.out.println("\n--- Lista de todos los usuarios ---");
        List<Usuario> muestra = getTodosLosUsuarios();
        mostrarListaUsuarios(muestra);

        // Ejemplo de eliminación
        System.out.println("\n--- Eliminando a Juan Pérez ---");
        eliminarUsuario("12345678-9");

        System.out.println("\n--- Lista de usuarios después de la eliminación ---");
        List<Usuario> muestraPostEliminacion = getTodosLosUsuarios();
        mostrarListaUsuarios(muestraPostEliminacion);
    }

    public void crearTablas() {
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios ("
                + "rut TEXT PRIMARY KEY, "
                + "nombre TEXT NOT NULL, "
                + "email TEXT NOT NULL UNIQUE, "
                + "pass TEXT NOT NULL, "
                + "rol TEXT NOT NULL);";

        String sqlConvenios = "CREATE TABLE IF NOT EXISTS convenios ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "universidad TEXT NOT NULL, "
                + "pais TEXT NOT NULL, "
                + "requisitos_academicos TEXT, "
                + "requisitos_economicos TEXT);";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlConvenios);
            System.out.println("Tablas creadas con éxito o ya existentes.");
        } catch (SQLException e) {
            System.out.println("Error al crear tablas: " + e.getMessage());
        }
    }

    public void insertarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (rut, nombre, email, pass, rol) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getRut());
            pstmt.setString(2, usuario.getNombreCompleto());
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getPass());
            pstmt.setString(5, usuario.getRol().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al insertar usuario: " + e.getMessage());
        }
    }

    public void eliminarUsuario(String rut) {
        String sql = "DELETE FROM usuarios WHERE rut = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rut);
            pstmt.executeUpdate();
            System.out.println("Usuario con RUT " + rut + " eliminado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
        }
    }

    public List<Usuario> getTodosLosUsuarios() {
        String sql = "SELECT rut, nombre, email, pass, rol FROM usuarios";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getString("rut"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("pass"),
                        Rol.valueOf(rs.getString("rol"))
                );
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    private void mostrarListaUsuarios(List<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }

        System.out.println("--------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-15s | %-30s | %-30s | %-12s | %-12s |%n",
                "RUT", "NOMBRE COMPLETO", "EMAIL", "ROL", "CLAVE");
        System.out.println("--------------------------------------------------------------------------------------------------------------------");

        for (Usuario u : usuarios) {
            System.out.printf("| %-15s | %-30s | %-30s | %-12s | %-12s |%n",
                    u.getRut(),
                    u.getNombreCompleto(),
                    u.getEmail(),
                    u.getRol(),
                    u.getPass());
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------");
    }
}
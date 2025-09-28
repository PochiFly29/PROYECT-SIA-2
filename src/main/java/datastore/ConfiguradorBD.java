package datastore;

import enums.Rol;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Asegúrate de tener este import
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Herramienta para inicializar la base de datos.
 * LIMPIA tablas antiguas, CREA la nueva estructura y CARGA datos desde CSV.
 * EJECUTAR ESTA CLASE UNA SOLA VEZ para preparar la base de datos.
 */
public class ConfiguradorBD {

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando configuración de la base de datos...");
            crearTablasConLimpieza();
            cargarProgramasIniciales();
            cargarDatosDesdeCSV();
            System.out.println("✅ ¡Base de datos configurada y cargada exitosamente!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error durante la configuración: " + e.getMessage());
        }
    }

    public static void crearTablasConLimpieza() throws SQLException {
        final String[] TABLAS = {"interacciones", "postulaciones", "estudiantes", "convenios", "programas", "usuarios"};
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            System.out.println("Limpiando tablas antiguas...");
            for (String tabla : TABLAS) {
                stmt.execute("DROP TABLE IF EXISTS " + tabla);
            }
            System.out.println(" > Limpieza completada.");

            System.out.println("Creando nueva estructura de tablas...");
            stmt.execute("CREATE TABLE usuarios (rut TEXT PRIMARY KEY, nombre TEXT NOT NULL, email TEXT NOT NULL UNIQUE, pass TEXT NOT NULL, rol TEXT NOT NULL, bloqueado INTEGER NOT NULL DEFAULT 0, intentos_fallidos INTEGER NOT NULL DEFAULT 0);");
            stmt.execute("CREATE TABLE estudiantes (rut_estudiante TEXT PRIMARY KEY, carrera TEXT NOT NULL, promedio REAL NOT NULL, semestres_cursados INTEGER NOT NULL, FOREIGN KEY(rut_estudiante) REFERENCES usuarios(rut) ON DELETE CASCADE);");
            stmt.execute("CREATE TABLE convenios (id_convenio TEXT PRIMARY KEY, universidad TEXT NOT NULL, pais TEXT NOT NULL, area TEXT NOT NULL, requisitos_academicos TEXT, requisitos_economicos TEXT);");
            stmt.execute("CREATE TABLE programas (id_programa INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, fecha_inicio TEXT NOT NULL, fecha_fin TEXT NOT NULL);");
            stmt.execute("CREATE TABLE postulaciones (id_postulacion INTEGER PRIMARY KEY AUTOINCREMENT, rut_estudiante TEXT NOT NULL, id_convenio TEXT NOT NULL, id_programa INTEGER NOT NULL, fecha_postulacion TEXT NOT NULL, estado TEXT NOT NULL, FOREIGN KEY(rut_estudiante) REFERENCES usuarios(rut), FOREIGN KEY(id_convenio) REFERENCES convenios(id_convenio), FOREIGN KEY(id_programa) REFERENCES programas(id_programa));");
            stmt.execute("CREATE TABLE interacciones (id_interaccion INTEGER PRIMARY KEY AUTOINCREMENT, id_postulacion INTEGER NOT NULL, rut_autor TEXT NOT NULL, tipo TEXT NOT NULL, titulo TEXT NOT NULL, fecha_hora TEXT NOT NULL, FOREIGN KEY(id_postulacion) REFERENCES postulaciones(id_postulacion) ON DELETE CASCADE, FOREIGN KEY(rut_autor) REFERENCES usuarios(rut));");
            System.out.println(" > Nueva estructura creada.");
        }
    }

    public static void cargarProgramasIniciales() throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM programas WHERE id_programa = 1;";
        String sqlInsert = "INSERT INTO programas(id_programa, nombre, fecha_inicio, fecha_fin) VALUES(1, 'Ciclo Intercambio 2025', '2025-01-01', '2025-12-31');";

        // CAMBIO CLAVE: El ResultSet ahora está dentro del try-with-resources
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlCheck)) {

            if (rs.next() && rs.getInt(1) == 0) {
                // Necesitamos un nuevo Statement para el executeUpdate, ya que el primero está ocupado con el ResultSet
                try (Statement updateStmt = conn.createStatement()) {
                    updateStmt.executeUpdate(sqlInsert);
                    System.out.println(" > Programa inicial 'Ciclo Intercambio 2025' cargado.");
                }
            } else {
                System.out.println(" > El programa inicial ya existe, no se realizaron cambios.");
            }
        } // Todos los recursos (conn, stmt, rs) se cierran aquí automáticamente.
    }

    public static void cargarDatosDesdeCSV() {
        System.out.println("Iniciando carga de datos desde archivos CSV...");
        cargarCSVConvenios("convenios.csv");
        cargarCSVUsuarios("usuarios.csv");
        cargarCSVPostulaciones("postulaciones.csv");
    }

    private static void cargarCSVConvenios(String rutaArchivo) {
        String sql = "INSERT INTO convenios (id_convenio, universidad, pais, area, requisitos_academicos, requisitos_economicos) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(";");
                for (int i = 0; i < 6; i++) {
                    pstmt.setString(i + 1, data[i]);
                }
                pstmt.executeUpdate();
            }
            System.out.println(" > Carga de convenios (" + rutaArchivo + ") completada.");
        } catch (IOException | SQLException e) {
            System.err.println("Error cargando convenios desde CSV: " + e.getMessage());
        }
    }

    private static void cargarCSVUsuarios(String rutaArchivo) {
        String sqlUsuario = "INSERT INTO usuarios (rol, rut, nombre, email, pass) VALUES (?, ?, ?, ?, ?)";
        String sqlEstudiante = "INSERT INTO estudiantes (rut_estudiante, carrera, semestres_cursados, promedio) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmtU = conn.prepareStatement(sqlUsuario);
             PreparedStatement pstmtE = conn.prepareStatement(sqlEstudiante)) {

            String line;
            br.readLine(); // Omitir encabezado
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] data = line.split(",");

                pstmtU.setString(1, data[0]);
                pstmtU.setString(2, data[1]);
                pstmtU.setString(3, data[2]);
                pstmtU.setString(4, data[3]);
                pstmtU.setString(5, data[4]);
                pstmtU.executeUpdate();

                if (Rol.valueOf(data[0]) == Rol.ESTUDIANTE) {
                    pstmtE.setString(1, data[1]);
                    pstmtE.setString(2, data[5]);
                    pstmtE.setInt(3, Integer.parseInt(data[6]));
                    pstmtE.setDouble(4, Double.parseDouble(data[7]));
                    pstmtE.executeUpdate();
                }
            }
            System.out.println(" > Carga de usuarios (" + rutaArchivo + ") completada.");
        } catch (IOException | SQLException | NumberFormatException e) {
            System.err.println("Error cargando usuarios desde CSV: " + e.getMessage());
        }
    }

    private static void cargarCSVPostulaciones(String rutaArchivo) {
        String sql = "INSERT INTO postulaciones (rut_estudiante, id_convenio, fecha_postulacion, estado, id_programa) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String line;
            br.readLine(); // Omitir encabezado
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                pstmt.setString(1, data[0]);
                pstmt.setString(2, data[1]);
                pstmt.setString(3, data[2]);
                pstmt.setString(4, "PENDIENTE");
                pstmt.setInt(5, 1);
                pstmt.executeUpdate();
            }
            System.out.println(" > Carga de postulaciones (" + rutaArchivo + ") completada.");
        } catch (IOException | SQLException e) {
            System.err.println("Error cargando postulaciones desde CSV: " + e.getMessage());
        }
    }
}
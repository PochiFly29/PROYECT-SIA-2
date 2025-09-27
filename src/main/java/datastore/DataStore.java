package datastore;

import enums.*;
import modelo.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    private final Map<String, Usuario> usuariosPorRut = new HashMap<>();
    private final Map<Integer, Programa> programasPorId = new HashMap<>();
    private final Map<String, Convenio> conveniosPorId = new HashMap<>();
    // NOTA: No necesitamos un mapa de postulaciones aquí, ya que cada postulación
    // vivirá dentro de la lista de su programa correspondiente.

    public DataStore() throws SQLException {
        // El constructor orquesta toda la carga inicial.
        System.out.println("Iniciando carga de datos...");
        cargarUsuarios();
        cargarConvenios();
        cargarProgramas();
        cargarPostulacionesEInteracciones(); // Carga y enlaza las entidades más complejas.
        System.out.println("✅ Carga de datos completada.");
    }

    // =========================================================================
    //         PASO 1: MÉTODOS DE CARGA INICIAL (PRIVADOS)
    // =========================================================================

    private void cargarUsuarios() throws SQLException {
        String sql = "SELECT u.*, e.carrera, e.promedio, e.semestres_cursados " +
                "FROM usuarios u LEFT JOIN estudiantes e ON u.rut = e.rut_estudiante";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // ... (lógica de mapeo de Usuario/Estudiante como en la versión anterior) ...
                // Esta parte ya estaba bien diseñada.
                String rut = rs.getString("rut");
                String nombre = rs.getString("nombre");
                String email = rs.getString("email");
                String pass = rs.getString("pass");
                Rol rol = Rol.valueOf(rs.getString("rol"));

                Usuario usuario;
                if (rs.getString("carrera") != null) {
                    usuario = new Estudiante(rut, nombre, email, pass, rs.getString("carrera"), rs.getDouble("promedio"), rs.getInt("semestres_cursados"));
                } else {
                    usuario = new Usuario(rut, nombre, email, pass, rol);
                }
                usuario.setBloqueado(rs.getBoolean("bloqueado"));
                usuario.setIntentosFallidos(rs.getInt("intentos_fallidos"));

                usuariosPorRut.put(rut, usuario);
            }
        }
        System.out.println(" > Usuarios cargados: " + usuariosPorRut.size());
    }

    private void cargarConvenios() throws SQLException {
        String sql = "SELECT * FROM convenios";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // CAMBIO CLAVE: Usamos el constructor corregido de Convenio.
                Convenio c = new Convenio(
                        rs.getString("id_convenio"),
                        rs.getString("universidad"),
                        rs.getString("pais"),
                        rs.getString("area"),
                        rs.getString("requisitos_academicos"),
                        rs.getString("requisitos_economicos")
                );
                conveniosPorId.put(c.getId(), c);
            }
        }
        System.out.println(" > Convenios cargados: " + conveniosPorId.size());
    }

    private void cargarProgramas() throws SQLException {
        String sql = "SELECT * FROM programas";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Programa p = new Programa(
                        rs.getInt("id_programa"),
                        rs.getString("nombre"),
                        LocalDate.parse(rs.getString("fecha_inicio")),
                        LocalDate.parse(rs.getString("fecha_fin"))
                );
                programasPorId.put(p.getId(), p);
            }
        }
        System.out.println(" > Programas cargados: " + programasPorId.size());
    }

    // CAMBIO ESTRUCTURAL: Este método ahora carga y enlaza las postulaciones y sus interacciones.
    private void cargarPostulacionesEInteracciones() throws SQLException {
        String sql = "SELECT * FROM postulaciones";
        int count = 0;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idPrograma = rs.getInt("id_programa");
                Programa programaAsociado = programasPorId.get(idPrograma);

                // Si la postulación pertenece a un programa que existe en memoria
                if (programaAsociado != null) {
                    String idConvenio = rs.getString("id_convenio");
                    Convenio convenioAsociado = conveniosPorId.get(idConvenio);

                    // Solo creamos la postulación si su convenio también existe
                    if (convenioAsociado != null) {
                        Postulacion p = new Postulacion(
                                rs.getInt("id_postulacion"),
                                rs.getString("rut_estudiante"),
                                convenioAsociado, // Usamos el objeto Convenio, no el ID
                                LocalDate.parse(rs.getString("fecha_postulacion")),
                                EstadoPostulacion.valueOf(rs.getString("estado"))
                        );

                        // Cargar y enlazar sus interacciones
                        p.setInteracciones(getInteraccionesDePostulacion(p.getId()));

                        // Enlazar la postulación a su programa
                        programaAsociado.agregarPostulacion(p);
                        count++;
                    }
                }
            }
        }
        System.out.println(" > Postulaciones cargadas y enlazadas: " + count);
    }

    private List<Interaccion> getInteraccionesDePostulacion(int idPostulacion) throws SQLException {
        List<Interaccion> interacciones = new ArrayList<>();
        String sql = "SELECT * FROM interacciones WHERE id_postulacion = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPostulacion);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Usuario autor = usuariosPorRut.get(rs.getString("rut_autor"));
                if (autor != null) {
                    Interaccion i = new Interaccion(
                            rs.getInt("id_interaccion"),
                            autor,
                            TipoInteraccion.valueOf(rs.getString("tipo")),
                            rs.getString("titulo"),
                            LocalDateTime.parse(rs.getString("fecha_hora"))
                    );
                    interacciones.add(i);
                }
            }
        }
        return interacciones;
    }

    // =========================================================================
    //         PASO 2: MÉTODOS PÚBLICOS DE ACCESO Y MODIFICACIÓN
    // =========================================================================

    public Usuario getUsuarioPorRut(String rut) { return usuariosPorRut.get(rut); }
    public Convenio getConvenioPorId(String id) { return conveniosPorId.get(id); }
    public Programa getProgramaPorId(int id) { return programasPorId.get(id); }
    public List<Programa> getProgramas() { return new ArrayList<>(programasPorId.values()); }
    public List<Convenio> getConvenios() { return new ArrayList<>(conveniosPorId.values()); }

    public void addPostulacion(int idPrograma, Postulacion p) throws SQLException {
        // RECOMENDACIÓN: Usa AUTOINCREMENT en tu tabla de postulaciones.
        String sql = "INSERT INTO postulaciones (rut_estudiante, id_convenio, fecha_postulacion, estado, id_programa) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, p.getRutEstudiante());
            pstmt.setString(2, p.getConvenioSeleccionado().getId()); // Obtenemos el ID del objeto
            pstmt.setString(3, p.getFechaPostulacion().toString());
            pstmt.setString(4, p.getEstado().name());
            pstmt.setInt(5, idPrograma);
            pstmt.executeUpdate();

            // Obtenemos el ID generado por la BD y lo asignamos al objeto
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                p.setId(generatedKeys.getInt(1));
            }

            // Añadir a la caché en memoria
            Programa programa = programasPorId.get(idPrograma);
            if (programa != null) {
                programa.agregarPostulacion(p);
            }
        }
    }

    public void actualizarEstadoPostulacion(int idPostulacion, EstadoPostulacion nuevoEstado) throws SQLException {
        String sql = "UPDATE postulaciones SET estado = ? WHERE id_postulacion = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado.name());
            pstmt.setInt(2, idPostulacion);
            pstmt.executeUpdate();

            // Actualizar caché: buscar la postulación en todos los programas y actualizarla
            programasPorId.values().stream()
                    .flatMap(prog -> prog.getPostulaciones().stream())
                    .filter(post -> post.getId() == idPostulacion)
                    .findFirst()
                    .ifPresent(post -> post.setEstado(nuevoEstado));
        }
    }

    public void persistirTodosLosUsuarios() throws SQLException {
        // Esta lógica de guardar todos los usuarios al final puede mantenerse
        // si se hacen cambios como el estado de bloqueo o los intentos fallidos.
        String sql = "UPDATE usuarios SET nombre = ?, email = ?, pass = ?, bloqueado = ?, intentos_fallidos = ? WHERE rut = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Usuario u : usuariosPorRut.values()) {
                pstmt.setString(1, u.getNombreCompleto());
                pstmt.setString(2, u.getEmail());
                pstmt.setString(3, u.getPass());
                pstmt.setBoolean(4, u.isBloqueado());
                pstmt.setInt(5, u.getIntentosFallidos());
                pstmt.setString(6, u.getRut());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public void registrarEstudiante(Estudiante estudiante) throws SQLException {
        // 1. Persistir en la tabla de usuarios
        String sqlUsuario = "INSERT INTO usuarios (rut, nombre, email, pass, rol) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlUsuario)) {
            pstmt.setString(1, estudiante.getRut());
            pstmt.setString(2, estudiante.getNombreCompleto());
            pstmt.setString(3, estudiante.getEmail());
            pstmt.setString(4, estudiante.getPass());
            pstmt.setString(5, estudiante.getRol().name());
            pstmt.executeUpdate();
        }

        // 2. Persistir en la tabla de estudiantes
        String sqlEstudiante = "INSERT INTO estudiantes (rut_estudiante, carrera, promedio, semestres_cursados) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlEstudiante)) {
            pstmt.setString(1, estudiante.getRut());
            pstmt.setString(2, estudiante.getCarrera());
            pstmt.setDouble(3, estudiante.getPromedio());
            pstmt.setInt(4, estudiante.getSemestresCursados());
            pstmt.executeUpdate();
        }

        // 3. Actualizar la caché en memoria
        usuariosPorRut.put(estudiante.getRut(), estudiante);
    }

    public void actualizarNombreUsuario(String rut, String nuevoNombre) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ? WHERE rut = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoNombre);
            pstmt.setString(2, rut);
            pstmt.executeUpdate();

            // Sincronizar caché
            Usuario u = usuariosPorRut.get(rut);
            if (u != null) { u.setNombreCompleto(nuevoNombre); }
        }
    }

    public void actualizarEmailUsuario(String rut, String nuevoEmail) throws SQLException {
        String sql = "UPDATE usuarios SET email = ? WHERE rut = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEmail);
            pstmt.setString(2, rut);
            pstmt.executeUpdate();

            // Sincronizar caché
            Usuario u = usuariosPorRut.get(rut);
            if (u != null) { u.setEmail(nuevoEmail); }
        }
    }

    public void actualizarPasswordUsuario(String rut, String nuevaPassword) throws SQLException {
        String sql = "UPDATE usuarios SET pass = ? WHERE rut = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevaPassword);
            pstmt.setString(2, rut);
            pstmt.executeUpdate();

            // Sincronizar caché
            Usuario u = usuariosPorRut.get(rut);
            if (u != null) { u.setPass(nuevaPassword); }
        }
    }

    public void agregarInteraccionAPostulacion(int idPostulacion, Interaccion interaccion) throws SQLException {
        String sql = "INSERT INTO interacciones (id_postulacion, rut_autor, tipo, titulo, fecha_hora) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idPostulacion);
            pstmt.setString(2, interaccion.getAutor().getRut());
            pstmt.setString(3, interaccion.getTipo().name());
            pstmt.setString(4, interaccion.getTitulo());
            pstmt.setString(5, interaccion.getFechaHora().toString());
            pstmt.executeUpdate();

            // Asignar el ID autogenerado al objeto
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                interaccion.setId(generatedKeys.getInt(1));
            }

            // Sincronizar caché: buscar la postulación y añadirle la interacción
            programasPorId.values().stream()
                    .flatMap(prog -> prog.getPostulaciones().stream())
                    .filter(post -> post.getId() == idPostulacion)
                    .findFirst()
                    .ifPresent(post -> post.agregarInteraccion(interaccion));
        }
    }

    public void actualizarDatosAcademicosEstudiante(String rut, String carrera, int semestres, double promedio) throws SQLException {
        String sql = "UPDATE estudiantes SET carrera = ?, semestres_cursados = ?, promedio = ? WHERE rut_estudiante = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, carrera);
            pstmt.setInt(2, semestres);
            pstmt.setDouble(3, promedio);
            pstmt.setString(4, rut);
            pstmt.executeUpdate();

            // Sincronizar caché
            Usuario u = usuariosPorRut.get(rut);
            if (u instanceof Estudiante) {
                Estudiante e = (Estudiante) u;
                e.setCarrera(carrera);
                e.setSemestresCursados(semestres);
                e.setPromedio(promedio);
            }
        }
    }
}
package gestores;

import enums.EstadoPostulacion;
import enums.Rol;
import enums.TipoInteraccion;
import modelo.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * La clase {@code DataStore} gestiona la persistencia y la sincronización
 * de los datos del sistema de intercambio académico entre la base de datos
 * SQLite y las estructuras en memoria.
 * <p>
 * Se encarga de:
 * <ul>
 *   <li>Conectar y crear las tablas en la base de datos.</li>
 *   <li>Cargar y guardar información de usuarios, programas, convenios y postulaciones.</li>
 *   <li>Mantener la coherencia entre objetos en memoria y la base de datos.</li>
 * </ul>
 */
public class DataStore {

    /** URL de conexión a la base de datos SQLite */
    private final String URL = "jdbc:sqlite:gestion_intercambio.db";

    /** Mapa de usuarios indexados por RUT */
    private final Map<String, Usuario> usuariosPorRut = new HashMap<>();

    /** Mapa de convenios indexados por ID */
    private final Map<String, Convenio> conveniosPorId = new HashMap<>();

    /** Mapa de programas indexados por ID */
    private final Map<Integer, Programa> programasPorId = new HashMap<>();

    /** Mapa de postulaciones indexadas por ID */
    private final Map<String, Postulacion> postulacionesPorId = new HashMap<>();

    /**
     * Constructor por defecto de {@code DataStore}.
     * Inicializa los mapas internos.
     */
    public DataStore() {}

    // --- Métodos de Conexión y Persistencia (SQL) ---

    /**
     * Establece una conexión con la base de datos SQLite.
     *
     * @return la conexión activa
     * @throws SQLException si ocurre un error al conectar
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Crea las tablas necesarias en la base de datos si no existen.
     *
     * @throws SQLException si ocurre un error al ejecutar las sentencias SQL
     */
    public void crearTablas() throws SQLException {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Tabla usuarios
            String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (\n"
                    + " rut TEXT PRIMARY KEY,\n"
                    + " nombre TEXT NOT NULL,\n"
                    + " email TEXT NOT NULL,\n"
                    + " pass TEXT NOT NULL,\n"
                    + " rol TEXT NOT NULL CHECK( rol IN ('ESTUDIANTE', 'FUNCIONARIO', 'AUDITOR') ),\n"
                    + " bloqueado INTEGER NOT NULL DEFAULT 0,\n"
                    + " intentos_fallidos INTEGER NOT NULL DEFAULT 0\n"
                    + ");";

            // Tabla estudiantes_info
            String sqlEstudiantes = "CREATE TABLE IF NOT EXISTS estudiantes_info (\n"
                    + " rut_estudiante TEXT PRIMARY KEY,\n"
                    + " carrera TEXT NOT NULL,\n"
                    + " promedio REAL NOT NULL,\n"
                    + " semestres_cursados INTEGER NOT NULL,\n"
                    + " FOREIGN KEY (rut_estudiante) REFERENCES usuarios(rut)\n"
                    + ");";

            // Tabla programas
            String sqlProgramas = "CREATE TABLE IF NOT EXISTS programas (\n"
                    + " id_programa INTEGER PRIMARY KEY,\n"
                    + " nombre TEXT NOT NULL,\n"
                    + " fecha_inicio TEXT NOT NULL,\n"
                    + " fecha_fin TEXT NOT NULL\n"
                    + ");";

            // Tabla convenios
            String sqlConvenios = "CREATE TABLE IF NOT EXISTS convenios (\n"
                    + " id_convenio TEXT PRIMARY KEY,\n"
                    + " universidad TEXT NOT NULL,\n"
                    + " pais TEXT NOT NULL,\n"
                    + " area_estudios TEXT NOT NULL,\n"
                    + " requisitos_academicos TEXT NOT NULL,\n"
                    + " requisitos_economicos TEXT NOT NULL,\n"
                    + " id_programa INTEGER NOT NULL,\n"
                    + " FOREIGN KEY (id_programa) REFERENCES programas(id_programa)\n"
                    + ");";

            // Tabla postulaciones
            String sqlPostulaciones = "CREATE TABLE IF NOT EXISTS postulaciones (\n"
                    + " id_postulacion TEXT PRIMARY KEY,\n"
                    + " rut_estudiante TEXT NOT NULL,\n"
                    + " id_convenio TEXT NOT NULL,\n"
                    + " fecha_postulacion TEXT NOT NULL,\n"
                    + " estado TEXT NOT NULL CHECK( estado IN ('POR_REVISAR', 'REVISADA', 'PRESELECCIONADA', 'ACEPTADA', 'RECHAZADA', 'ABANDONADA') ),\n"
                    + " FOREIGN KEY (rut_estudiante) REFERENCES usuarios(rut),\n"
                    + " FOREIGN KEY (id_convenio) REFERENCES convenios(id_convenio)\n"
                    + ");";
            String sqlInteracciones = "CREATE TABLE IF NOT EXISTS interacciones (\n"
                    + " id_interaccion INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " id_postulacion TEXT NOT NULL,\n"
                    + " rut_autor TEXT NOT NULL,\n"
                    + " tipo TEXT NOT NULL CHECK( tipo IN ('COMENTARIO', 'DOCUMENTO') ),\n"
                    + " titulo TEXT NOT NULL,\n"
                    + " fecha_hora TEXT NOT NULL,\n"
                    + " FOREIGN KEY (id_postulacion) REFERENCES postulaciones(id_postulacion),\n"
                    + " FOREIGN KEY (rut_autor) REFERENCES usuarios(rut)\n"
                    + ");";
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlEstudiantes);
            stmt.execute(sqlProgramas);
            stmt.execute(sqlConvenios);
            stmt.execute(sqlPostulaciones);
            stmt.execute(sqlInteracciones);
        }
    }

    // ---------------- Carga de datos ----------------
    /**
     * Carga todos los datos desde la base de datos a las estructuras en memoria.
     * Realiza el enlace entre convenios, programas, estudiantes y postulaciones.
     *
     * @throws SQLException si ocurre un error al leer de la base de datos
     */
    public void cargarDatosDesdeBD() throws SQLException {
        usuariosPorRut.clear();
        conveniosPorId.clear();
        programasPorId.clear();
        postulacionesPorId.clear();

        getTodosLosUsuariosBD().forEach(u -> usuariosPorRut.put(u.getRut(), u));
        getTodosLosProgramasBD().forEach(p -> programasPorId.put(p.getId(), p));
        getTodosLosConveniosBD().forEach(c -> conveniosPorId.put(c.getId(), c));
        getTodasLasPostulacionesBD().forEach(p -> postulacionesPorId.put(p.getId(), p));

        // Enlazar los datos en memoria
        enlazarDatos();
    }

    /**
     * Enlaza los datos cargados en memoria, asignando convenios a programas,
     * postulaciones a estudiantes y agregando interacciones.
     *
     * @throws SQLException si ocurre un error al leer interacciones
     */
    private void enlazarDatos() throws SQLException {
        conveniosPorId.values().forEach(c -> {
            Programa programa = programasPorId.get(c.getIdPrograma());
            if (programa != null) programa.agregarConvenio(c);
        });

        postulacionesPorId.values().forEach(p -> {
            Usuario u = usuariosPorRut.get(p.getRutEstudiante());
            if (u instanceof Estudiante) ((Estudiante) u).agregarPostulacion(p);
            Convenio c = conveniosPorId.get(p.getIdConvenio());
            if (c != null) p.setConvenioSeleccionado(c);

            try {
                getInteraccionesPorPostulacionBD(p.getId()).forEach(p::agregarInteraccion);
            } catch (SQLException e) {
                System.out.println("Error al enlazar interacciones: " + e.getMessage());
            }
        });
    }

    // Métodos para leer de la base de datos
    /**
     * Obtiene todos los usuarios desde la base de datos.
     *
     * @return lista de usuarios
     * @throws SQLException si ocurre un error en la consulta
     */
    private List<Usuario> getTodosLosUsuariosBD() throws SQLException {
        // ... (Tu código de lectura de usuarios) ...
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, e.carrera, e.promedio, e.semestres_cursados FROM usuarios u LEFT JOIN estudiantes_info e ON u.rut = e.rut_estudiante";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String rut = rs.getString("rut");
                String nombre = rs.getString("nombre");
                String email = rs.getString("email");
                String pass = rs.getString("pass");
                Rol rol = Rol.valueOf(rs.getString("rol"));
                boolean bloqueado = rs.getInt("bloqueado") == 1;
                int intentosFallidos = rs.getInt("intentos_fallidos");

                Usuario usuario;
                if (rol == Rol.ESTUDIANTE) {
                    String carrera = rs.getString("carrera");
                    double promedio = rs.getDouble("promedio");
                    int semestres = rs.getInt("semestres_cursados");
                    usuario = new Estudiante(rut, nombre, email, pass, carrera, promedio, semestres);
                } else {
                    usuario = new Usuario(rut, nombre, email, pass, rol);
                }

                usuario.setBloqueado(bloqueado);
                usuario.setIntentosFallidos(intentosFallidos);
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }

    // Métodos  de escritura y sincronización

    /**
     * Obtiene todos los programas desde la base de datos.
     *
     * @return lista de programas
     * @throws SQLException si ocurre un error en la consulta
     */
    private List<Programa> getTodosLosProgramasBD() throws SQLException {
        // ... (Tu código de lectura de programas) ...
        List<Programa> programas = new ArrayList<>();
        String sql = "SELECT id_programa, nombre, fecha_inicio, fecha_fin FROM programas";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int idPrograma = rs.getInt("id_programa");
                String nombre = rs.getString("nombre");
                LocalDate fechaInicio = LocalDate.parse(rs.getString("fecha_inicio"));
                LocalDate fechaFin = LocalDate.parse(rs.getString("fecha_fin"));
                programas.add(new Programa(idPrograma, nombre, fechaInicio, fechaFin));
            }
        }
        return programas;
    }

    /**
     * Obtiene todos los convenios desde la base de datos.
     *
     * @return lista de convenios
     * @throws SQLException si ocurre un error en la consulta
     */
    private List<Convenio> getTodosLosConveniosBD() throws SQLException {
        // ... (Tu código de lectura de convenios) ...
        List<Convenio> convenios = new ArrayList<>();
        String sql = "SELECT id_convenio, universidad, pais, area_estudios, requisitos_academicos, requisitos_economicos, id_programa FROM convenios";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String idConvenio = rs.getString("id_convenio");
                String universidad = rs.getString("universidad");
                String pais = rs.getString("pais");
                String areaEstudios = rs.getString("area_estudios");
                String reqAcademicos = rs.getString("requisitos_academicos");
                String reqEconomicos = rs.getString("requisitos_economicos");
                int idPrograma = rs.getInt("id_programa");
                convenios.add(new Convenio(idConvenio, universidad, pais, areaEstudios, reqAcademicos, reqEconomicos, idPrograma));
            }
        }
        return convenios;
    }

    /**
     * Obtiene todas las postulaciones desde la base de datos.
     *
     * @return lista de postulaciones
     * @throws SQLException si ocurre un error en la consulta
     */
    private List<Postulacion> getTodasLasPostulacionesBD() throws SQLException {
        List<Postulacion> postulaciones = new ArrayList<>();
        String sql = "SELECT id_postulacion, rut_estudiante, id_convenio, fecha_postulacion, estado FROM postulaciones";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String idPostulacion = rs.getString("id_postulacion");
                String rutEstudiante = rs.getString("rut_estudiante");
                String idConvenio = rs.getString("id_convenio");
                LocalDate fechaPostulacion = LocalDate.parse(rs.getString("fecha_postulacion"));
                EstadoPostulacion estado = EstadoPostulacion.valueOf(rs.getString("estado"));
                postulaciones.add(new Postulacion(idPostulacion, rutEstudiante, idConvenio, fechaPostulacion, estado));
            }
        }
        return postulaciones;
    }

    /**
     * Obtiene todas las interacciones asociadas a una postulacion.
     *
     * @param idPostulacion ID de la postulacion
     * @return lista de interacciones
     * @throws SQLException si ocurre un error en la consulta
     */
    private List<Interaccion> getInteraccionesPorPostulacionBD(String idPostulacion) throws SQLException {
        List<Interaccion> interacciones = new ArrayList<>();
        String sql = "SELECT rut_autor, tipo, titulo, fecha_hora FROM interacciones WHERE id_postulacion = ? ORDER BY fecha_hora ASC";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idPostulacion);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String rutAutor = rs.getString("rut_autor");
                    TipoInteraccion tipo = TipoInteraccion.valueOf(rs.getString("tipo"));
                    String titulo = rs.getString("titulo");
                    LocalDateTime fechaHora = LocalDateTime.parse(rs.getString("fecha_hora"));
                    Usuario autor = usuariosPorRut.get(rutAutor);
                    if (autor != null) {
                        interacciones.add(new Interaccion(autor, tipo, titulo, fechaHora, null));
                    }
                }
            }
        }
        return interacciones;
    }

    // --- Métodos de escritura en la base de datos (transaccionales) ---

    /**
     * Guarda todos los datos en la base de datos, reemplazando los existentes.
     *
     * @throws SQLException si ocurre un error al guardar
     */
    public void guardarDatos() throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            conn.createStatement().execute("PRAGMA foreign_keys = OFF;");
            conn.createStatement().execute("DELETE FROM usuarios");
            conn.createStatement().execute("DELETE FROM estudiantes_info");
            conn.createStatement().execute("DELETE FROM programas");
            conn.createStatement().execute("DELETE FROM convenios");
            conn.createStatement().execute("DELETE FROM postulaciones");
            conn.createStatement().execute("DELETE FROM interacciones");
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");

            for (Usuario u : usuariosPorRut.values()) {
                insertarUsuario(conn, u);
                if (u instanceof Estudiante) insertarEstudiante(conn, (Estudiante) u);
            }
            for (Programa p : programasPorId.values()) insertarPrograma(conn, p);
            for (Convenio c : conveniosPorId.values()) insertarConvenio(conn, c);
            for (Postulacion p : postulacionesPorId.values()) {
                insertarPostulacion(conn, p);
                p.getInteracciones().forEach(i -> {
                    try {
                        insertarInteraccion(conn, i, p.getId());
                    } catch (SQLException e) {
                        System.out.println("Error al insertar interacción: " + e.getMessage());
                    }
                });
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println("Error al guardar datos: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Inserta un usuario en la base de datos.
     *
     * @param conn la conexión activa
     * @param usuario el usuario a insertar
     * @throws SQLException si ocurre un error al ejecutar el INSERT
     */
    private void insertarUsuario(Connection conn, Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (rut, nombre, email, pass, rol, bloqueado, intentos_fallidos) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getRut());
            pstmt.setString(2, usuario.getNombreCompleto());
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getPass());
            pstmt.setString(5, usuario.getRol().name());
            pstmt.setInt(6, usuario.isBloqueado() ? 1 : 0);
            pstmt.setInt(7, usuario.getIntentosFallidos());
            pstmt.executeUpdate();
        }
    }
    // ... (rest of insert methods for Estudiante, Programa, Convenio, Postulacion, Interaccion) ...
    private void insertarEstudiante(Connection conn, Estudiante estudiante) throws SQLException {
        String sql = "INSERT INTO estudiantes_info (rut_estudiante, carrera, promedio, semestres_cursados) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estudiante.getRut());
            pstmt.setString(2, estudiante.getCarrera());
            pstmt.setDouble(3, estudiante.getPromedio());
            pstmt.setInt(4, estudiante.getSemestresCursados());
            pstmt.executeUpdate();
        }
    }

    private void insertarPrograma(Connection conn, Programa programa) throws SQLException {
        String sql = "INSERT INTO programas (id_programa, nombre, fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, programa.getId());
            pstmt.setString(2, programa.getNombre());
            pstmt.setString(3, programa.getFechaInicio().toString());
            pstmt.setString(4, programa.getFechaFin().toString());
            pstmt.executeUpdate();
        }
    }

    /**
     * Inserta un convenio en la base de datos.
     *
     * @param conn    la conexión activa
     * @param convenio el convenio a insertar
     * @throws SQLException si ocurre un error al ejecutar el INSERT
     */
    private void insertarConvenio(Connection conn, Convenio convenio) throws SQLException {
        String sql = "INSERT INTO convenios (id_convenio, universidad, pais, area_estudios, requisitos_academicos, requisitos_economicos, id_programa) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, convenio.getId());
            pstmt.setString(2, convenio.getUniversidad());
            pstmt.setString(3, convenio.getPais());
            pstmt.setString(4, convenio.getArea());
            pstmt.setString(5, convenio.getRequisitosAcademicos());
            pstmt.setString(6, convenio.getRequisitosEconomicos());
            pstmt.setInt(7, convenio.getIdPrograma());
            pstmt.executeUpdate();
        }
    }

    /**
     * Inserta una postulación en la base de datos.
     *
     * @param conn        la conexión activa
     * @param postulacion la postulación a insertar
     * @throws SQLException si ocurre un error al ejecutar el INSERT
     */
    private void insertarPostulacion(Connection conn, Postulacion postulacion) throws SQLException {
        String sql = "INSERT INTO postulaciones (id_postulacion, rut_estudiante, id_convenio, fecha_postulacion, estado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postulacion.getId());
            pstmt.setString(2, postulacion.getRutEstudiante());
            pstmt.setString(3, postulacion.getIdConvenio());
            pstmt.setString(4, postulacion.getFechaPostulacion().toString());
            pstmt.setString(5, postulacion.getEstado().name());
            pstmt.executeUpdate();
        }
    }

    /**
     * Inserta una interacción asociada a una postulación en la base de datos.
     *
     * @param conn          la conexión activa
     * @param interaccion   la interacción a insertar
     * @param idPostulacion el ID de la postulación a la que pertenece
     * @throws SQLException si ocurre un error al ejecutar el INSERT
     */
    private void insertarInteraccion(Connection conn, Interaccion interaccion, String idPostulacion) throws SQLException {
        String sql = "INSERT INTO interacciones (id_postulacion, rut_autor, tipo, titulo, fecha_hora) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idPostulacion);
            pstmt.setString(2, interaccion.getAutor().getRut());
            pstmt.setString(3, interaccion.getTipo().name());
            pstmt.setString(4, interaccion.getTitulo());
            pstmt.setString(5, interaccion.getFechaHora().toString());
            pstmt.executeUpdate();
        }
    }

    // --- Métodos de sincronización bidireccional (en memoria y BD) ---

    /**
     * Agrega un usuario en memoria y en la base de datos.
     *
     * @param u usuario a agregar
     */
    public void addUsuario(Usuario u) {
        usuariosPorRut.put(u.getRut(), u);
        try (Connection conn = connect()) {
            insertarUsuario(conn, u);
            if (u instanceof Estudiante) insertarEstudiante(conn, (Estudiante) u);
        } catch (SQLException e) {
            System.out.println("Error al guardar usuario en la BD: " + e.getMessage());
        }
    }
    public void addConvenio(Convenio c) {
        conveniosPorId.put(c.getId(), c);
        try (Connection conn = connect()) {
            insertarConvenio(conn, c);
        } catch (SQLException e) {
            System.out.println("Error al guardar convenio en la BD: " + e.getMessage());
        }
    }
    public void addPrograma(Programa p) {
        programasPorId.put(p.getId(), p);
        try (Connection conn = connect()) {
            insertarPrograma(conn, p);
        } catch (SQLException e) {
            System.out.println("Error al guardar programa en la BD: " + e.getMessage());
        }
    }
    public void addPostulacion(Postulacion p) {
        postulacionesPorId.put(p.getId(), p);
        try (Connection conn = connect()) {
            insertarPostulacion(conn, p);
        } catch (SQLException e) {
            System.out.println("Error al guardar postulación en la BD: " + e.getMessage());
        }
    }
/*
    public void actualizarNombreUsuario(String rut, String nuevoNombre) {
        usuariosPorRut.get(rut).setNombreCompleto(nuevoNombre);
        String sql = "UPDATE usuarios SET nombre = ? WHERE rut = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoNombre);
            pstmt.setString(2, rut);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al actualizar nombre en la BD: " + e.getMessage());
        }
    }
*/
    public void actualizarNombreUsuario(String rut, String nuevoNombre) {
        Usuario u = usuariosPorRut.get(rut);
        if (u != null) {
            // Actualizar en memoria
            u.setNombreCompleto(nuevoNombre);

            // Actualizar en BD
            String sql = "UPDATE usuarios SET nombre = ? WHERE rut = ?";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nuevoNombre);
                pstmt.setString(2, rut);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error al actualizar nombre en la BD: " + e.getMessage());
            }
        }
    }


    public void actualizarEmailUsuario(String rut, String nuevoEmail) {
        usuariosPorRut.get(rut).setEmail(nuevoEmail);
        String sql = "UPDATE usuarios SET email = ? WHERE rut = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEmail);
            pstmt.setString(2, rut);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al actualizar email en la BD: " + e.getMessage());
        }
    }

    public void actualizarPasswordUsuario(String rut, String nuevaPass) {
        usuariosPorRut.get(rut).setPass(nuevaPass);
        String sql = "UPDATE usuarios SET pass = ? WHERE rut = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevaPass);
            pstmt.setString(2, rut);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al actualizar contraseña en la BD: " + e.getMessage());
        }
    }

    public void actualizarCarreraEstudiante(String rut, String nuevaCarrera) {
        Usuario u = usuariosPorRut.get(rut);
        if (u instanceof Estudiante) {
            ((Estudiante) u).setCarrera(nuevaCarrera);
            String sql = "UPDATE estudiantes_info SET carrera = ? WHERE rut_estudiante = ?";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nuevaCarrera);
                pstmt.setString(2, rut);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error al actualizar carrera en la BD: " + e.getMessage());
            }
        }
    }

    public void agregarInteraccionAPostulacion(String idPostulacion, Interaccion interaccion) {
        Postulacion p = postulacionesPorId.get(idPostulacion);
        if (p != null) {
            p.agregarInteraccion(interaccion);
            try (Connection conn = connect()) {
                insertarInteraccion(conn, interaccion, idPostulacion);
            } catch (SQLException e) {
                System.out.println("Error al guardar interacción en la BD: " + e.getMessage());
            }
        }
    }

    public void actualizarEstadoPostulacion(String idPostulacion, EstadoPostulacion nuevoEstado) {
        Postulacion p = postulacionesPorId.get(idPostulacion);
        if (p != null) {
            p.setEstado(nuevoEstado);
            String sql = "UPDATE postulaciones SET estado = ? WHERE id_postulacion = ?";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nuevoEstado.name());
                pstmt.setString(2, idPostulacion);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error al actualizar estado en la BD: " + e.getMessage());
            }
        }
    }

    public void actualizarEstadosPostulaciones(String rutEstudiante, String idPostulacionAExcluir, EstadoPostulacion nuevoEstado) {
        Usuario u = usuariosPorRut.get(rutEstudiante);
        if (u instanceof Estudiante) {
            Estudiante estudiante = (Estudiante) u;
            for (Postulacion p : estudiante.getPostulaciones()) {
                if (!Objects.equals(p.getId(), idPostulacionAExcluir)) {
                    p.setEstado(nuevoEstado);
                    actualizarEstadoPostulacion(p.getId(), nuevoEstado);
                }
            }
        }
    }

    // --- Métodos para obtener elementos (desde la memoria) ---

    /**
     * Obtiene un usuario por su RUT.
     *
     * @param rut RUT del usuario
     * @return el usuario correspondiente, o null si no existe
     */
    public Usuario getUsuarioPorRut(String rut) { return usuariosPorRut.get(rut); }
    public Convenio getConvenioPorId(String id) { return conveniosPorId.get(id); }
    public Programa getProgramaPorId(int id) { return programasPorId.get(id); }
    public Postulacion getPostulacionPorId(String id) { return postulacionesPorId.get(id); }

    public List<Usuario> getUsuarios() { return new ArrayList<>(usuariosPorRut.values()); }
    public List<Convenio> getConvenios() { return new ArrayList<>(conveniosPorId.values()); }
    public List<Programa> getProgramas() { return new ArrayList<>(programasPorId.values()); }
    public List<Postulacion> getPostulaciones() { return new ArrayList<>(postulacionesPorId.values()); }
}
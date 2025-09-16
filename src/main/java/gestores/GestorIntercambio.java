package gestores;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import enums.EstadoPostulacion;
import enums.Rol;
import enums.TipoInteraccion;
import modelo.*;


public class GestorIntercambio {

    private final String URL = "jdbc:sqlite:gestion_intercambio.db";
    private DataStore dataStore;

    public GestorIntercambio() {
        try {
            crearTablas();
            List<Usuario> usuarios = getTodosLosUsuarios();
            List<Programa> programas = getTodosLosProgramas();
            List<Convenio> convenios = getTodosLosConvenios();
            List<Postulacion> postulaciones = getTodasLasPostulaciones();

            this.dataStore = new DataStore(usuarios, convenios, programas, postulaciones);
            enlazarDatos();
            System.out.println("Datos cargados y enlazados en DataStore. Usuarios: " + dataStore.getUsuarios().size() + ", Convenios: " + dataStore.getConvenios().size() + ", Postulaciones: " + dataStore.getPostulaciones().size());

        } catch (SQLException e) {
            System.out.println("Error al cargar datos: " + e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Métodos para persistencia de datos (SQL)
    public void crearTablas() throws SQLException {
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("PRAGMA foreign_keys = ON;")) {
            pstmt.execute();
            String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (\n"
                    + " rut TEXT PRIMARY KEY,\n"
                    + " nombre TEXT NOT NULL,\n"
                    + " email TEXT NOT NULL,\n"
                    + " pass TEXT NOT NULL,\n"
                    + " rol TEXT NOT NULL CHECK( rol IN ('ESTUDIANTE', 'FUNCIONARIO', 'AUDITOR') ),\n"
                    + " bloqueado INTEGER NOT NULL DEFAULT 0,\n"
                    + " intentos_fallidos INTEGER NOT NULL DEFAULT 0\n"
                    + ");";
            String sqlEstudiantes = "CREATE TABLE IF NOT EXISTS estudiantes_info (\n"
                    + " rut_estudiante TEXT PRIMARY KEY,\n"
                    + " carrera TEXT NOT NULL,\n"
                    + " promedio REAL NOT NULL,\n"
                    + " semestres_cursados INTEGER NOT NULL,\n"
                    + " FOREIGN KEY (rut_estudiante) REFERENCES usuarios(rut)\n"
                    + ");";
            String sqlProgramas = "CREATE TABLE IF NOT EXISTS programas (\n"
                    + " id_programa INTEGER PRIMARY KEY,\n"
                    + " nombre TEXT NOT NULL,\n"
                    + " fecha_inicio TEXT NOT NULL,\n"
                    + " fecha_fin TEXT NOT NULL\n"
                    + ");";
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

            conn.createStatement().execute(sqlUsuarios);
            conn.createStatement().execute(sqlEstudiantes);
            conn.createStatement().execute(sqlProgramas);
            conn.createStatement().execute(sqlConvenios);
            conn.createStatement().execute(sqlPostulaciones);
            conn.createStatement().execute(sqlInteracciones);

        }
    }

    private void enlazarDatos() throws SQLException {
        // Enlazar los convenios a sus programas
        dataStore.getConvenios().forEach(c -> {
            Programa programa = dataStore.getProgramaPorId(c.getIdPrograma());
            if (programa != null) {
                programa.agregarConvenio(c);
            }
        });

        // Enlazar las postulaciones a estudiantes, convenios e interacciones
        dataStore.getPostulaciones().forEach(p -> {
            Usuario u = dataStore.getUsuarioPorRut(p.getRutEstudiante());
            if (u instanceof Estudiante) {
                ((Estudiante) u).agregarPostulacion(p);
            }
            Convenio c = dataStore.getConvenioPorId(p.getIdConvenio());
            if (c != null) {
                p.setConvenioSeleccionado(c);
            }
            try {
                getInteraccionesPorPostulacion(p.getId()).forEach(p::agregarInteraccion);
            } catch (SQLException e) {
                System.out.println("Error al enlazar interacciones para " + p.getId() + ": " + e.getMessage());
            }
        });
    }

    public void guardarDatos() {
        try {
            try (Connection conn = connect()) {
                conn.createStatement().execute("DELETE FROM usuarios");
                conn.createStatement().execute("DELETE FROM estudiantes_info");
                conn.createStatement().execute("DELETE FROM programas");
                conn.createStatement().execute("DELETE FROM convenios");
                conn.createStatement().execute("DELETE FROM postulaciones");
                conn.createStatement().execute("DELETE FROM interacciones");

                for (Usuario u : dataStore.getUsuarios()) {
                    insertarUsuario(u);
                    if (u instanceof Estudiante) {
                        insertarEstudiante((Estudiante) u);
                    }
                }
                for (Programa p : dataStore.getProgramas()) {
                    insertarPrograma(p);
                }
                for (Convenio c : dataStore.getConvenios()) {
                    insertarConvenio(c);
                }
                for (Postulacion p : dataStore.getPostulaciones()) {
                    insertarPostulacion(p);
                    for (Interaccion i : p.getInteracciones()) {
                        insertarInteraccion(i, p.getId());
                    }
                }
            }
            System.out.println("Datos guardados exitosamente.");
        } catch (SQLException e) {
            System.out.println("Error al guardar datos: " + e.getMessage());
        }
    }

    private void insertarUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (rut, nombre, email, pass, rol, bloqueado, intentos_fallidos) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

    private void insertarEstudiante(Estudiante estudiante) throws SQLException {
        String sql = "INSERT INTO estudiantes_info (rut_estudiante, carrera, promedio, semestres_cursados) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estudiante.getRut());
            pstmt.setString(2, estudiante.getCarrera());
            pstmt.setDouble(3, estudiante.getPromedio());
            pstmt.setInt(4, estudiante.getSemestresCursados());
            pstmt.executeUpdate();
        }
    }

    private void insertarPrograma(Programa programa) throws SQLException {
        String sql = "INSERT INTO programas (id_programa, nombre, fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, programa.getId());
            pstmt.setString(2, programa.getNombre());
            pstmt.setString(3, programa.getFechaInicio().toString());
            pstmt.setString(4, programa.getFechaFin().toString());
            pstmt.executeUpdate();
        }
    }

    private void insertarConvenio(Convenio convenio) throws SQLException {
        String sql = "INSERT INTO convenios (id_convenio, universidad, pais, area_estudios, requisitos_academicos, requisitos_economicos, id_programa) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

    private void insertarPostulacion(Postulacion postulacion) throws SQLException {
        String sql = "INSERT INTO postulaciones (id_postulacion, rut_estudiante, id_convenio, fecha_postulacion, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postulacion.getId());
            pstmt.setString(2, postulacion.getRutEstudiante());
            pstmt.setString(3, postulacion.getIdConvenio());
            pstmt.setString(4, postulacion.getFechaPostulacion().toString());
            pstmt.setString(5, postulacion.getEstado().name());
            pstmt.executeUpdate();
        }
    }

    private void insertarInteraccion(Interaccion interaccion, String idPostulacion) throws SQLException {
        String sql = "INSERT INTO interacciones (id_postulacion, rut_autor, tipo, titulo, fecha_hora) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idPostulacion);
            pstmt.setString(2, interaccion.getAutor().getRut());
            pstmt.setString(3, interaccion.getTipo().name());
            pstmt.setString(4, interaccion.getTitulo());
            pstmt.setString(5, interaccion.getFechaHora().toString());
            pstmt.executeUpdate();
        }
    }

    // --- Métodos para obtener datos de la base de datos ---
    public List<Usuario> getTodosLosUsuarios() throws SQLException {
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

    public List<Programa> getTodosLosProgramas() throws SQLException {
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

    public List<Convenio> getTodosLosConvenios() throws SQLException {
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

    public List<Postulacion> getTodasLasPostulaciones() throws SQLException {
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

    private List<Interaccion> getInteraccionesPorPostulacion(String idPostulacion) throws SQLException {
        List<Interaccion> interacciones = new ArrayList<>();
        String sql = "SELECT id_interaccion, rut_autor, tipo, titulo, fecha_hora FROM interacciones WHERE id_postulacion = ? ORDER BY fecha_hora ASC";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idPostulacion);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String rutAutor = rs.getString("rut_autor");
                    TipoInteraccion tipo = TipoInteraccion.valueOf(rs.getString("tipo"));
                    String titulo = rs.getString("titulo");
                    LocalDateTime fechaHora = LocalDateTime.parse(rs.getString("fecha_hora"));
                    Usuario autor = dataStore.getUsuarioPorRut(rutAutor);
                    if (autor != null) {
                        interacciones.add(new Interaccion(autor, tipo, titulo, fechaHora, null));
                    }
                }
            }
        }
        return interacciones;
    }

    // --- Métodos de lógica de negocio ---
    public ResultadoLogin iniciarSesion(String rut, String pass) {
        Usuario usuario = dataStore.getUsuarioPorRut(rut);
        if (usuario == null) {
            return new ResultadoLogin( "El RUT no está registrado.");
        }
        if (usuario.isBloqueado()) {
            return new ResultadoLogin("Su cuenta ha sido bloqueada. Contacte a un funcionario.");
        }
        if (usuario.getPass().equals(pass)) {
            usuario.setIntentosFallidos(0);
            return new ResultadoLogin(usuario);
        } else {
            usuario.setIntentosFallidos();
            if (usuario.getIntentosFallidos() >= 3) {
                usuario.setBloqueado(true);
                return new ResultadoLogin("Demasiados intentos fallidos. Su cuenta ha sido bloqueada.");
            }
            return new ResultadoLogin("Contraseña incorrecta. Intento " + usuario.getIntentosFallidos() + " de 3.");
        }
    }

    public void cerrarSesion() {

    }

    public void registrarEstudiante(String rut, String nombre, String email, String pass, String carrera, int semestres, double promedio) {
        if (dataStore.getUsuarioPorRut(rut) != null) {
            System.out.println("El RUT ya se encuentra registrado.");
            return;
        }
        Estudiante nuevoEstudiante = new Estudiante(rut, nombre, email, pass, carrera, promedio, semestres);
        dataStore.addUsuario(nuevoEstudiante);
        System.out.println("Estudiante " + nombre + " registrado exitosamente.");
    }

    public List<Programa> getProgramasVigentes() {
        return dataStore.getProgramas();
    }

    public Optional<Convenio> buscarConvenio(String idConvenio) {
        return Optional.ofNullable(dataStore.getConvenioPorId(idConvenio));
    }

    public Programa getProgramaDeConvenio(Convenio convenio) {
        return dataStore.getProgramaPorId(convenio.getIdPrograma());
    }

    public boolean postular(Estudiante estudiante, Convenio convenio) {
        if (dataStore.getPostulaciones().stream().anyMatch(p -> p.getRutEstudiante().equals(estudiante.getRut()) && p.getIdConvenio().equals(convenio.getId()))) {
            return false;
        }
        String nuevoId = "P" + (dataStore.getPostulaciones().size() + 1);
        Postulacion nuevaPostulacion = new Postulacion(nuevoId, estudiante.getRut(), convenio.getId(), LocalDate.now(), EstadoPostulacion.POR_REVISAR);
        nuevaPostulacion.setConvenioSeleccionado(convenio);
        dataStore.addPostulacion(nuevaPostulacion);
        estudiante.agregarPostulacion(nuevaPostulacion);
        return true;
    }


    public List<Postulacion> getPostulaciones(String tipoFiltro, String valorFiltro) {
        Stream<Postulacion> postulacionesStream = dataStore.getPostulaciones().stream();

        switch (tipoFiltro) {
            case "rut":
                // Ordena por fecha de postulación de forma descendente (las más recientes primero)
                return postulacionesStream
                        .filter(p -> p.getRutEstudiante().equals(valorFiltro))
                        .sorted(Comparator.comparing(Postulacion::getFechaPostulacion).reversed())
                        .collect(Collectors.toList());
            case "estado":
                EstadoPostulacion estado = EstadoPostulacion.valueOf(valorFiltro.toUpperCase());
                // Si el estado es "POR_REVISAR", ordena por fecha ascendente (las más antiguas primero)
                if (estado == EstadoPostulacion.POR_REVISAR) {
                    return postulacionesStream
                            .filter(p -> p.getEstado() == estado)
                            .sorted(Comparator.comparing(Postulacion::getFechaPostulacion)) // Orden ascendente
                            .collect(Collectors.toList());
                }
                // Para otros estados, usa el orden por ID
                return postulacionesStream
                        .filter(p -> p.getEstado() == estado)
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getId().substring(1))))
                        .collect(Collectors.toList());
            case "convenio":
                // Ordena por ID de forma descendente (las más recientes primero)
                return postulacionesStream
                        .filter(p -> p.getIdConvenio().equals(valorFiltro))
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getId().substring(1))))
                        .collect(Collectors.toList());
            default:
                // Orden general por ID de forma descendente
                return postulacionesStream
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getId().substring(1))))
                        .collect(Collectors.toList());
        }
    }

    public Estudiante buscarEstudiantePorPostulacion(String idPostulacion) {
        Postulacion p = dataStore.getPostulacionPorId(idPostulacion);
        if (p == null) return null;
        Usuario u = dataStore.getUsuarioPorRut(p.getRutEstudiante());
        if (u instanceof Estudiante) {
            return (Estudiante) u;
        }
        return null;
    }

    public void descartarOtrasPostulaciones(Estudiante estudiante, String idPostulacionAceptada) {
        for (Postulacion p : dataStore.getPostulaciones()) {
            if (p.getRutEstudiante().equals(estudiante.getRut()) && !p.getId().equals(idPostulacionAceptada)) {
                p.setEstado(EstadoPostulacion.RECHAZADA);
            }
        }
    }

    public void agregarInteraccionAPostulacion(String idPostulacion, Interaccion interaccion) {
        Postulacion p = dataStore.getPostulacionPorId(idPostulacion);
        if (p != null) {
            p.agregarInteraccion(interaccion);
        }
    }

    public void crearProgramaPorDefecto() {
        try (Connection conn = connect()) {
            String sql = "INSERT INTO programas (id_programa, nombre, fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Programa programa = new Programa(1, "Programa General de Intercambio", LocalDate.of(2025, 1, 1), LocalDate.of(2027, 12, 31));
                pstmt.setInt(1, programa.getId());
                pstmt.setString(2, programa.getNombre());
                pstmt.setString(3, programa.getFechaInicio().toString());
                pstmt.setString(4, programa.getFechaFin().toString());
                pstmt.executeUpdate();
                System.out.println("Programa por defecto '" + programa.getId() + "' creado exitosamente.");
            }
        } catch (SQLException e) {
            if (!e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Error al crear el programa por defecto: " + e.getMessage());
            }
        }
    }

    public void cargarDatosIniciales() {

        // Crear un funcionario y un auditor
        Usuario funcionario = new Usuario("11111111-1", "Ana Gomez", "ana.gomez@sgie.cl", "pass123", Rol.FUNCIONARIO);
        Usuario auditor = new Usuario("22222222-2", "Pedro Soto", "pedro.soto@sgie.cl", "pass123", Rol.AUDITOR);
        dataStore.addUsuario(funcionario);
        dataStore.addUsuario(auditor);

        String[] carreras = {"Ingeniería Civil", "Ingeniería en Informática", "Ingeniería Comercial", "Derecho", "Medicina", "Arquitectura", "Diseño"};
        List<Convenio> convenios = dataStore.getConvenios();

        for (int i = 1; i <= 50; i++) {
            String rut = String.format("90%07d-K", i);
            String nombre = "Estudiante " + i;
            String email = "estudiante" + i + "@mail.cl";
            String pass = "pass123";
            String carrera = carreras[i % carreras.length];
            double promedio = 5.0 + (Math.random() * 2.0); // Promedio entre 5.0 y 7.0
            int semestres = (int) (Math.random() * 8) + 1; // Entre 1 y 8 semestres

            Estudiante estudiante = new Estudiante(rut, nombre, email, pass, carrera, promedio, semestres);
            dataStore.addUsuario(estudiante);

            // Crear una o dos postulaciones
            int numPostulaciones = (int) (Math.random() * 2) + 1; // 1 o 2
            for (int j = 0; j < numPostulaciones; j++) {
                if (convenios.isEmpty()) break;

                int convenioIndex = (int) (Math.random() * convenios.size());
                Convenio convenio = convenios.get(convenioIndex);

                // Verificar si el estudiante ya tiene una postulación a este convenio
                if (!estudiante.getPostulaciones().stream().anyMatch(p -> p.getIdConvenio().equals(convenio.getId()))) {
                    String idPostulacion = "P" + (dataStore.getPostulaciones().size() + 1);
                    Postulacion postulacion = new Postulacion(idPostulacion, rut, convenio.getId(), LocalDate.now(), EstadoPostulacion.POR_REVISAR);
                    postulacion.setConvenioSeleccionado(convenio); // Enlazar el objeto Convenio
                    dataStore.addPostulacion(postulacion);
                    estudiante.agregarPostulacion(postulacion);
                }
            }
        }
        System.out.println("Datos de prueba (50 estudiantes, 2 usuarios, postulaciones) cargados en memoria.");
    }

    public void recargarDatos() {
        try {
            List<Usuario> usuarios = getTodosLosUsuarios();
            List<Programa> programas = getTodosLosProgramas();
            List<Convenio> convenios = getTodosLosConvenios();
            List<Postulacion> postulaciones = getTodasLasPostulaciones();

            this.dataStore = new DataStore(usuarios, convenios, programas, postulaciones);
            enlazarDatos();
            System.out.println("Datos recargados y enlazados. Usuarios: " + dataStore.getUsuarios().size() + ", Convenios: " + dataStore.getConvenios().size() + ", Postulaciones: " + dataStore.getPostulaciones().size());
        } catch (SQLException e) {
            System.out.println("Error al recargar datos: " + e.getMessage());
        }
    }

    public void cargarConveniosDesdeArchivo(String rutaArchivo) {
        List<Programa> programasVigentes = dataStore.getProgramas();
        crearProgramaPorDefecto();
        if (programasVigentes.isEmpty()) {
            System.out.println("No hay programas vigentes para asociar los convenios. Por favor, cree un programa primero.");
            return;
        }
        int idPrograma = programasVigentes.get(0).getId();

        System.out.println("Cargando convenios desde el archivo: " + rutaArchivo);
        try (Stream<String> lineas = Files.lines(Paths.get(rutaArchivo))) {
            lineas.forEach(linea -> {
                String[] partes = linea.split(";");
                if (partes.length == 8) {
                    try {
                        String id = partes[0].trim();
                        String universidad = partes[1].trim();
                        String pais = partes[2].trim();
                        String area = partes[3].trim();
                        String reqAcademicos = partes[4].trim();
                        String reqEconomicos = partes[5].trim();
                        LocalDate fechaInicio = LocalDate.parse(partes[6].trim());
                        LocalDate fechaFin = LocalDate.parse(partes[7].trim());

                        if (dataStore.getConvenioPorId(id) == null) {
                            Convenio nuevoConvenio = new Convenio(id, universidad, pais, area, reqAcademicos, reqEconomicos, idPrograma);
                            dataStore.addConvenio(nuevoConvenio);
                            insertarConvenio(nuevoConvenio);
                            System.out.println("Convenio " + id + " cargado y guardado.");
                        } else {
                            System.out.println("Convenio " + id + " ya existe en la base de datos. Saltando.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error al procesar la línea: " + linea + ". Error: " + e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}
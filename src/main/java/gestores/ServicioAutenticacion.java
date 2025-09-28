package gestores;

import datastore.DataStore;
import enums.Rol;
import modelo.Estudiante;
import modelo.ResultadoLogin;
import modelo.Usuario;
import java.sql.SQLException;

/**
 * **Servicio de Dominio para Autenticación y Gestión de Usuarios.**
 * <p>Implementa la lógica de negocio relacionada con el acceso, la seguridad
 * (bloqueo por intentos) y las operaciones de perfil para todas las entidades
 * {@link Usuario} y {@link Estudiante}.</p>
 */
public class ServicioAutenticacion {
    private final DataStore dataStore;

    /**
     * Constructor que inicializa el servicio inyectando la dependencia del {@code DataStore}.
     * @param dataStore La instancia única de {@link DataStore} que contiene la caché de datos.
     */
    public ServicioAutenticacion(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Procesa el intento de inicio de sesión de un usuario, aplicando las reglas
     * de seguridad de la aplicación.
     * <p>Reglas de negocio:</p>
     * <ul>
     * <li>Verifica la existencia y el estado de bloqueo del usuario.</li>
     * <li>Si la contraseña es incorrecta, incrementa los intentos fallidos.</li>
     * <li>Si los intentos fallidos alcanzan el límite (3), **bloquea** al usuario.</li>
     * <li>Si es exitoso, reinicia el contador de intentos.</li>
     * </ul>
     * @param rut El RUT del usuario que intenta iniciar sesión.
     * @param pass La contraseña ingresada.
     * @return Un objeto {@link ResultadoLogin} que contiene el usuario si es exitoso, o un mensaje de error.
     */
    public ResultadoLogin iniciarSesion(String rut, String pass) {
        Usuario usuario = dataStore.getUsuarioPorRut(rut);
        if (usuario == null) return new ResultadoLogin("El RUT no está registrado.");
        if (usuario.isBloqueado()) return new ResultadoLogin("Su cuenta ha sido bloqueada. Contacte a un funcionario.");

        if (usuario.validarCredenciales(pass)) {
            usuario.setIntentosFallidos(0);
            return new ResultadoLogin(usuario);
        } else {
            usuario.incrementarIntentosFallidos(); // Usamos el método corregido
            if (usuario.getIntentosFallidos() >= 3) {
                usuario.setBloqueado(true);
            }
            // La persistencia del cambio se hará al final de la sesión
            return new ResultadoLogin("Contraseña incorrecta. Intento " + usuario.getIntentosFallidos() + " de 3.");
        }
    }

    /**
     * Registra un nuevo estudiante en el sistema.
     * Crea un objeto {@link Estudiante} y delega su persistencia al {@code DataStore}
     * en las tablas `usuarios` y `estudiantes`.
     * @param rut El RUT del estudiante.
     * @param nombre El nombre completo.
     * @param email El correo electrónico.
     * @param password La contraseña inicial.
     * @param carrera La carrera.
     * @param semestres El número de semestres cursados.
     * @param promedio El promedio académico.
     * @throws SQLException Si ocurre un error al intentar registrar en la base de datos.
     */
    public void registrarEstudiante(String rut, String nombre, String email, String password, String carrera, int semestres, double promedio) throws SQLException {
        Estudiante nuevo = new Estudiante(rut, nombre, email, password, carrera, promedio, semestres);
        dataStore.registrarEstudiante(nuevo);
    }

    /**
     * Actualiza el nombre y/o el email de un usuario en el sistema.
     * @param rut El RUT del usuario a actualizar.
     * @param nuevoNombre El nuevo nombre completo.
     * @param nuevoEmail El nuevo correo electrónico.
     * @throws SQLException Si ocurre un error al intentar actualizar en la base de datos.
     */
    public void actualizarPerfilUsuario(String rut, String nuevoNombre, String nuevoEmail) throws SQLException {
        dataStore.actualizarNombreUsuario(rut, nuevoNombre);
        dataStore.actualizarEmailUsuario(rut, nuevoEmail);
    }

    /**
     * Actualiza la contraseña de un usuario en el sistema.
     * @param rut El RUT del usuario a actualizar.
     * @param nuevaPassword La nueva contraseña (debe estar hasheada antes de llamar a este método).
     * @throws SQLException Si ocurre un error al intentar actualizar en la base de datos.
     */
    public void actualizarPasswordUsuario(String rut, String nuevaPassword) throws SQLException {
        dataStore.actualizarPasswordUsuario(rut, nuevaPassword);
    }

    /**
     * Persiste en la base de datos los cambios realizados en los objetos {@link Usuario}
     * de la caché, como el estado de bloqueo o el contador de intentos fallidos.
     * <p>Debe ser llamado al cerrar la aplicación o al finalizar la sesión del usuario.</p>
     */
    public void guardarCambiosDeUsuarios() {
        try {
            dataStore.persistirTodosLosUsuarios();
            System.out.println("✅ Datos de usuarios guardados en la BD.");
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar datos de usuarios: " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo usuario con rol administrativo (Funcionario o Auditor) en el sistema.
     * @param rut El RUT del nuevo usuario.
     * @param nombre El nombre.
     * @param email El correo.
     * @param pass La contraseña.
     * @param rol El rol asignado (debe ser FUNCIONARIO o AUDITOR).
     * @throws SQLException Si ocurre un error de persistencia en la base de datos.
     * @throws IllegalArgumentException Si el rol no es administrativo o si el RUT ya existe.
     */
    public void crearUsuarioAdministrativo(String rut, String nombre, String email, String pass, Rol rol) throws SQLException, IllegalArgumentException {
        if (rol != Rol.FUNCIONARIO && rol != Rol.AUDITOR) {
            throw new IllegalArgumentException("Este método solo puede crear Funcionarios o Auditores.");
        }
        if (dataStore.getUsuarioPorRut(rut) != null) {
            throw new IllegalArgumentException("El RUT ingresado ya existe en el sistema.");
        }

        Usuario nuevoUsuario = new Usuario(rut, nombre, email, pass, rol);
        dataStore.crearUsuario(nuevoUsuario); // Necesitarás crear este método en DataStore
    }

    /**
     * Bloquea un usuario existente, impidiendo su acceso al sistema.
     * @param rut El RUT del usuario a bloquear.
     * @throws SQLException Si ocurre un error al guardar el estado de bloqueo en la base de datos.
     * @throws IllegalArgumentException Si no se encuentra el usuario con el RUT especificado.
     */
    public void bloquearUsuario(String rut) throws SQLException, IllegalArgumentException {
        Usuario usuario = dataStore.getUsuarioPorRut(rut);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró un usuario con el RUT especificado.");
        }
        usuario.setBloqueado(true);
        dataStore.actualizarUsuario(usuario); // Necesitarás un método genérico para guardar cambios en DataStore
    }

    /**
     * Verifica si un RUT ya está registrado como usuario en el sistema.
     * @param rut El RUT a verificar.
     * @return {@code true} si el usuario existe, {@code false} en caso contrario.
     */
    public boolean existeUsuario(String rut) {
        return dataStore.getUsuarioPorRut(rut) != null;
    }

    /**
     * Actualiza la información académica de un estudiante.
     * @param rut El RUT del estudiante.
     * @param carrera La nueva carrera.
     * @param semestres El nuevo número de semestres cursados.
     * @param promedio El nuevo promedio.
     * @throws SQLException Si ocurre un error al intentar actualizar en la base de datos.
     */
    public void actualizarDatosAcademicos(String rut, String carrera, int semestres, double promedio) throws SQLException {
        dataStore.actualizarDatosAcademicosEstudiante(rut, carrera, semestres, promedio);
    }

}
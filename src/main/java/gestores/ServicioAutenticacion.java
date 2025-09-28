package gestores;

import datastore.DataStore;
import modelo.Estudiante;
import modelo.ResultadoLogin;
import modelo.Usuario;
import java.sql.SQLException;

/**
 * Servicio encargado de la autenticación y gestión de usuarios dentro del sistema.
 * <p>
 * Proporciona métodos para:
 * <ul>
 *   <li>Iniciar sesión de usuarios con validación de credenciales.</li>
 *   <li>Registrar estudiantes en el sistema.</li>
 *   <li>Actualizar datos personales, académicos y contraseñas.</li>
 *   <li>Persistir cambios de usuarios en la base de datos.</li>
 * </ul>
 * Este servicio se apoya en la clase {@link DataStore} para acceder y modificar la información.
 * </p>
 */
public class ServicioAutenticacion {
    /** Acceso centralizado a los datos del sistema. */
    private final DataStore dataStore;

    /**
     * Constructor que inicializa el servicio con el {@link DataStore}.
     *
     * @param dataStore instancia de {@link DataStore} para persistencia de datos.
     */
    public ServicioAutenticacion(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Inicia sesión verificando credenciales de un usuario.
     * <p>
     * - Si el usuario no existe, devuelve un error.<br>
     * - Si está bloqueado, no permite el acceso.<br>
     * - Si la contraseña es incorrecta, incrementa los intentos fallidos.<br>
     * - Bloquea la cuenta tras 3 intentos fallidos.<br>
     * </p>
     *
     * @param rut  RUT del usuario.
     * @param pass contraseña ingresada.
     * @return un {@link ResultadoLogin} con el resultado del intento de inicio de sesión.
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
     *
     * @param rut       RUT del estudiante.
     * @param nombre    nombre completo.
     * @param email     correo electrónico.
     * @param password  contraseña.
     * @param carrera   carrera universitaria.
     * @param semestres cantidad de semestres cursados.
     * @param promedio  promedio académico.
     * @throws SQLException si ocurre un error al insertar en la base de datos.
     */
    public void registrarEstudiante(String rut, String nombre, String email, String password, String carrera, int semestres, double promedio) throws SQLException {
        Estudiante nuevo = new Estudiante(rut, nombre, email, password, carrera, promedio, semestres);
        dataStore.registrarEstudiante(nuevo);
    }

    /**
     * Actualiza el nombre y correo electrónico de un usuario.
     *
     * @param rut         RUT del usuario.
     * @param nuevoNombre nuevo nombre.
     * @param nuevoEmail  nuevo correo electrónico.
     * @throws SQLException si ocurre un error en la actualización.
     */
    public void actualizarPerfilUsuario(String rut, String nuevoNombre, String nuevoEmail) throws SQLException {
        dataStore.actualizarNombreUsuario(rut, nuevoNombre);
        dataStore.actualizarEmailUsuario(rut, nuevoEmail);
    }

    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param rut           RUT del usuario.
     * @param nuevaPassword nueva contraseña.
     * @throws SQLException si ocurre un error en la actualización.
     */
    public void actualizarPasswordUsuario(String rut, String nuevaPassword) throws SQLException {
        dataStore.actualizarPasswordUsuario(rut, nuevaPassword);
    }

    /**
     * Persiste todos los cambios realizados en los usuarios hacia la base de datos.
     * <p>
     * Si ocurre un error, se muestra un mensaje de error en consola.
     * </p>
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
     * Verifica si un usuario existe en el sistema a partir de su RUT.
     *
     * @param rut RUT del usuario.
     * @return {@code true} si el usuario existe, {@code false} en caso contrario.
     */
    public boolean existeUsuario(String rut) {
        return dataStore.getUsuarioPorRut(rut) != null;
    }

    /**
     * Actualiza los datos académicos de un estudiante.
     *
     * @param rut       RUT del estudiante.
     * @param carrera   nueva carrera.
     * @param semestres cantidad de semestres cursados.
     * @param promedio  nuevo promedio académico.
     * @throws SQLException si ocurre un error en la actualización.
     */
    public void actualizarDatosAcademicos(String rut, String carrera, int semestres, double promedio) throws SQLException {
        dataStore.actualizarDatosAcademicosEstudiante(rut, carrera, semestres, promedio);
    }


}
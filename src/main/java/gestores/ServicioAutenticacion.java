package gestores;

import datastore.DataStore;
import enums.Rol;
import modelo.Estudiante;
import modelo.ResultadoLogin;
import modelo.Usuario;
import java.sql.SQLException;

public class ServicioAutenticacion {
    private final DataStore dataStore;

    public ServicioAutenticacion(DataStore dataStore) {
        this.dataStore = dataStore;
    }

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

    public void registrarEstudiante(String rut, String nombre, String email, String password, String carrera, int semestres, double promedio) throws SQLException {
        Estudiante nuevo = new Estudiante(rut, nombre, email, password, carrera, promedio, semestres);
        dataStore.registrarEstudiante(nuevo);
    }

    public void actualizarPerfilUsuario(String rut, String nuevoNombre, String nuevoEmail) throws SQLException {
        dataStore.actualizarNombreUsuario(rut, nuevoNombre);
        dataStore.actualizarEmailUsuario(rut, nuevoEmail);
    }

    public void actualizarPasswordUsuario(String rut, String nuevaPassword) throws SQLException {
        dataStore.actualizarPasswordUsuario(rut, nuevaPassword);
    }

    public void guardarCambiosDeUsuarios() {
        try {
            dataStore.persistirTodosLosUsuarios();
            System.out.println("✅ Datos de usuarios guardados en la BD.");
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar datos de usuarios: " + e.getMessage());
        }
    }

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

    public void bloquearUsuario(String rut) throws SQLException, IllegalArgumentException {
        Usuario usuario = dataStore.getUsuarioPorRut(rut);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró un usuario con el RUT especificado.");
        }
        usuario.setBloqueado(true);
        dataStore.actualizarUsuario(usuario); // Necesitarás un método genérico para guardar cambios en DataStore
    }

    public boolean existeUsuario(String rut) {
        return dataStore.getUsuarioPorRut(rut) != null;
    }

    public void actualizarDatosAcademicos(String rut, String carrera, int semestres, double promedio) throws SQLException {
        dataStore.actualizarDatosAcademicosEstudiante(rut, carrera, semestres, promedio);
    }

}
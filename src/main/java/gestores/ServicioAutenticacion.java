package gestores;

import datastore.DataStore;
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

    public boolean existeUsuario(String rut) {
        return dataStore.getUsuarioPorRut(rut) != null;
    }

    public void actualizarDatosAcademicos(String rut, String carrera, int semestres, double promedio) throws SQLException {
        dataStore.actualizarDatosAcademicosEstudiante(rut, carrera, semestres, promedio);
    }


}
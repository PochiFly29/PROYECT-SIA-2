package gestores;

import enums.EstadoPostulacion;
import modelo.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GestorIntercambio {

    private DataStore dataStore;

    public GestorIntercambio() {
        this.dataStore = new DataStore();
        try {
            dataStore.crearTablas();
            dataStore.cargarDatosDesdeBD();
            System.out.println("Datos cargados y enlazados en DataStore. Usuarios: " + dataStore.getUsuarios().size() + ", Convenios: " + dataStore.getConvenios().size() + ", Postulaciones: " + dataStore.getPostulaciones().size());
        } catch (SQLException e) {
            System.out.println("Error al inicializar el gestor: " + e.getMessage());
        }
    }

    public void guardarDatos() {
        try {
            dataStore.guardarDatos();
        } catch (SQLException e) {
            System.out.println("Error al guardar datos: " + e.getMessage());
        }
    }

    public ResultadoLogin iniciarSesion(String rut, String pass) {
        Usuario usuario = dataStore.getUsuarioPorRut(rut);
        if (usuario == null) {
            return new ResultadoLogin("El RUT no está registrado.");
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

    public void actualizarNombreUsuario(String rut, String nuevoNombre) {
        dataStore.actualizarNombreUsuario(rut, nuevoNombre);
    }

    public void actualizarEmailUsuario(String rut, String nuevoEmail) {
        dataStore.actualizarEmailUsuario(rut, nuevoEmail);
    }

    public void actualizarPasswordUsuario(String rut, String nuevaPass) {
        dataStore.actualizarPasswordUsuario(rut, nuevaPass);
    }

    public void actualizarCarreraEstudiante(String rut, String nuevaCarrera) {
        dataStore.actualizarCarreraEstudiante(rut, nuevaCarrera);
    }

    public void agregarInteraccionAPostulacion(String idPostulacion, Interaccion interaccion) {
        dataStore.agregarInteraccionAPostulacion(idPostulacion, interaccion);
    }

    public void actualizarEstadoPostulacion(String idPostulacion, EstadoPostulacion nuevoEstado) {
        dataStore.actualizarEstadoPostulacion(idPostulacion, nuevoEstado);
    }

    public Estudiante buscarEstudiantePorPostulacion(String idPostulacion) {
        Postulacion p = dataStore.getPostulacionPorId(idPostulacion);
        if (p == null) return null;
        Usuario u = dataStore.getUsuarioPorRut(p.getRutEstudiante());
        return u instanceof Estudiante ? (Estudiante) u : null;
    }

    public void descartarOtrasPostulaciones(String rutEstudiante, String idPostulacionAExcluir) {
        dataStore.actualizarEstadosPostulaciones(rutEstudiante, idPostulacionAExcluir, EstadoPostulacion.RECHAZADA);
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
                return postulacionesStream
                        .filter(p -> p.getRutEstudiante().equals(valorFiltro))
                        .sorted(Comparator.comparing(Postulacion::getFechaPostulacion).reversed())
                        .collect(Collectors.toList());
            case "estado":
                EstadoPostulacion estado = EstadoPostulacion.valueOf(valorFiltro.toUpperCase());
                if (estado == EstadoPostulacion.POR_REVISAR) {
                    return postulacionesStream
                            .filter(p -> p.getEstado() == estado)
                            .sorted(Comparator.comparing(Postulacion::getFechaPostulacion))
                            .collect(Collectors.toList());
                }
                return postulacionesStream
                        .filter(p -> p.getEstado() == estado)
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getId().substring(1))))
                        .collect(Collectors.toList());
            case "convenio":
                return postulacionesStream
                        .filter(p -> p.getIdConvenio().equals(valorFiltro))
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getId().substring(1))))
                        .collect(Collectors.toList());
            default:
                return dataStore.getPostulaciones();
        }
    }

    public boolean existeUsuario(String rut) {
        return dataStore.getUsuarioPorRut(rut) != null;
    }

    public void cerrarSesion() {

    }
}
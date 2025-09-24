package gestores;

import modelo.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Clase de almacenamiento de datos en memoria para acceso rápido.
 * Funciona como un caché central para las listas de la aplicación,
 * usando mapas para optimizar las búsquedas.
 */
public class DataStore {
    private final Map<String, Usuario> usuariosPorRut;
    private final Map<String, Convenio> conveniosPorId;
    private final Map<Integer, Programa> programasPorId;
    private final Map<String, Postulacion> postulacionesPorId;

    public DataStore(List<Usuario> usuarios, List<Convenio> convenios, List<Programa> programas, List<Postulacion> postulaciones) {
        this.usuariosPorRut = new HashMap<>();
        this.conveniosPorId = new HashMap<>();
        this.programasPorId = new HashMap<>();
        this.postulacionesPorId = new HashMap<>();

        // Llenar los mapas desde las listas
        usuarios.forEach(u -> this.usuariosPorRut.put(u.getRut(), u));
        convenios.forEach(c -> this.conveniosPorId.put(c.getId(), c));
        programas.forEach(p -> this.programasPorId.put(p.getId(), p));
        postulaciones.forEach(p -> this.postulacionesPorId.put(p.getId(), p));
    }

    // Métodos para obtener elementos
    public Usuario getUsuarioPorRut(String rut) { return usuariosPorRut.get(rut); }
    public Convenio getConvenioPorId(String id) { return conveniosPorId.get(id); }
    public Programa getProgramaPorId(int id) { return programasPorId.get(id); }
    public Postulacion getPostulacionPorId(String id) { return postulacionesPorId.get(id); }

    // Métodos para agregar elementos (actualizan los mapas y las listas subyacentes)
    public void addUsuario(Usuario u) { usuariosPorRut.put(u.getRut(), u); }
    public void addConvenio(Convenio c) { conveniosPorId.put(c.getId(), c); }
    public void addPrograma(Programa p) { programasPorId.put(p.getId(), p); }
    public void addPostulacion(Postulacion p) { postulacionesPorId.put(p.getId(), p); }

    // Métodos para obtener todas las listas
    public List<Usuario> getUsuarios() { return new ArrayList<>(usuariosPorRut.values()); }
    public List<Convenio> getConvenios() { return new ArrayList<>(conveniosPorId.values()); }
    public List<Programa> getProgramas() { return new ArrayList<>(programasPorId.values()); }
    public List<Postulacion> getPostulaciones() { return new ArrayList<>(postulacionesPorId.values()); }
}
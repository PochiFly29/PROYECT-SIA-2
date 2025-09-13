package modelo;

import enums.Rol;

public class Auditor extends Usuario {
    public Auditor(String rut, String nombreCompleto, String email, String pass) {
        super(rut, nombreCompleto, email, pass, Rol.AUDITOR);
    }
}
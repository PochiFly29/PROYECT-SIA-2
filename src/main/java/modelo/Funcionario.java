package modelo;

import enums.Rol;

public class Funcionario extends Usuario {
    public Funcionario(String rut, String nombreCompleto, String email, String pass) {
        super(rut, nombreCompleto, email, pass, Rol.FUNCIONARIO);
    }
}
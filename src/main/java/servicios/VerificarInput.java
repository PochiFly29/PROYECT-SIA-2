package servicios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VerificarInput {

    private final BufferedReader leer;

    public VerificarInput() {
        this.leer = new BufferedReader(new InputStreamReader(System.in));
    }

    public String leerLinea() {
        try {
            return leer.readLine().trim();
        } catch (IOException e) {
            return "";
        }
    }

    public String leerLinea(String prompt) {
        System.out.print(prompt);
        return leerLinea();
    }

    public int leerEntero(String prompt, int defaultVal) {
        System.out.print(prompt);
        while (true) {
            String s = leerLinea();
            if (s.isEmpty()) return defaultVal;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                System.out.print("Entrada inválida. Ingrese un número válido: ");
            }
        }
    }

    public double leerDouble(String prompt, double defaultVal) {
        System.out.print(prompt);
        while (true) {
            String s = leerLinea().replace(',', '.');
            if (s.isEmpty()) return defaultVal;
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException nfe) {
                System.out.print("Entrada inválida. Ingrese un número válido: ");
            }
        }
    }

    public void pausaEnter(String prompt) {
        System.out.print(prompt);
        leerLinea();
    }

    public static String normalizarRut(String s) {
        if (s == null) return "";
        return s.toUpperCase().replaceAll("[^0-9K]", "");
    }

    public static boolean rutValido(String rut) {
        if (rut == null) return false;
        rut = rut.trim().toUpperCase();
        return rut.matches("^[0-9]{8}[0-9K]$");
    }
}
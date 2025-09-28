package datastore;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * **Herramienta de Migración de Esquema de la Base de Datos (BD).**
 * <p>Esta clase se utiliza para aplicar cambios incrementales al esquema de la BD
 * (ej. añadir nuevas columnas o tablas) sin eliminar los datos existentes.</p>
 * <p><strong>NOTA DE USO:</strong> Está diseñada para ser ejecutada una sola vez
 * por cada cambio de esquema. Debe actualizarse para futuras migraciones.</p>
 */
public class MigracionBD {

    /**
     * Punto de entrada principal para ejecutar las operaciones de migración.
     * @param args Argumentos de la línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        System.out.println("Iniciando migración de la base de datos...");
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {

            // PASO 1: Añadir la nueva columna 'estado' a la tabla 'programas'.
            // Se le asigna 'FINALIZADO' por defecto a todas las filas existentes.
            stmt.execute("ALTER TABLE programas ADD COLUMN estado TEXT NOT NULL DEFAULT 'FINALIZADO'");
            System.out.println(" > Columna 'estado' añadida a la tabla 'programas'.");

            // PASO 2: Actualizar el programa existente (ID=1) para que sea el ACTIVO.
            stmt.execute("UPDATE programas SET estado = 'ACTIVO' WHERE id_programa = 1");
            System.out.println(" > Programa con ID=1 marcado como 'ACTIVO'.");

            System.out.println("✅ ¡Migración completada exitosamente!");

        } catch (SQLException e) {
            // Es normal que esto falle si ya ejecutaste la migración.
            if (e.getMessage().contains("duplicate column name: estado")) {
                System.out.println("AVISO: La columna 'estado' ya existe. No se realizaron cambios.");
            } else {
                System.err.println("❌ Error durante la migración: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
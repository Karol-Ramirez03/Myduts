package co.uts.duts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre conexiones a la base de datos SQLite local.
 *
 * El archivo de la base de datos queda siempre en la misma ruta, sin importar desde dónde se ejecute
 * el programa (NetBeans, Visual Studio o el .jar): carpeta .myduts dentro del directorio del usuario.
 * Para usar otra ruta (por ejemplo en pruebas) se puede ejecutar con -Dmyduts.db=/ruta/archivo.db
 */
public final class ConexionSQLite {
    private static final String URL = construirUrl();

    private ConexionSQLite() { }

    private static String construirUrl() {
        String ruta = System.getProperty("myduts.db");
        if (ruta == null || ruta.isBlank()) {
            Path carpeta = Paths.get(System.getProperty("user.home"), ".myduts");
            try {
                Files.createDirectories(carpeta);
            } catch (IOException ex) {
                throw new IllegalStateException("No se pudo crear la carpeta de datos: " + carpeta, ex);
            }
            ruta = carpeta.resolve("myduts.db").toString();
        }
        return "jdbc:sqlite:" + ruta;
    }

    public static String getUrl() { return URL; }

    public static Connection abrir() throws SQLException {
        Connection conexion = DriverManager.getConnection(URL);
        try (var statement = conexion.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return conexion;
    }
}

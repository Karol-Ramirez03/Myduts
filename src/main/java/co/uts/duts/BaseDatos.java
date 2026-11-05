package co.uts.duts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Crea las tablas de MyDUTS si no existen y actualiza bases de datos antiguas agregando las
 * columnas nuevas (migración), para que nadie tenga que borrar su archivo .db al actualizar el código.
 */
public final class BaseDatos {
    static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private BaseDatos() { }

    public static void inicializar() {
        String[] tablas = {
            """
            CREATE TABLE IF NOT EXISTS estudiante (
                id_estudiante INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo_estudiantil TEXT NOT NULL UNIQUE,
                nombre_completo TEXT NOT NULL,
                correo TEXT NOT NULL UNIQUE,
                contrasena_hash TEXT NOT NULL,
                saldo_duts INTEGER NOT NULL DEFAULT 0 CHECK (saldo_duts >= 0),
                saldo_minimo_alerta INTEGER NOT NULL DEFAULT 5000,
                fecha_registro TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
                rol TEXT NOT NULL DEFAULT 'ESTUDIANTE',
                pin_hash TEXT,
                ruta_foto TEXT,
                duts_acumulados INTEGER NOT NULL DEFAULT 0 CHECK (duts_acumulados >= 0)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS periodo_academico (
                id_periodo INTEGER PRIMARY KEY AUTOINCREMENT,
                anio INTEGER NOT NULL,
                semestre INTEGER NOT NULL CHECK (semestre IN (1, 2)),
                fecha_inicio TEXT NOT NULL,
                fecha_fin TEXT NOT NULL,
                UNIQUE (anio, semestre)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS nota_academica (
                id_nota INTEGER PRIMARY KEY AUTOINCREMENT,
                id_estudiante INTEGER NOT NULL,
                id_periodo INTEGER NOT NULL,
                asignatura TEXT NOT NULL,
                actividad TEXT NOT NULL,
                nota REAL NOT NULL CHECK (nota >= 0 AND nota <= 5),
                porcentaje REAL NOT NULL CHECK (porcentaje >= 0 AND porcentaje <= 100),
                fecha_registro TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante) ON DELETE CASCADE,
                FOREIGN KEY (id_periodo) REFERENCES periodo_academico(id_periodo) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS novedad (
                id_novedad INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                fecha_inicio TEXT NOT NULL,
                fecha_fin TEXT NOT NULL,
                duts_recompensa INTEGER NOT NULL DEFAULT 0 CHECK (duts_recompensa >= 0),
                cupo_maximo INTEGER,
                estado TEXT NOT NULL DEFAULT 'PUBLICADA' CHECK (estado IN ('BORRADOR','PUBLICADA','FINALIZADA','CANCELADA'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS inscripcion_novedad (
                id_inscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_estudiante INTEGER NOT NULL,
                id_novedad INTEGER NOT NULL,
                fecha_inscripcion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                estado TEXT NOT NULL DEFAULT 'INSCRITO' CHECK (estado IN ('INSCRITO','ASISTIO','NO_ASISTIO','CANCELADO')),
                UNIQUE (id_estudiante, id_novedad),
                FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante) ON DELETE CASCADE,
                FOREIGN KEY (id_novedad) REFERENCES novedad(id_novedad) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS movimiento_duts (
                id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,
                id_estudiante_origen INTEGER,
                id_estudiante_destino INTEGER,
                tipo TEXT NOT NULL CHECK (tipo IN ('TRANSFERENCIA','RECOMPENSA_EVENTO','AJUSTE')),
                cantidad_duts INTEGER NOT NULL CHECK (cantidad_duts > 0),
                descripcion TEXT,
                estado TEXT NOT NULL DEFAULT 'CONFIRMADO' CHECK (estado IN ('PENDIENTE','CONFIRMADO','RECHAZADO')),
                fecha_creacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                id_novedad INTEGER,
                FOREIGN KEY (id_estudiante_origen) REFERENCES estudiante(id_estudiante) ON DELETE SET NULL,
                FOREIGN KEY (id_estudiante_destino) REFERENCES estudiante(id_estudiante) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS logro (
                id_logro INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                duts_requeridos INTEGER NOT NULL DEFAULT 0
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS estudiante_logro (
                id_estudiante INTEGER NOT NULL,
                id_logro INTEGER NOT NULL,
                fecha_obtencion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id_estudiante, id_logro),
                FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante) ON DELETE CASCADE,
                FOREIGN KEY (id_logro) REFERENCES logro(id_logro) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS pregunta_frecuente (
                id_pregunta INTEGER PRIMARY KEY AUTOINCREMENT,
                pregunta TEXT NOT NULL,
                respuesta TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS calificacion_novedad (
                id_calificacion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_estudiante INTEGER NOT NULL,
                id_novedad INTEGER NOT NULL,
                estrellas INTEGER NOT NULL CHECK (estrellas BETWEEN 1 AND 5),
                comentario TEXT,
                fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                UNIQUE (id_estudiante, id_novedad),
                FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante) ON DELETE CASCADE,
                FOREIGN KEY (id_novedad) REFERENCES novedad(id_novedad) ON DELETE CASCADE
            )
            """
        };

        try (Connection conexion = ConexionSQLite.abrir(); Statement statement = conexion.createStatement()) {
            for (String tabla : tablas) statement.executeUpdate(tabla);

            // Migración: bases creadas con la versión anterior no tienen estas columnas.
            agregarColumna(conexion, "estudiante", "rol", "TEXT NOT NULL DEFAULT 'ESTUDIANTE'");
            agregarColumna(conexion, "estudiante", "pin_hash", "TEXT");
            agregarColumna(conexion, "estudiante", "ruta_foto", "TEXT");
            agregarColumna(conexion, "estudiante", "duts_acumulados", "INTEGER NOT NULL DEFAULT 0");
            agregarColumna(conexion, "logro", "duts_requeridos", "INTEGER NOT NULL DEFAULT 0");
            agregarColumna(conexion, "movimiento_duts", "id_novedad", "INTEGER");

            sembrarAdministrador(conexion);
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo inicializar la base de datos SQLite.", ex);
        }
    }

    private static void agregarColumna(Connection conexion, String tabla, String columna, String definicion)
            throws SQLException {
        boolean existe = false;
        try (Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabla + ")")) {
            while (rs.next()) {
                if (columna.equalsIgnoreCase(rs.getString("name"))) existe = true;
            }
        }
        if (!existe) {
            try (Statement st = conexion.createStatement()) {
                st.executeUpdate("ALTER TABLE " + tabla + " ADD COLUMN " + columna + " " + definicion);
            }
        }
    }

    /**
     * Crea un administrador inicial para poder probar el sistema (solo si no existe).
     * Usuario: ADMIN  |  Contraseña: admin123  -> cámbiala antes de la sustentación.
     */
    private static void sembrarAdministrador(Connection conexion) throws SQLException {
        String sql = "INSERT OR IGNORE INTO estudiante (codigo_estudiantil, nombre_completo, correo, contrasena_hash, rol, fecha_registro) "
                + "VALUES ('ADMIN', 'Administrador UTS', 'admin@uts.edu.co', ?, 'ADMIN', ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, Seguridad.hash("admin123"));
            ps.setString(2, LocalDateTime.now().format(FORMATO_FECHA));
            ps.executeUpdate();
        }
    }
}

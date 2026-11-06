package co.uts.duts.datos;


import co.uts.duts.duts.Cuenta;
import co.uts.duts.usuarios.Administrador;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Gestionable;
import co.uts.duts.usuarios.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla estudiante (que también guarda a los administradores, diferenciados por la columna rol).
 * Implementa el CRUD de estudiantes. "Eliminar" es una eliminación lógica (activo = 0).
 */
public class EstudianteDAO implements Gestionable<Estudiante> {

    // ---------------- registro y autenticación ----------------

    public void registrar(String nombre, String codigo, String correo, String contrasena) throws SQLException {
        crear(new Estudiante(nombre, codigo, correo, contrasena));
    }

    /** Devuelve el Estudiante o Administrador que corresponda, o null si las credenciales no son correctas. */
    public Usuario autenticar(String usuario, String contrasena) throws SQLException {
        String sql = "SELECT * FROM estudiante WHERE activo = 1 AND (correo = ? OR codigo_estudiantil = ?) AND contrasena_hash = ?";
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, usuario);
            ps.setString(3, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean existeCorreoOCodigo(String correo, String codigo) throws SQLException {
        String sql = "SELECT 1 FROM estudiante WHERE correo = ? OR codigo_estudiantil = ? LIMIT 1";
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Busca un estudiante activo por su código estudiantil o su correo (para elegir el destino de un envío). */
    public Estudiante buscarPorCodigoOCorreo(String texto) throws SQLException {
        String sql = "SELECT * FROM estudiante WHERE activo = 1 AND rol = 'ESTUDIANTE' AND (codigo_estudiantil = ? OR correo = ?) LIMIT 1";
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, texto);
            ps.setString(2, texto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? comoEstudiante(mapear(rs)) : null;
            }
        }
    }

    // ---------------- CRUD (Gestionable) ----------------

    @Override
    public boolean crear(Estudiante e) throws SQLException {
        String sql = "INSERT INTO estudiante (codigo_estudiantil, nombre_completo, correo, contrasena_hash, "
                + "saldo_duts, duts_acumulados, saldo_minimo_alerta, pin_hash, ruta_foto, fecha_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionSQLite.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Cuenta c = e.getCuenta();
            ps.setString(1, e.getCodigo());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getCorreo());
            ps.setString(4, e.getContrasena());
            ps.setInt(5, c.getSaldoDuts());
            ps.setInt(6, c.getDutsAcumulados());
            ps.setInt(7, c.getSaldoMinimoAlerta());
            ps.setString(8, c.getPin());
            ps.setString(9, e.getRutaFoto());
            ps.setString(10, e.getFechaRegistro().format(BaseDatos.FORMATO_FECHA));
            boolean ok = ps.executeUpdate() > 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getLong(1));
            }
            return ok;
        }
    }

    @Override
    public Estudiante consultar(long id) throws SQLException {
        try (Connection conexion = ConexionSQLite.abrir()) {
            return consultar(conexion, id);
        }
    }

    /** Versión que usa una conexión ya abierta (para participar en una transacción). */
    public Estudiante consultar(Connection conexion, long id) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM estudiante WHERE id_estudiante = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? comoEstudiante(mapear(rs)) : null;
            }
        }
    }

    /** Actualiza los datos de perfil (no toca saldos; para eso está {@link #actualizarCuenta}). */
    @Override
    public boolean actualizar(Estudiante e) throws SQLException {
        String sql = "UPDATE estudiante SET nombre_completo = ?, correo = ?, ruta_foto = ?, activo = ? "
                + "WHERE id_estudiante = ? AND rol = 'ESTUDIANTE'";
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getCorreo());
            ps.setString(3, e.getRutaFoto());
            ps.setInt(4, e.isActivo() ? 1 : 0);
            ps.setLong(5, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Eliminación lógica: el estudiante queda inactivo y ya no puede iniciar sesión. */
    @Override
    public boolean eliminar(long id) throws SQLException {
        String sql = "UPDATE estudiante SET activo = 0 WHERE id_estudiante = ? AND rol = 'ESTUDIANTE'";
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Estudiante> listar() throws SQLException {
        return consultarLista("SELECT * FROM estudiante WHERE rol = 'ESTUDIANTE' AND activo = 1 ORDER BY nombre_completo", -1);
    }

    // ---------------- cuenta de DUTS y ranking ----------------

    /** Guarda saldo, acumulados, saldo mínimo de alerta y PIN de la cuenta, dentro de una transacción. */
    public void actualizarCuenta(Connection conexion, Estudiante e) throws SQLException {
        String sql = "UPDATE estudiante SET saldo_duts = ?, duts_acumulados = ?, saldo_minimo_alerta = ?, pin_hash = ? "
                + "WHERE id_estudiante = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            Cuenta c = e.getCuenta();
            ps.setInt(1, c.getSaldoDuts());
            ps.setInt(2, c.getDutsAcumulados());
            ps.setInt(3, c.getSaldoMinimoAlerta());
            ps.setString(4, c.getPin());
            ps.setLong(5, e.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarCuenta(Estudiante e) throws SQLException {
        try (Connection conexion = ConexionSQLite.abrir()) {
            actualizarCuenta(conexion, e);
        }
    }

    /** Los {@code n} estudiantes con más DUTS acumulados (no saldo actual). */
    public List<Estudiante> rankingTop(int n) throws SQLException {
        return consultarLista("SELECT * FROM estudiante WHERE rol = 'ESTUDIANTE' AND activo = 1 "
                + "ORDER BY duts_acumulados DESC, nombre_completo ASC LIMIT ?", n);
    }

    // ---------------- utilidades privadas ----------------

    private List<Estudiante> consultarLista(String sql, int parametro) throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (parametro >= 0) ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Estudiante e = comoEstudiante(mapear(rs));
                    if (e != null) lista.add(e);
                }
            }
        }
        return lista;
    }

    private Estudiante comoEstudiante(Usuario u) {
        return u instanceof Estudiante ? (Estudiante) u : null;
    }

    /** Convierte una fila en Estudiante o Administrador según la columna rol. */
    private Usuario mapear(ResultSet rs) throws SQLException {
        long id = rs.getLong("id_estudiante");
        String nombre = rs.getString("nombre_completo");
        String codigo = rs.getString("codigo_estudiantil");
        String correo = rs.getString("correo");
        String contrasena = rs.getString("contrasena_hash");
        boolean activo = rs.getInt("activo") == 1;
        LocalDateTime fecha = parseFecha(rs.getString("fecha_registro"));

        if ("ADMIN".equals(rs.getString("rol"))) {
            return new Administrador(id, nombre, codigo, correo, contrasena, activo, null);
        }
        Cuenta cuenta = new Cuenta(rs.getInt("saldo_duts"), rs.getInt("duts_acumulados"),
                rs.getInt("saldo_minimo_alerta"), rs.getString("pin_hash"));
        return new Estudiante(id, nombre, codigo, correo, contrasena, activo, fecha, rs.getString("ruta_foto"), cuenta);
    }

    static LocalDateTime parseFecha(String texto) {
        if (texto == null || texto.isBlank()) return LocalDateTime.now();
        return LocalDateTime.parse(texto, BaseDatos.FORMATO_FECHA);
    }
}

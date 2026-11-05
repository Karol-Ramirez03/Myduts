package co.uts.duts.datos;


import co.uts.duts.duts.AjusteAdmin;
import co.uts.duts.duts.Movimiento;
import co.uts.duts.duts.RecompensaEvento;
import co.uts.duts.duts.Transferencia;
import co.uts.duts.usuarios.Gestionable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla movimiento_duts: CRUD de movimientos de DUTS.
 *
 * Regla de integridad: un movimiento CONFIRMADO ya modificó saldos, por eso no se puede eliminar
 * (los errores se corrigen con un ajuste del administrador). Solo se eliminan los PENDIENTES o RECHAZADOS.
 */
public class MovimientoDAO implements Gestionable<Movimiento> {

    @Override
    public boolean crear(Movimiento m) throws SQLException {
        try (Connection conexion = ConexionSQLite.abrir()) {
            return crear(conexion, m);
        }
    }

    /** Versión que usa una conexión ya abierta (para participar en una transacción). */
    public boolean crear(Connection conexion, Movimiento m) throws SQLException {
        String sql = "INSERT INTO movimiento_duts (id_estudiante_origen, id_estudiante_destino, tipo, cantidad_duts, "
                + "descripcion, estado, fecha_creacion, id_novedad) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarLong(ps, 1, m.getIdOrigen());
            asignarLong(ps, 2, m.getIdDestino());
            ps.setString(3, m.getTipo());
            ps.setInt(4, m.getMonto());
            ps.setString(5, m.getDescripcion());
            ps.setString(6, m.getEstado());
            ps.setString(7, m.getFecha().format(BaseDatos.FORMATO_FECHA));
            asignarLong(ps, 8, m instanceof RecompensaEvento r ? Long.valueOf(r.getIdNovedad()) : null);
            boolean ok = ps.executeUpdate() > 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getLong(1));
            }
            return ok;
        }
    }

    @Override
    public Movimiento consultar(long id) throws SQLException {
        try (Connection conexion = ConexionSQLite.abrir();
             PreparedStatement ps = conexion.prepareStatement("SELECT * FROM movimiento_duts WHERE id_movimiento = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Solo permite corregir la descripción y el estado; los montos y las cuentas de un movimiento no cambian. */
    @Override
    public boolean actualizar(Movimiento m) throws SQLException {
        String sql = "UPDATE movimiento_duts SET descripcion = ?, estado = ? WHERE id_movimiento = ?";
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, m.getDescripcion());
            ps.setString(2, m.getEstado());
            ps.setLong(3, m.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(long id) throws SQLException {
        String sql = "DELETE FROM movimiento_duts WHERE id_movimiento = ? AND estado <> 'CONFIRMADO'";
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Movimiento> listar() throws SQLException {
        return filtrar(null, null, null, null);
    }

    public List<Movimiento> listarPorEstudiante(long idEstudiante) throws SQLException {
        return filtrar(idEstudiante, null, null, null);
    }

    /**
     * Historial con filtros opcionales (cualquier parámetro puede ser null).
     *
     * @param categoria null = todos; "INGRESOS" = DUTS recibidos; "EGRESOS" = transferencias enviadas;
     *                  "EVENTOS" = recompensas de eventos
     */
    public List<Movimiento> filtrar(Long idEstudiante, String categoria, LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM movimiento_duts WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (idEstudiante != null) {
            if ("INGRESOS".equals(categoria)) {
                sql.append(" AND id_estudiante_destino = ?");
                params.add(idEstudiante);
            } else if ("EGRESOS".equals(categoria)) {
                sql.append(" AND id_estudiante_origen = ? AND tipo = 'TRANSFERENCIA'");
                params.add(idEstudiante);
            } else if ("EVENTOS".equals(categoria)) {
                sql.append(" AND id_estudiante_destino = ? AND tipo = 'RECOMPENSA_EVENTO'");
                params.add(idEstudiante);
            } else {
                sql.append(" AND (id_estudiante_origen = ? OR id_estudiante_destino = ?)");
                params.add(idEstudiante);
                params.add(idEstudiante);
            }
        }
        if (desde != null) {
            sql.append(" AND fecha_creacion >= ?");
            params.add(desde.format(BaseDatos.FORMATO_FECHA));
        }
        if (hasta != null) {
            sql.append(" AND fecha_creacion <= ?");
            params.add(hasta.format(BaseDatos.FORMATO_FECHA));
        }
        sql.append(" ORDER BY fecha_creacion DESC, id_movimiento DESC");

        List<Movimiento> lista = new ArrayList<>();
        try (Connection conexion = ConexionSQLite.abrir(); PreparedStatement ps = conexion.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ---------------- utilidades privadas ----------------

    /** Crea el tipo concreto de movimiento según la columna tipo (polimorfismo). */
    private Movimiento mapear(ResultSet rs) throws SQLException {
        long id = rs.getLong("id_movimiento");
        Long origen = leerLong(rs, "id_estudiante_origen");
        Long destino = leerLong(rs, "id_estudiante_destino");
        int monto = rs.getInt("cantidad_duts");
        String descripcion = rs.getString("descripcion");
        String estado = rs.getString("estado");
        LocalDateTime fecha = EstudianteDAO.parseFecha(rs.getString("fecha_creacion"));
        String tipo = rs.getString("tipo");

        switch (tipo) {
            case "TRANSFERENCIA":
                return new Transferencia(id, origen == null ? 0 : origen, destino == null ? 0 : destino,
                        monto, descripcion, fecha, estado);
            case "RECOMPENSA_EVENTO":
                Long novedad = leerLong(rs, "id_novedad");
                return new RecompensaEvento(id, destino == null ? 0 : destino, novedad == null ? 0 : novedad,
                        monto, descripcion, fecha, estado);
            case "AJUSTE":
                return new AjusteAdmin(id, origen == null ? 0 : origen, destino == null ? 0 : destino,
                        monto, descripcion, fecha, estado);
            default:
                throw new SQLException("Tipo de movimiento desconocido: " + tipo);
        }
    }

    private Long leerLong(ResultSet rs, String columna) throws SQLException {
        long valor = rs.getLong(columna);
        return rs.wasNull() ? null : valor;
    }

    private void asignarLong(PreparedStatement ps, int indice, Long valor) throws SQLException {
        if (valor == null) ps.setNull(indice, Types.INTEGER);
        else ps.setLong(indice, valor);
    }
}

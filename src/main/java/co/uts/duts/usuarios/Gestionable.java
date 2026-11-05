package co.uts.duts.usuarios;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrato CRUD (crear, consultar, actualizar, eliminar) que implementan todos los DAO.
 *
 * @param <T> tipo de objeto que se administra
 */
public interface Gestionable<T> {
    boolean crear(T obj) throws SQLException;

    T consultar(long id) throws SQLException;

    boolean actualizar(T obj) throws SQLException;

    boolean eliminar(long id) throws SQLException;

    List<T> listar() throws SQLException;
}

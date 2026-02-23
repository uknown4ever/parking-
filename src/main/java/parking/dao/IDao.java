package parking.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface générique CRUD.
 */
public interface IDao<T> {
    void   create(T entity) throws SQLException;
    void   update(T entity) throws SQLException;
    void   delete(int id)   throws SQLException;
    T      findById(int id) throws SQLException;
    List<T> findAll()       throws SQLException;
}

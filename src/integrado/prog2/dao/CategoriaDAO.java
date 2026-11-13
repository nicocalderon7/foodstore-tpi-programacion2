package integrado.prog2.dao;

import integrado.prog2.config.ConexionDB;
import integrado.prog2.entities.Categoria;
import integrado.prog2.exception.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla categoria mediante JDBC.
 * Todas las sentencias usan PreparedStatement (consultas parametrizadas).
 */
public class CategoriaDAO implements GenericDAO<Categoria> {

    private static final String COLUMNAS = "id, nombre, descripcion, eliminado, created_at";

    private static final String SQL_INSERT =
            "INSERT INTO categoria (nombre, descripcion, eliminado, created_at) VALUES (?, ?, FALSE, ?)";
    private static final String SQL_FIND_ALL =
            "SELECT " + COLUMNAS + " FROM categoria WHERE eliminado = FALSE ORDER BY id";
    private static final String SQL_FIND_BY_ID =
            "SELECT " + COLUMNAS + " FROM categoria WHERE id = ? AND eliminado = FALSE";
    private static final String SQL_UPDATE =
            "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id = ? AND eliminado = FALSE";
    private static final String SQL_SOFT_DELETE =
            "UPDATE categoria SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";
    private static final String SQL_EXISTE_NOMBRE =
            "SELECT COUNT(*) FROM categoria WHERE eliminado = FALSE AND LOWER(nombre) = LOWER(?) AND id <> ?";
    private static final String SQL_CONTAR_PRODUCTOS =
            "SELECT COUNT(*) FROM producto WHERE categoria_id = ? AND eliminado = FALSE";

    /** INSERT parametrizado. Asigna al objeto el id generado por la base. */
    public Categoria insert(Categoria categoria) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setTimestamp(3, Timestamp.valueOf(categoria.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    categoria.setId(claves.getLong(1));
                }
            }
            return categoria;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("crear la categoria", e);
        }
    }

    @Override
    public List<Categoria> findAll() throws PersistenciaException {
        List<Categoria> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("listar las categorias", e);
        }
    }

    @Override
    public Optional<Categoria> findById(Long id) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ErroresSQL.traducir("buscar la categoria", e);
        }
    }

    @Override
    public boolean update(Categoria categoria) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setLong(3, categoria.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("actualizar la categoria", e);
        }
    }

    @Override
    public boolean softDelete(Long id) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_SOFT_DELETE)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("eliminar la categoria", e);
        }
    }

    /**
     * Indica si ya hay otra categoria ACTIVA con ese nombre (sin distinguir mayusculas).
     *
     * @param excluirId id a ignorar (la propia categoria al editar); null al crear
     */
    public boolean existeNombreActivo(String nombre, Long excluirId) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_EXISTE_NOMBRE)) {
            ps.setString(1, nombre);
            ps.setLong(2, excluirId == null ? -1L : excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErroresSQL.traducir("validar el nombre de la categoria", e);
        }
    }

    /** Cantidad de productos activos asociados a la categoria (impide la baja). */
    public int contarProductosActivos(Long idCategoria) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_CONTAR_PRODUCTOS)) {
            ps.setLong(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw ErroresSQL.traducir("contar los productos de la categoria", e);
        }
    }

    /** Convierte la fila actual del ResultSet en un objeto Categoria. */
    private Categoria mapear(ResultSet rs) throws SQLException {
        Timestamp creado = rs.getTimestamp("created_at");
        return new Categoria(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("eliminado"),
                creado == null ? null : creado.toLocalDateTime());
    }
}

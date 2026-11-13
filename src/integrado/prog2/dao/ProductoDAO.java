package integrado.prog2.dao;

import integrado.prog2.config.ConexionDB;
import integrado.prog2.entities.Producto;
import integrado.prog2.exception.PersistenciaException;
import integrado.prog2.exception.StockInvalidoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Acceso a la tabla producto mediante JDBC.
 *
 * Es la unica clase que conoce la columna categoria_id: el id de la categoria
 * llega como parametro (insert, reasignarCategoria, findByCategoria) y nunca
 * se guarda dentro del objeto Producto, respetando la direccionalidad del UML.
 */
public class ProductoDAO implements GenericDAO<Producto> {

    private static final String COLUMNAS =
            "id, nombre, precio, descripcion, stock, imagen, disponible, eliminado, created_at";

    private static final String SQL_INSERT =
            "INSERT INTO producto (nombre, precio, descripcion, stock, imagen, disponible, eliminado, created_at, categoria_id) "
          + "VALUES (?, ?, ?, ?, ?, ?, FALSE, ?, ?)";
    private static final String SQL_FIND_ALL =
            "SELECT " + COLUMNAS + " FROM producto WHERE eliminado = FALSE ORDER BY id";
    private static final String SQL_FIND_BY_ID =
            "SELECT " + COLUMNAS + " FROM producto WHERE id = ? AND eliminado = FALSE";
    private static final String SQL_FIND_BY_CATEGORIA =
            "SELECT " + COLUMNAS + " FROM producto WHERE categoria_id = ? AND eliminado = FALSE ORDER BY nombre";
    // El UPDATE de los atributos propios NO toca categoria_id.
    private static final String SQL_UPDATE =
            "UPDATE producto SET nombre = ?, precio = ?, descripcion = ?, stock = ?, imagen = ?, disponible = ? "
          + "WHERE id = ? AND eliminado = FALSE";
    private static final String SQL_REASIGNAR =
            "UPDATE producto SET categoria_id = ? WHERE id = ? AND eliminado = FALSE";
    private static final String SQL_SOFT_DELETE =
            "UPDATE producto SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";
    // El descuento solo se aplica si alcanza el stock: la condicion stock >= ? evita stocks negativos
    // incluso si otro proceso modifico el producto entre la lectura y la confirmacion.
    private static final String SQL_DESCONTAR =
            "UPDATE producto SET stock = stock - ? WHERE id = ? AND eliminado = FALSE AND disponible = TRUE AND stock >= ?";
    private static final String SQL_REPONER =
            "UPDATE producto SET stock = stock + ? WHERE id = ?";
    private static final String SQL_STOCK_ACTUAL =
            "SELECT nombre, stock, disponible, eliminado FROM producto WHERE id = ?";

    /**
     * INSERT parametrizado. El id de la categoria viaja como parametro y se guarda
     * solo en la columna categoria_id (el objeto Producto no lo conserva).
     */
    public Producto insert(Producto producto, Long idCategoria) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setString(3, producto.getDescripcion());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImagen());
            ps.setBoolean(6, Boolean.TRUE.equals(producto.getDisponible()));
            ps.setTimestamp(7, Timestamp.valueOf(producto.getCreatedAt()));
            ps.setLong(8, idCategoria);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setId(claves.getLong(1));
                }
            }
            return producto;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("crear el producto", e);
        }
    }

    @Override
    public List<Producto> findAll() throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            return mapearLista(rs);
        } catch (SQLException e) {
            throw ErroresSQL.traducir("listar los productos", e);
        }
    }

    @Override
    public Optional<Producto> findById(Long id) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ErroresSQL.traducir("buscar el producto", e);
        }
    }

    /** Productos activos de una categoria: navegacion Categoria -> Productos. */
    public List<Producto> findByCategoria(Long idCategoria) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_FIND_BY_CATEGORIA)) {
            ps.setLong(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        } catch (SQLException e) {
            throw ErroresSQL.traducir("listar los productos de la categoria", e);
        }
    }

    @Override
    public boolean update(Producto producto) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setString(3, producto.getDescripcion());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImagen());
            ps.setBoolean(6, Boolean.TRUE.equals(producto.getDisponible()));
            ps.setLong(7, producto.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("actualizar el producto", e);
        }
    }

    /** Mueve el producto a otra categoria: actualiza unicamente la clave foranea. */
    public boolean reasignarCategoria(Long idProducto, Long idCategoria) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_REASIGNAR)) {
            ps.setLong(1, idCategoria);
            ps.setLong(2, idProducto);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("cambiar la categoria del producto", e);
        }
    }

    @Override
    public boolean softDelete(Long id) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_SOFT_DELETE)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("eliminar el producto", e);
        }
    }

    /**
     * Descuenta el stock de varios productos en UNA sola transaccion.
     * Si algun producto no tiene stock suficiente (o fue dado de baja / no esta
     * disponible), se hace ROLLBACK de todo y se lanza StockInvalidoException.
     *
     * Los productos se actualizan ordenados por id para que dos pedidos simultaneos
     * bloqueen las filas en el mismo orden (reduce la posibilidad de deadlock).
     *
     * @param cantidades id de producto -> cantidad a descontar
     */
    public void descontarStock(Map<Long, Integer> cantidades) throws PersistenciaException, StockInvalidoException {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_DESCONTAR)) {
                for (Map.Entry<Long, Integer> e : new TreeMap<>(cantidades).entrySet()) {
                    ps.setInt(1, e.getValue());
                    ps.setLong(2, e.getKey());
                    ps.setInt(3, e.getValue());
                    if (ps.executeUpdate() != 1) {
                        String motivo = motivoSinStock(con, e.getKey(), e.getValue());
                        con.rollback();
                        throw new StockInvalidoException(motivo + " No se registro el pedido ni se modifico ningun stock.");
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw ErroresSQL.traducir("descontar el stock", e);
        }
    }

    /**
     * Devuelve al stock las cantidades indicadas, en una sola transaccion
     * (se usa al cancelar o eliminar un pedido vigente).
     */
    public void reponerStock(Map<Long, Integer> cantidades) throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_REPONER)) {
                for (Map.Entry<Long, Integer> e : new TreeMap<>(cantidades).entrySet()) {
                    ps.setInt(1, e.getValue());
                    ps.setLong(2, e.getKey());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw ErroresSQL.traducir("reponer el stock", e);
        }
    }

    /** Arma un mensaje que explica por que no se pudo descontar el stock de un producto. */
    private String motivoSinStock(Connection con, Long id, int pedido) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_STOCK_ACTUAL)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getBoolean("eliminado")) {
                    return "El producto id " + id + " ya no existe en el catalogo.";
                }
                String nombre = rs.getString("nombre");
                if (!rs.getBoolean("disponible")) {
                    return "El producto '" + nombre + "' no esta disponible.";
                }
                return "Stock insuficiente para '" + nombre + "': se pidieron " + pedido
                     + " y hay " + rs.getInt("stock") + ".";
            }
        }
    }

    private List<Producto> mapearLista(ResultSet rs) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        return lista;
    }

    /** Convierte la fila actual del ResultSet en un objeto Producto (sin categoria). */
    private Producto mapear(ResultSet rs) throws SQLException {
        Timestamp creado = rs.getTimestamp("created_at");
        return new Producto(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getString("descripcion"),
                rs.getInt("stock"),
                rs.getString("imagen"),
                rs.getBoolean("disponible"),
                rs.getBoolean("eliminado"),
                creado == null ? null : creado.toLocalDateTime());
    }
}

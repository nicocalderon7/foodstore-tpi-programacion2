package integrado.prog2.service;

import integrado.prog2.dao.ProductoDAO;
import integrado.prog2.entities.Producto;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.exception.PersistenciaException;
import integrado.prog2.exception.StockInvalidoException;

import java.util.List;

/** Casos de uso de Producto. Destino de los datos: BASE DE DATOS (ProductoDAO). */
public class ProductoService {

    private final ProductoDAO productoDAO;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoDAO productoDAO, CategoriaService categoriaService) {
        this.productoDAO = productoDAO;
        this.categoriaService = categoriaService;
    }

    public List<Producto> listar() throws PersistenciaException {
        return productoDAO.findAll();
    }

    /** Producto activo o EntidadNoEncontradaException. */
    public Producto buscar(Long id) throws FoodStoreException {
        return productoDAO.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("el producto", id));
    }

    /**
     * Alta de producto. La categoria debe existir y estar activa; su id se pasa
     * al DAO pero no se guarda en el objeto Producto.
     */
    public Producto crear(String nombre, String descripcion, Double precio, int stock,
                          String imagen, boolean disponible, Long idCategoria) throws FoodStoreException {
        Producto p = new Producto(
                Validador.obligatorio(nombre, "nombre", 100),
                Validador.precio(precio),
                Validador.opcional(descripcion, "descripcion", 255),
                validarStock(stock),
                Validador.opcional(imagen, "imagen", 255),
                disponible);
        categoriaService.buscar(idCategoria); // lanza excepcion si no existe o esta eliminada
        return productoDAO.insert(p, idCategoria);
    }

    /** Edicion de atributos propios. Los parametros null (o texto vacio) conservan el valor anterior. */
    public Producto editar(Long id, String nombre, String descripcion, Double precio, Integer stock,
                           String imagen, Boolean disponible) throws FoodStoreException {
        Producto p = buscar(id);
        String n = Validador.opcional(nombre, "nombre", 100);
        String d = Validador.opcional(descripcion, "descripcion", 255);
        String img = Validador.opcional(imagen, "imagen", 255);
        if (n != null) p.setNombre(n);
        if (d != null) p.setDescripcion(d);
        if (img != null) p.setImagen(img);
        if (precio != null) p.setPrecio(Validador.precio(precio));
        if (stock != null) p.setStock(validarStock(stock));
        if (disponible != null) p.setDisponible(disponible);
        if (!productoDAO.update(p)) {
            throw new EntidadNoEncontradaException("el producto", id);
        }
        return p;
    }

    /** Operacion aparte: solo cambia la clave foranea categoria_id. */
    public void cambiarCategoria(Long idProducto, Long idCategoriaNueva) throws FoodStoreException {
        buscar(idProducto);
        categoriaService.buscar(idCategoriaNueva);
        if (!productoDAO.reasignarCategoria(idProducto, idCategoriaNueva)) {
            throw new EntidadNoEncontradaException("el producto", idProducto);
        }
    }

    public void eliminar(Long id) throws FoodStoreException {
        buscar(id);
        if (!productoDAO.softDelete(id)) {
            throw new EntidadNoEncontradaException("el producto", id);
        }
    }

    private int validarStock(int stock) throws StockInvalidoException {
        if (stock < 0) {
            throw new StockInvalidoException("El stock no puede ser negativo (ingresado: " + stock + ").");
        }
        return stock;
    }
}

package integrado.prog2.service;

import integrado.prog2.dao.CategoriaDAO;
import integrado.prog2.dao.ProductoDAO;
import integrado.prog2.entities.Categoria;
import integrado.prog2.exception.DuplicadoException;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.exception.PersistenciaException;
import integrado.prog2.exception.ReglaNegocioException;

import java.util.List;

/** Casos de uso de Categoria. Destino de los datos: BASE DE DATOS (CategoriaDAO). */
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;
    private final ProductoDAO productoDAO;

    public CategoriaService(CategoriaDAO categoriaDAO, ProductoDAO productoDAO) {
        this.categoriaDAO = categoriaDAO;
        this.productoDAO = productoDAO;
    }

    public List<Categoria> listar() throws PersistenciaException {
        return categoriaDAO.findAll();
    }

    /** Categoria activa o EntidadNoEncontradaException. */
    public Categoria buscar(Long id) throws FoodStoreException {
        return categoriaDAO.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("la categoria", id));
    }

    /** Devuelve la categoria con su lista de productos cargada (agregacion 1:N). */
    public Categoria buscarConProductos(Long id) throws FoodStoreException {
        Categoria categoria = buscar(id);
        categoria.setProductos(productoDAO.findByCategoria(id));
        return categoria;
    }

    public Categoria crear(String nombre, String descripcion) throws FoodStoreException {
        String n = Validador.obligatorio(nombre, "nombre", 100);
        String d = Validador.obligatorio(descripcion, "descripcion", 255);
        validarNombreUnico(n, null);
        return categoriaDAO.insert(new Categoria(n, d));
    }

    /** Los parametros vacios o null conservan el valor anterior. */
    public Categoria editar(Long id, String nuevoNombre, String nuevaDescripcion) throws FoodStoreException {
        Categoria categoria = buscar(id);
        String n = Validador.opcional(nuevoNombre, "nombre", 100);
        String d = Validador.opcional(nuevaDescripcion, "descripcion", 255);
        if (n != null) {
            validarNombreUnico(n, id);
            categoria.setNombre(n);
        }
        if (d != null) {
            categoria.setDescripcion(d);
        }
        if (!categoriaDAO.update(categoria)) {
            throw new EntidadNoEncontradaException("la categoria", id);
        }
        return categoria;
    }

    /** Baja logica. Regla de la catedra: no se permite si tiene productos activos. */
    public void eliminar(Long id) throws FoodStoreException {
        buscar(id);
        int productosActivos = categoriaDAO.contarProductosActivos(id);
        if (productosActivos > 0) {
            throw new ReglaNegocioException("No se puede eliminar la categoria: tiene " + productosActivos
                    + " producto(s) activo(s) asociado(s). Elimine o reasigne esos productos primero.");
        }
        if (!categoriaDAO.softDelete(id)) {
            throw new EntidadNoEncontradaException("la categoria", id);
        }
    }

    /** La unicidad se valida con una consulta SQL, no recorriendo una coleccion. */
    private void validarNombreUnico(String nombre, Long excluirId) throws FoodStoreException {
        if (categoriaDAO.existeNombreActivo(nombre, excluirId)) {
            throw new DuplicadoException("Ya existe una categoria activa con el nombre '" + nombre + "'.");
        }
    }
}

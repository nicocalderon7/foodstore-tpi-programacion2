package integrado.prog2.repository;

import integrado.prog2.entities.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Usuarios en MEMORIA, guardados en un ArrayList.
 * El id lo genera el propio programa con un contador.
 */
public class UsuarioRepository implements RepositorioEnMemoria<Usuario> {

    private final List<Usuario> usuarios = new ArrayList<>();
    private long ultimoId = 0;

    @Override
    public Usuario guardar(Usuario usuario) {
        usuario.setId(++ultimoId);
        usuarios.add(usuario);
        return usuario;
    }

    @Override
    public List<Usuario> buscarActivos() {
        return usuarios.stream().filter(u -> !u.isEliminado()).toList();
    }

    /** Todos los usuarios, incluidos los eliminados (para consultar el historial de pedidos). */
    public List<Usuario> buscarTodos() {
        return Collections.unmodifiableList(usuarios);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarios.stream()
                .filter(u -> !u.isEliminado() && u.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean eliminar(Long id) {
        Optional<Usuario> usuario = buscarPorId(id);
        usuario.ifPresent(u -> u.setEliminado(true));
        return usuario.isPresent();
    }

    /**
     * Recorre la coleccion para saber si el mail ya lo usa otro usuario activo.
     *
     * @param excluirId id del propio usuario al editar (null al crear)
     */
    public boolean existeMail(String mail, Long excluirId) {
        for (Usuario u : usuarios) {
            if (!u.isEliminado()
                    && u.getMail().equalsIgnoreCase(mail)
                    && !u.getId().equals(excluirId)) {
                return true;
            }
        }
        return false;
    }
}

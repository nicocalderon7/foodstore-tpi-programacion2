package integrado.prog2.service;

import integrado.prog2.entities.Usuario;
import integrado.prog2.enums.Rol;
import integrado.prog2.exception.DuplicadoException;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.exception.ValidacionException;
import integrado.prog2.repository.UsuarioRepository;

import java.util.List;

/** Casos de uso de Usuario. Destino de los datos: MEMORIA (UsuarioRepository). */
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listar() {
        return usuarioRepository.buscarActivos();
    }

    public Usuario buscar(Long id) throws EntidadNoEncontradaException {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("el usuario", id));
    }

    public Usuario crear(String nombre, String apellido, String mail, String celular,
                         String contrasena, Rol rol) throws FoodStoreException {
        String m = Validador.mail(mail);
        validarMailUnico(m, null);
        Usuario u = new Usuario(
                Validador.obligatorio(nombre, "nombre", 60),
                Validador.obligatorio(apellido, "apellido", 60),
                m,
                Validador.celular(celular),
                validarContrasena(contrasena),
                validarRol(rol));
        return usuarioRepository.guardar(u);
    }

    /** Los parametros null o vacios conservan el valor anterior. */
    public Usuario editar(Long id, String nombre, String apellido, String mail, String celular,
                          String contrasena, Rol rol) throws FoodStoreException {
        Usuario u = buscar(id);
        // Primero se validan todos los datos y recien despues se modifica el objeto,
        // para no dejarlo a medio actualizar si algun dato es invalido.
        String n = Validador.opcional(nombre, "nombre", 60);
        String a = Validador.opcional(apellido, "apellido", 60);
        String m = (mail == null || mail.isBlank()) ? null : Validador.mail(mail);
        String c = (celular == null || celular.isBlank()) ? null : Validador.celular(celular);
        String pass = (contrasena == null || contrasena.isBlank()) ? null : validarContrasena(contrasena);
        if (m != null) {
            validarMailUnico(m, id); // se excluye al propio usuario
        }
        if (n != null) u.setNombre(n);
        if (a != null) u.setApellido(a);
        if (m != null) u.setMail(m);
        if (c != null) u.setCelular(c);
        if (pass != null) u.setContrasena(pass);
        if (rol != null) u.setRol(rol);
        return u;
    }

    /** Baja logica: el usuario queda en la coleccion (con sus pedidos como historial). */
    public void eliminar(Long id) throws EntidadNoEncontradaException {
        if (!usuarioRepository.eliminar(id)) {
            throw new EntidadNoEncontradaException("el usuario", id);
        }
    }

    private void validarMailUnico(String mail, Long excluirId) throws DuplicadoException {
        if (usuarioRepository.existeMail(mail, excluirId)) {
            throw new DuplicadoException("El mail '" + mail + "' ya esta registrado por otro usuario.");
        }
    }

    private String validarContrasena(String contrasena) throws ValidacionException {
        String pass = Validador.obligatorio(contrasena, "contrasena", 60);
        if (pass.length() < 4) {
            throw new ValidacionException("La contrasena debe tener al menos 4 caracteres.");
        }
        return pass;
    }

    private Rol validarRol(Rol rol) throws ValidacionException {
        if (rol == null) {
            throw new ValidacionException("El rol es obligatorio.");
        }
        return rol;
    }
}

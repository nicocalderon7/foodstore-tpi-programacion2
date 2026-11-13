package integrado.prog2.repository;

import integrado.prog2.entities.Pedido;
import integrado.prog2.entities.Usuario;

/**
 * Resultado de una busqueda de pedidos: el pedido junto al usuario en cuya
 * lista se encontro.
 *
 * No es una entidad ni agrega un atributo "usuario" a Pedido: como la relacion
 * Usuario -> Pedido es unidireccional, para saber de quien es un pedido hay que
 * recorrer los usuarios, y este record solo transporta ese resultado.
 */
public record PedidoConUsuario(Usuario usuario, Pedido pedido) {
}

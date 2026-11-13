package integrado.prog2.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clase abstracta base del modelo (UML). Todas las entidades heredan de ella.
 * Concentra los atributos comunes: identificador, marca de baja logica y fecha de creacion.
 */
public abstract class Base {

    private Long id;
    private boolean eliminado;
    private LocalDateTime createdAt;

    protected Base() {
        this.eliminado = false;
        this.createdAt = LocalDateTime.now();
    }

    protected Base(Long id, boolean eliminado, LocalDateTime createdAt) {
        this.id = id;
        this.eliminado = eliminado;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Dos entidades son iguales si son de la misma clase y tienen el mismo id.
     * Si alguna todavia no tiene id (no fue guardada), solo es igual a si misma.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Base otra = (Base) o;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}

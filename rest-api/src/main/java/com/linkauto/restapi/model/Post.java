package com.linkauto.restapi.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * @brief Representa una publicación en la red social.
 * 
 * Esta clase modela una publicación realizada por un usuario, que puede contener 
 * un mensaje, imágenes, comentarios y "likes".
 */
@Entity
@Table(name = "post")
public class Post {

    /** Identificador único de la publicación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que ha realizado la publicación. */
    @ManyToOne()
    @JoinColumn(name = "username", nullable = false)
    private User usuario;

    /** Mensaje de texto de la publicación. */
    private String mensaje;

    /** Fecha de creación de la publicación (en formato timestamp). */
    private long fechaCreacion;

    /**
     * Lista de URLs de imágenes asociadas a la publicación.
     * Se almacena en una tabla separada llamada "post_images".
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private final List<String> imagenes;

    /**
     * Lista de comentarios asociados a la publicación.
     * Se gestiona en cascada y con eliminación automática.
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Comment> comentarios;

    /**
     * Conjunto de nombres de usuario que han dado "like" a la publicación.
     * Se almacena en una tabla separada llamada "post_likes".
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "likes")
    private Set<String> likes;

    /** Constructor por defecto. Inicializa listas vacías. */
    public Post() {
        this.imagenes = new ArrayList<>();
        this.comentarios = new ArrayList<>();
        this.likes = new HashSet<>();
    }

    /**
     * Constructor completo.
     * 
     * @param id Identificador de la publicación.
     * @param usuario Usuario que realiza la publicación.
     * @param mensaje Texto de la publicación.
     * @param fechaCreacion Fecha de creación de la publicación.
     * @param imagenes Lista de URLs de imágenes.
     * @param comentarios Lista de comentarios.
     * @param likes Conjunto de usuarios que dieron like.
     */
    public Post(Long id, User usuario, String mensaje, long fechaCreacion, List<String> imagenes, List<Comment> comentarios, Set<String> likes) {
        this.id = id;
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.fechaCreacion = fechaCreacion;
        this.imagenes = new ArrayList<>();
        for (String imagen : imagenes) {
            this.imagenes.add(imagen);
        }
        this.comentarios = new ArrayList<>();
        for (Comment comentario : comentarios) {
            this.comentarios.add(comentario);
        }
        this.likes = new HashSet<>(likes);
    }

    /** @return ID de la publicación. */
    public Long getId() {
        return id;
    }

    /** @return Usuario que creó la publicación. */
    public User getUsuario() {
        return usuario;
    }

    /**
     * Establece el usuario que realiza la publicación.
     * @param usuario Usuario autor.
     */
    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    /** @return Mensaje de la publicación. */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Establece el mensaje de la publicación.
     * @param mensaje Texto del post.
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /** @return Fecha de creación del post. */
    public long getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Establece la fecha de creación.
     * @param fechaCreacion Fecha en formato timestamp.
     */
    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /** @return Lista de URLs de imágenes. */
    public List<String> getImagenes() {
        return imagenes;
    }

    /**
     * Añade una imagen a la publicación.
     * @param imagen URL de la imagen.
     */
    public void addImagen(String imagen) {
        this.imagenes.add(imagen);
    }

    /** @return Lista de comentarios de la publicación. */
    public List<Comment> getComentarios() {
        return comentarios;
    }

    /**
     * Establece la lista completa de comentarios.
     * @param comentarios Lista de comentarios.
     */
    public void setComentarios(List<Comment> comentarios) {
        this.comentarios = comentarios;
    }

    /**
     * Añade un comentario a la publicación.
     * @param comentario Comentario a añadir.
     */
    public void addComentario(Comment comentario) {
        this.comentarios.add(comentario);
    }

    /**
     * Añade un "like" a la publicación.
     * @param u Nombre de usuario que da like.
     */
    public void setLikes(String u) {
        this.likes.add(u);
    }

    /**
     * Elimina un "like" de la publicación.
     * @param u Nombre de usuario a eliminar del conjunto de likes.
     */
    public void removeLikes(String u) {
        this.likes.remove(u);
    }

    /** @return Conjunto de nombres de usuario que han dado like. */
    public Set<String> getLikes() {
        return likes;
    }

    /** @return Código hash de la publicación. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return id != null && id.equals(post.id);
    }
  
    /**
     * Compara dos publicaciones por su contenido.
     * @param obj Objeto a comparar.
     * @return true si los objetos son equivalentes.
     */

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    /** @return Cadena representando el contenido del post. */
    @Override
    public String toString() {
        return "Post [id=" + id + ", usuario=" + usuario.getUsername() + ", mensaje=" + mensaje + ", fechaCreacion=" + fechaCreacion
                + ", imagenes=" + imagenes + ", comentarios=" + comentarios + ", likes=" + likes + "]";
    }
}
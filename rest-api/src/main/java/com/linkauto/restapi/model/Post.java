package com.linkauto.restapi.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne()
    @JoinColumn(name = "username", nullable = false)        
    private User usuario;      
    private String mensaje;      
    private long fechaCreacion; 
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private final List<String> imagenes;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Comment> comentarios; 
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "likes")
    private Set<String> likes; 

    public Post() {
        this.imagenes = new ArrayList<>();
    }

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


    public Long getId() {
        return id;
    }
    
    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void addImagen(String imagen) {
        this.imagenes.add(imagen);
    }

    public List<Comment> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comment> comentarios) {
        this.comentarios = comentarios;
    }

    public void addComentario(Comment comentario) {
        this.comentarios.add(comentario);
    }

    public void setLikes(String u) {
        this.likes.add(u);
    }

    public void removeLikes(String u) {
        this.likes.remove(u);
    }

    public Set<String> getLikes() {
        return likes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, usuario, mensaje, fechaCreacion, imagenes, comentarios, likes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Post other = (Post) obj;
        return Objects.equals(id, other.id) &&
            Objects.equals(usuario, other.usuario) &&
            Objects.equals(mensaje, other.mensaje) &&
            Objects.equals(fechaCreacion, other.fechaCreacion) &&
            Objects.equals(imagenes, other.imagenes) &&
            Objects.equals(comentarios, other.comentarios) &&
            Objects.equals(likes, other.likes);
    }

    @Override
    public String toString() {
        return "Post [id=" + id + ", usuario=" + usuario + ", mensaje=" + mensaje 
                + ", fechaCreacion=" + fechaCreacion + ", imagenes=" + imagenes + ", comentarios=" + comentarios + "]";
    }
}


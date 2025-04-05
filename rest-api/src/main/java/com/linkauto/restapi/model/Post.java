package com.linkauto.restapi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

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
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;

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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_comments", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "comment")
    private Map<User, List<String>> comentarios = new HashMap<>(); 
    @ManyToMany
    @JoinTable(
    name = "post_likes",
    joinColumns = @JoinColumn(name = "id"),
    inverseJoinColumns = @JoinColumn(name = "username")
    )
    private Set<User> likes; 

    public Post() {
        this.imagenes = new ArrayList<>();
    }

    public Post(Long id, User usuario, String mensaje, long fechaCreacion, List<String> imagenes, Map<User, List<String>> comentarios, Set<User> likes) {
        this.id = id;
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.fechaCreacion = fechaCreacion;
        this.imagenes = new ArrayList<>();
        for (String imagen : imagenes) {
        	this.imagenes.add(imagen);
		}
        this.comentarios = new HashMap<>();
        for (Map.Entry<User, List<String>> entry : comentarios.entrySet()) {
            this.comentarios.put(entry.getKey(), new ArrayList<>(entry.getValue()));
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

    public Map<User, List<String>> getComentarios() {
        return comentarios;
    }

    public void setComentarios(Map<User, List<String>> comentarios) {
        this.comentarios = comentarios;
    }

    public void setLikes(Set<User> likes) {
        this.likes = likes;
    }

    public Set<User> getLikes() {
        return likes;
    }
    
    @Override
    public String toString() {
        return "Post [id=" + id + ", usuario=" + usuario + ", mensaje=" + mensaje 
                + ", fechaCreacion=" + fechaCreacion + ", imagenes=" + imagenes + ", comentarios=" + comentarios + "]";
    }
}


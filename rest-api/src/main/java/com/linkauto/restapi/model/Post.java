package com.linkauto.restapi.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "username", nullable = false)        
    private User usuario;      
    private String mensaje;      
    private long fechaCreacion;  
    private List<String> imagenes; 

    public Post() {
        this.imagenes = new ArrayList<>();
    }

    public Post(Long id, User usuario, String mensaje, long fechaCreacion, List<String> imagenes) {
        this.id = id;
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.fechaCreacion = fechaCreacion;
        this.imagenes = new ArrayList<>();
        for (String imagen : imagenes) {
        	this.imagenes.add(imagen);
		}
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

    @Override
    public String toString() {
        return "Post [id=" + id + ", usuario=" + usuario + ", mensaje=" + mensaje 
                + ", fechaCreacion=" + fechaCreacion + ", imagenes=" + imagenes + "]";
    }
}


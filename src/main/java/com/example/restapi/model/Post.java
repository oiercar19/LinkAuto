package com.example.restapi.model;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private Long id;             
    private String usuario;      
    private String mensaje;      
    private long fechaCreacion;  
    private List<String> imagenes; 

    public Post() {
        this.imagenes = new ArrayList<>();
    }

    public Post(Long id, String usuario, String mensaje, long fechaCreacion, List<String> imagenes) {
        this.id = id;
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.fechaCreacion = fechaCreacion;
        this.imagenes = new ArrayList<String>();
        for (String imagen : imagenes) {
        	this.imagenes.add(imagen);
		}
    }


    public Long getId() {
        return id;
    }
    
    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
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


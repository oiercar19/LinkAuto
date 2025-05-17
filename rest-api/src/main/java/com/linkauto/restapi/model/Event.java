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
import jakarta.persistence.Transient;

@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne()
    @JoinColumn(name = "username", nullable = false)        
    private User creador;
    
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private long fechaInicio;
    private long fechaFin;
    
    // Añadimos esta propiedad para que coincida con el método del repositorio
    private long eventDate;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    private final List<String> imagenes;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "participant")
    private Set<String> participantes;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Comment> comentarios;

    public Event() {
        this.imagenes = new ArrayList<>();
        this.participantes = new HashSet<>();
        this.comentarios = new ArrayList<>();
    }

    public Event(Long id, User creador, String titulo, String descripcion, String ubicacion, 
                long fechaInicio, long fechaFin, List<String> imagenes, 
                Set<String> participantes, List<Comment> comentarios) {
        this.id = id;
        this.creador = creador;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        // Añadimos también el eventDate con el mismo valor que fechaInicio
        this.eventDate = fechaInicio;
        this.imagenes = new ArrayList<>();
        for (String imagen : imagenes) {
            this.imagenes.add(imagen);
        }
        this.participantes = new HashSet<>(participantes);
        this.comentarios = new ArrayList<>();
        for (Comment comentario : comentarios) {
            this.comentarios.add(comentario);
        }
    }

    public Long getId() {
        return id;
    }

    public User getCreador() {
        return creador;
    }

    public void setCreador(User creador) {
        this.creador = creador;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
        // Actualizamos también eventDate cuando se actualiza fechaInicio
        this.eventDate = fechaInicio;
    }

    public long getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(long fechaFin) {
        this.fechaFin = fechaFin;
    }
    
    // Añadimos getters y setters para eventDate
    public long getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void addImagen(String imagen) {
        this.imagenes.add(imagen);
    }

    public Set<String> getParticipantes() {
        return participantes;
    }
    
    public void addParticipante(String participante) {
        this.participantes.add(participante);
    }
    
    public void removeParticipante(String participante) {
        this.participantes.remove(participante);
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

    @Override
    public int hashCode() {
        return Objects.hash(id, creador, titulo, descripcion, ubicacion, fechaInicio, fechaFin, 
                           eventDate, imagenes, participantes, comentarios);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        return Objects.equals(id, other.id) &&
            Objects.equals(creador, other.creador) &&
            Objects.equals(titulo, other.titulo) &&
            Objects.equals(descripcion, other.descripcion) &&
            Objects.equals(ubicacion, other.ubicacion) &&
            Objects.equals(fechaInicio, other.fechaInicio) &&
            Objects.equals(fechaFin, other.fechaFin) &&
            Objects.equals(eventDate, other.eventDate) &&
            Objects.equals(imagenes, other.imagenes) &&
            Objects.equals(participantes, other.participantes) &&
            Objects.equals(comentarios, other.comentarios);
    }

    @Override
    public String toString() {
        return "Event [id=" + id + ", creador=" + creador.getUsername() + ", titulo=" + titulo + 
                ", descripcion=" + descripcion + ", ubicacion=" + ubicacion + 
                ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin + 
                ", eventDate=" + eventDate +
                ", imagenes=" + imagenes + ", participantes=" + participantes + 
                ", comentarios=" + comentarios + "]";
    }
}
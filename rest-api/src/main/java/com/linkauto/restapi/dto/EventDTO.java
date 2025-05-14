/**
 *
 * @author YourName
 */

 package com.linkauto.restapi.dto;

 import java.util.List;
 import java.util.Set;
 
 public class EventDTO {
     private Long id;
     private String creador;
     private String titulo;
     private String descripcion;
     private String ubicacion;
     private long fechaInicio;
     private long fechaFin;
     private List<String> imagenes;
     private Set<String> participantes;
     private List<CommentDTO> comentarios;
 
     public EventDTO() {
     }
 
     public EventDTO(Long id, String creador, String titulo, String descripcion, String ubicacion,
                   long fechaInicio, long fechaFin, List<String> imagenes,
                   Set<String> participantes, List<CommentDTO> comentarios) {
         this.id = id;
         this.creador = creador;
         this.titulo = titulo;
         this.descripcion = descripcion;
         this.ubicacion = ubicacion;
         this.fechaInicio = fechaInicio;
         this.fechaFin = fechaFin;
         this.imagenes = imagenes;
         this.participantes = participantes;
         this.comentarios = comentarios;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getCreador() {
         return creador;
     }
 
     public void setCreador(String creador) {
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
     }
 
     public long getFechaFin() {
         return fechaFin;
     }
 
     public void setFechaFin(long fechaFin) {
         this.fechaFin = fechaFin;
     }
 
     public List<String> getImagenes() {
         return imagenes;
     }
 
     public void setImagenes(List<String> imagenes) {
         this.imagenes = imagenes;
     }
 
     public Set<String> getParticipantes() {
         return participantes;
     }
 
     public void setParticipantes(Set<String> participantes) {
         this.participantes = participantes;
     }
 
     public List<CommentDTO> getComentarios() {
         return comentarios;
     }
 
     public void setComentarios(List<CommentDTO> comentarios) {
         this.comentarios = comentarios;
     }
 }
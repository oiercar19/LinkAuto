package com.linkauto.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.linkauto.restapi.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Transactional
    void deleteById(long id);
    
    List<Event> findByCreador_Username(String username);
    
    List<Event> findByParticipantesContaining(String username);
    
    List<Event> findByEventDateGreaterThanOrderByEventDateAsc(long currentDate);
}
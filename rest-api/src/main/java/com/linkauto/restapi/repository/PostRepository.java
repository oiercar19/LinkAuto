package com.linkauto.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.linkauto.restapi.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Transactional
    void deleteById(long id);
    
    List<Post> findByUsuario_Username(String username);
}

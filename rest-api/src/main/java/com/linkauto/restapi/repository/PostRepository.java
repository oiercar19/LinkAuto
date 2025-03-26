package com.linkauto.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.linkauto.restapi.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> { 
}

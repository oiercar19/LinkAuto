package com.linkauto.restapi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.linkauto.restapi.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

}

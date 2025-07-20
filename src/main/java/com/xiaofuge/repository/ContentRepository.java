package com.xiaofuge.repository;

import com.xiaofuge.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    
    List<Content> findByStatus(Content.ReviewStatus status);
    
    List<Content> findByAuthorId(String authorId);
    
    List<Content> findByType(Content.ContentType type);
}
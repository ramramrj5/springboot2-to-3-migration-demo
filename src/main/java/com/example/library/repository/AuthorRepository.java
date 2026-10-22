package com.example.library.repository;

import com.example.library.domain.Author;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    /** Fetch authors and their books in a single LEFT JOIN instead of 1 + N selects. */
    @EntityGraph(attributePaths = "books")
    List<Author> findAllWithBooks();
}

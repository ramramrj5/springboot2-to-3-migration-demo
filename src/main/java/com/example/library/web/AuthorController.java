package com.example.library.web;

import com.example.library.domain.Author;
import com.example.library.domain.Book;
import com.example.library.dto.AuthorResponse;
import com.example.library.repository.AuthorRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /**
     * Classic N+1: findAll() loads authors in 1 query, then each author.getBooks()
     * triggers one more SELECT. 20 authors => 21 queries.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public List<AuthorResponse> all() {
        return authorRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuthorResponse toResponse(Author a) {
        List<String> titles = a.getBooks().stream()
                .map(Book::getTitle)
                .collect(Collectors.toList());
        return new AuthorResponse(a.getId(), a.getName(), a.getCountry(), titles);
    }
}

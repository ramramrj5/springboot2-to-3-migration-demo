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

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /** One query: authors LEFT JOIN books via @EntityGraph. Was 21 queries on the legacy branch. */
    @GetMapping
    @Transactional(readOnly = true)
    public List<AuthorResponse> all() {
        return authorRepository.findAllWithBooks().stream()
                .map(this::toResponse)
                .toList();
    }

    private AuthorResponse toResponse(Author a) {
        List<String> titles = a.getBooks().stream().map(Book::getTitle).toList();
        return new AuthorResponse(a.getId(), a.getName(), a.getCountry(), titles);
    }
}

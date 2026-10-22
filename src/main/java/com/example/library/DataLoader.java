package com.example.library;

import com.example.library.domain.Author;
import com.example.library.domain.Book;
import com.example.library.repository.AuthorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final AuthorRepository authorRepository;

    public DataLoader(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public void run(String... args) {
        if (authorRepository.count() > 0) {
            return;
        }
        List<Author> authors = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Author a = new Author("Author " + i, i % 2 == 0 ? "IN" : "UK");
            for (int b = 1; b <= 5; b++) {
                a.addBook(new Book("Book " + i + "-" + b, 1990 + b));
            }
            authors.add(a);
        }
        authorRepository.saveAll(authors);
    }
}

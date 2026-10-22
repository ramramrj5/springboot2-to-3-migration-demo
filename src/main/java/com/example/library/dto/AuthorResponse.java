package com.example.library.dto;

import java.util.List;

public class AuthorResponse {

    private final Long id;
    private final String name;
    private final String country;
    private final List<String> books;

    public AuthorResponse(Long id, String name, String country, List<String> books) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.books = books;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public List<String> getBooks() {
        return books;
    }
}

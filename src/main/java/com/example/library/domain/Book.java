package com.example.library.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int publishedYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private Author author;

    protected Book() {
    }

    public Book(String title, int publishedYear) {
        this.title = title;
        this.publishedYear = publishedYear;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getPublishedYear() {
        return publishedYear;
    }

    public Author getAuthor() {
        return author;
    }

    void setAuthor(Author author) {
        this.author = author;
    }
}

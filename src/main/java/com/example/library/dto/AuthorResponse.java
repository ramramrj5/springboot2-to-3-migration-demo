package com.example.library.dto;

import java.util.List;

public record AuthorResponse(Long id, String name, String country, List<String> books) {
}

package com.example.brisa.dtos.people;

public record PeopleFilterOptionDTO(
        Long id,
        String label,
        Long programaId
) {
    public PeopleFilterOptionDTO(Long id, String label) {
        this(id, label, null);
    }
}

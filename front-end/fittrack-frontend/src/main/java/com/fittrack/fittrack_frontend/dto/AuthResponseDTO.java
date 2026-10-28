package com.fittrack.fittrack_frontend.dto;

public record AuthResponseDTO(
    String token,
    Long praticanteId,
    String nome,
    String email
) {}

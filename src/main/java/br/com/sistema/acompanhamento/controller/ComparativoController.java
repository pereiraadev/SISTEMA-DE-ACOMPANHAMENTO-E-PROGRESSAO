package br.com.sistema.acompanhamento.controller;

import br.com.sistema.acompanhamento.dto.ComparativoDTO;
import br.com.sistema.acompanhamento.service.ComparativoService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/treinos", produces = MediaType.APPLICATION_JSON_VALUE)
public class ComparativoController {

    private final ComparativoService service;

    public ComparativoController(ComparativoService service) {
        this.service = service;
    }

    // GET /api/treinos/12/comparativo/3
    // Compara o exercicio 3 do treino 12 com a sessao anterior que tambem fez esse exercicio.
    @GetMapping("/{id}/comparativo/{exercicioId}")
    public ComparativoDTO comparativo(
            @PathVariable Long id,
            @PathVariable Long exercicioId,
            Authentication authentication) {

        if (id == null || id <= 0 || exercicioId == null || exercicioId <= 0) {
            throw new IllegalArgumentException("id e exercicioId devem ser maiores que zero.");
        }
        if (authentication == null) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }

        return service.comparar(authentication.getName(), id, exercicioId);
    }
}

package br.com.sistema.acompanhamento.controller;

import br.com.sistema.acompanhamento.dto.DashboardDTO;
import br.com.sistema.acompanhamento.service.DashboardService;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/praticantes", produces = MediaType.APPLICATION_JSON_VALUE)
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    // GET /api/praticantes/dashboard
    // Devolve KPIs, grupos musculares, exercicios mais feitos e estagnacao num json so.
    @GetMapping("/dashboard")
    public DashboardDTO dashboard(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }
        return service.montar(authentication.getName());
    }
}

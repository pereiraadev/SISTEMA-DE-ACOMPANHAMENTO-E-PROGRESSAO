package com.fittrack.fittrack_frontend.service;

import org.springframework.stereotype.Service;

@Service
public class PractitionerSession {
    private String nome;

    public synchronized void definir(String nome) {
        this.nome = nome;
    }

    public synchronized String obterNome() {
        return nome == null || nome.isBlank() ? "Praticante" : nome;
    }

    public synchronized void limpar() {
        nome = null;
    }
}

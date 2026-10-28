package com.fittrack.fittrack_frontend.client;

import java.util.List;

import com.fittrack.fittrack_frontend.dto.MetricaTreinoDTO;
import com.fittrack.fittrack_frontend.dto.RegistroHistoricoDTO;
import com.fittrack.fittrack_frontend.dto.TreinoAtivoDTO;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP das operações de Treino (sessão ativa, finalização e métricas por treino).
 * Todos os métodos são bloqueantes: chame-os fora da JavaFX Application Thread (veja FxAsync).
 */
@Component
public class TreinoApiClient {

    private final RestClient restClient;

    public TreinoApiClient(RestClient fitTrackRestClient) {
        this.restClient = fitTrackRestClient;
    }

    /**
     * Cria (clona do modelo do dia) ou retoma a sessão de treino do praticante.
     * Retorna null quando não há treino planejado para hoje.
     */
    public TreinoAtivoDTO buscarTreinoDoDia(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/treino-do-dia", praticanteId)
                .retrieve()
                .body(TreinoAtivoDTO.class));
    }

    /** Persiste as séries executadas e conclui o treino (status CONCLUIDO, finalizado_em). */
    public void finalizarTreino(RegistroHistoricoDTO registro) {
        ApiException.chamar(() -> restClient.post()
                .uri("/api/treinos/{id}/finalizar", registro.treinoId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(registro)
                .retrieve()
                .toBodilessEntity());
    }

    /** Aba "Treinos" das métricas: consistência e total de sessões por ficha. */
    public List<MetricaTreinoDTO> listarMetricasTreinos(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/metricas/treinos", praticanteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<MetricaTreinoDTO>>() {}));
    }
}

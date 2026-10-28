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
 * Cliente HTTP das operações de treino, conectando a execução da sessão e suas métricas à API.
 * Como as requisições bloqueiam a thread chamadora, os controllers as delegam a {@link com.fittrack.fittrack_frontend.util.FxAsync}.
 */
@Component
public class TreinoApiClient {

    /** Cliente HTTP compartilhado e configurado pelo Spring. */
    private final RestClient restClient;

    /**
     * Recebe a configuração HTTP usada para acessar os endpoints de treino.
     *
     * @param fitTrackRestClient cliente Spring com URL-base e autenticação
     */
    public TreinoApiClient(RestClient fitTrackRestClient) {
        this.restClient = fitTrackRestClient;
    }

    /**
     * Solicita a sessão do dia; a API pode criar uma sessão a partir da ficha ou retomar uma existente.
     *
     * @param praticanteId identificador do praticante autenticado
     * @return treino ativo, ou {@code null} quando não há treino planejado
     */
    public TreinoAtivoDTO buscarTreinoDoDia(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/treino-do-dia", praticanteId)
                .retrieve()
                .body(TreinoAtivoDTO.class));
    }

    /**
     * Envia as séries registradas e solicita a conclusão da sessão.
     *
     * @param registro duração e séries a persistir
     */
    public void finalizarTreino(RegistroHistoricoDTO registro) {
        ApiException.chamar(() -> restClient.post()
                .uri("/api/treinos/{id}/finalizar", registro.treinoId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(registro)
                .retrieve()
                .toBodilessEntity());
    }

    /**
     * Consulta consistência e quantidade de sessões por ficha para a aba de métricas.
     *
     * @param praticanteId identificador do praticante autenticado
     * @return métricas das fichas do praticante
     */
    public List<MetricaTreinoDTO> listarMetricasTreinos(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/metricas/treinos", praticanteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<MetricaTreinoDTO>>() {}));
    }
}

package com.fittrack.fittrack_frontend.client;

import java.util.List;

import com.fittrack.fittrack_frontend.dto.AuthResponseDTO;
import com.fittrack.fittrack_frontend.dto.CadastroDTO;
import com.fittrack.fittrack_frontend.dto.DesempenhoGrupoDTO;
import com.fittrack.fittrack_frontend.dto.DetalheGrupoDTO;
import com.fittrack.fittrack_frontend.dto.EstatisticasGeraisDTO;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.HistoricoExercicioItemDTO;
import com.fittrack.fittrack_frontend.dto.LoginDTO;
import com.fittrack.fittrack_frontend.dto.RecordesPessoaisDTO;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP das operações do Praticante: autenticação, estatísticas, catálogo de exercícios,
 * desempenho por grupo muscular e recordes pessoais.
 * Métodos bloqueantes: chame-os fora da JavaFX Application Thread (veja FxAsync).
 */
@Component
public class PraticanteApiClient {

    private final RestClient restClient;

    public PraticanteApiClient(RestClient fitTrackRestClient) {
        this.restClient = fitTrackRestClient;
    }

    public AuthResponseDTO login(LoginDTO dto) {
        return ApiException.chamar(() -> restClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(AuthResponseDTO.class));
    }

    public AuthResponseDTO cadastrar(CadastroDTO dto) {
        return ApiException.chamar(() -> restClient.post()
                .uri("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(AuthResponseDTO.class));
    }

    /** Praticante.consultarEstatisticasGerais(). */
    public EstatisticasGeraisDTO buscarEstatisticasGerais(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/estatisticas", praticanteId)
                .retrieve()
                .body(EstatisticasGeraisDTO.class));
    }

    /** Praticante.obterExerciciosMaisRealizados(). */
    public List<ExercicioDTO> listarExerciciosMaisRealizados(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/exercicios/mais-realizados", praticanteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExercicioDTO>>() {}));
    }

    /** Catálogo de exercícios ativos do praticante (aba "Exercícios"). */
    public List<ExercicioDTO> listarExercicios(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/exercicios", praticanteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExercicioDTO>>() {}));
    }

    /** Exercicio.consultarHistoricoTreinos(): execuções passadas de um exercício. */
    public List<HistoricoExercicioItemDTO> listarHistoricoExercicio(Long praticanteId, Long exercicioId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/exercicios/{exercicioId}/historico", praticanteId, exercicioId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<HistoricoExercicioItemDTO>>() {}));
    }

    /** Aba "Grupos Musculares": um DesempenhoGrupoDTO por GrupoMuscularEnum. */
    public List<DesempenhoGrupoDTO> listarDesempenhoGrupos(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/metricas/grupos", praticanteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<DesempenhoGrupoDTO>>() {}));
    }

    /** Detalhe de um grupo (PEITO, COSTAS, PERNAS, OMBROS, BRACOS). */
    public DetalheGrupoDTO buscarDetalheGrupo(Long praticanteId, String grupoMuscular) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/metricas/grupos/{grupo}", praticanteId, grupoMuscular)
                .retrieve()
                .body(DetalheGrupoDTO.class));
    }

    /** Hall da Fama: recordes pessoais sincronizados com os campos recorde_* de EXERCICIO. */
    public RecordesPessoaisDTO buscarRecordesPessoais(Long praticanteId) {
        return ApiException.chamar(() -> restClient.get()
                .uri("/api/praticantes/{id}/recordes", praticanteId)
                .retrieve()
                .body(RecordesPessoaisDTO.class));
    }
}

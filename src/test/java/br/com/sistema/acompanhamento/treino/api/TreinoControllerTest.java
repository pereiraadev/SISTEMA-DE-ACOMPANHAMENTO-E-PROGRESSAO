package br.com.sistema.acompanhamento.treino.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.sistema.acompanhamento.praticante.domain.Praticante;
import br.com.sistema.acompanhamento.praticante.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.api.ApiErrors;
import br.com.sistema.acompanhamento.treino.api.dto.HistoricoTreinoDTO;
import br.com.sistema.acompanhamento.treino.domain.CriterioOrdenacao;
import br.com.sistema.acompanhamento.treino.service.TreinoService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class TreinoControllerTest {
    @Mock
    private TreinoService service;
    @Mock
    private PraticanteRepository praticanteRepository;
    @Mock
    private Praticante praticante;

    private TreinoController controller;
    private final Authentication autenticacao = new TestingAuthenticationToken("gabriel@email.com", null);

    @BeforeEach
    void setUp() {
        controller = new TreinoController(service, praticanteRepository);
    }

    @Test
    void deveConsultarHistoricoDoPraticanteAutenticadoComOCriterioPedido() {
        when(praticanteRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.of(praticante));
        when(praticante.getId()).thenReturn(7L);
        List<HistoricoTreinoDTO> esperado = List.of();
        when(service.consultarHistoricoOrdenado(7L, CriterioOrdenacao.CARGA)).thenReturn(esperado);

        assertThat(controller.historico("carga", autenticacao)).isSameAs(esperado);
        verify(service).consultarHistoricoOrdenado(7L, CriterioOrdenacao.CARGA);
    }

    @Test
    void deveRecusarCriterioInvalidoComBadRequest() {
        when(praticanteRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.of(praticante));

        assertThatThrownBy(() -> controller.historico("peso", autenticacao))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("peso");
    }

    @Test
    void deveResponderBadRequestViaHttpParaCriterioInvalido() throws Exception {
        when(praticanteRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.of(praticante));
        var mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiErrors()).build();
        mvc.perform(get("/treinos/historico?ordem=peso").principal(autenticacao))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));
    }

    @Test
    void deveRecusarPraticanteInexistente() {
        when(praticanteRepository.findByEmailIgnoreCase("gabriel@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.historico("data", autenticacao))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Praticante nao encontrado");
    }
}

package br.com.sistema.acompanhamento.api;

import br.com.sistema.acompanhamento.praticante.domain.*;
import br.com.sistema.acompanhamento.praticante.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.praticante.service.PraticanteService;
import br.com.sistema.acompanhamento.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthHttpTest {
    @Test void validaCadastroEProtegeSenhaNaResposta() throws Exception {
        PraticanteRepository repo = mock(PraticanteRepository.class);
        var encoder = new BCryptPasswordEncoder();
        var controller = new AuthController(new PraticanteService(repo, encoder), repo, encoder,
                new TokenService("segredo-de-teste-com-pelo-menos-32-bytes", 3600000));
        var validator = new LocalValidatorFactoryBean(); validator.afterPropertiesSet();
        var mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiErrors())
                .setValidator(validator).build();
        mvc.perform(post("/auth/cadastrar").contentType("application/json")
                .content("{\"nome\":\"\",\"email\":\"invalido\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));
        when(repo.save(any())).thenAnswer(i -> {
            Praticante p = i.getArgument(0); ReflectionTestUtils.setField(p, "id", 1L); return p;
        });
        mvc.perform(post("/auth/cadastrar").contentType("application/json")
                .content("{\"nome\":\"Pessoa\",\"email\":\"pessoa@example.com\",\"senha\":\"senha-secreta\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("pessoa@example.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
        validator.close();
    }
}

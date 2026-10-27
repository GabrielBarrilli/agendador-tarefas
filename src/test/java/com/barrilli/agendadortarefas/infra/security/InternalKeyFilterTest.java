package com.barrilli.agendadortarefas.infra.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class InternalKeyFilterTest {

    private final InternalKeyFilter filter = new InternalKeyFilter();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "apiKey", "chave-de-teste");
    }

    // "%69" é "i": o Spring decodifica e roteia para /internal, então o filtro tem que pegar também
    @ParameterizedTest
    @ValueSource(strings = {"/internal/tarefas/pendentes", "/%69nternal/tarefas/pendentes", "/%69%6Eternal/tarefas/pendentes"})
    void rotaInternaSemChaveDa401(String uri) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("GET", uri), response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/internal/tarefas/pendentes", "/%69nternal/tarefas/pendentes"})
    void rotaInternaComChaveCertaPassa(String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.addHeader("X-Internal-Key", "chave-de-teste");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }
}

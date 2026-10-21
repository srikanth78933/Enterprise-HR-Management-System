package com.example.employee.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void passesPageRequestsThroughTheChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/employees");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void skipsHealthChecksAndStaticFiles() {
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/actuator/health"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/css/style.css"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/js/app.js"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/favicon.ico"))).isTrue();
    }

    @Test
    void logsApplicationPages() {
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/employees"))).isFalse();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/departments"))).isFalse();
    }
}

package com.cinq.vestaboard.presentation.controller;

import com.cinq.vestaboard.application.BoardService;
import com.cinq.vestaboard.configuration.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "app.security.allowed-domain=cinqict.nl",
        "app.security.allowed-origins=http://localhost:4200",
        "app.security.admin-emails=admin@cinqict.nl, daniel.eijkelenboom@cinqict.nl"
})
class BoardControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

    @Autowired
    private SecurityConfig securityConfig;

    private static final SimpleGrantedAuthority ROLE_VIEWER = new SimpleGrantedAuthority("ROLE_VIEWER");
    private static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    private static final String VALID_BODY = """
            {"type":"CELEBRATION","params":{"title":"Test"}}
            """;

    // --- GET /vestaboard ---

    @Test
    void get_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/vestaboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void get_withViewerRole_returns200() throws Exception {
        given(boardService.getCurrentMessage()).willReturn("current");

        mockMvc.perform(get("/vestaboard")
                .with(jwt().authorities(ROLE_VIEWER)))
                .andExpect(status().isOk());
    }

    // --- POST /vestaboard ---

    @Test
    void post_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/vestaboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void post_withViewerRole_returns403() throws Exception {
        mockMvc.perform(post("/vestaboard")
                .with(jwt().authorities(ROLE_VIEWER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void post_withAdminRole_returns200() throws Exception {
        given(boardService.setMessage(any(), anyBoolean())).willReturn("sent");

        mockMvc.perform(post("/vestaboard")
                .with(jwt().authorities(ROLE_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isOk());
    }

    // --- GET /vestaboard/template ---

    @Test
    void getTemplate_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/vestaboard/template").param("type", "CELEBRATION"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTemplate_withViewerRole_returns200() throws Exception {
        mockMvc.perform(get("/vestaboard/template")
                .with(jwt().authorities(ROLE_VIEWER))
                .param("type", "CELEBRATION"))
                .andExpect(status().isOk());
    }

    // --- POST /vestaboard/compose ---

    @Test
    void compose_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/vestaboard/compose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void compose_withViewerRole_returns403() throws Exception {
        mockMvc.perform(post("/vestaboard/compose")
                .with(jwt().authorities(ROLE_VIEWER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void compose_withAdminRole_returns200() throws Exception {
        given(boardService.compose(any())).willReturn(new int[6][22]);

        mockMvc.perform(post("/vestaboard/compose")
                .with(jwt().authorities(ROLE_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
                .andExpect(status().isOk());
    }

    // --- Role assignment (domainRestrictedConverter logic) ---

    @Test
    void domainUser_withoutAdminEmail_getsViewerRoleOnly() {
        JwtAuthenticationConverter converter = ReflectionTestUtils.invokeMethod(securityConfig, "domainRestrictedConverter");
        Jwt jwt = buildJwt("user@cinqict.nl", "cinqict.nl");

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .filteredOn(a -> a.startsWith("ROLE_"))
                .containsExactly("ROLE_VIEWER");
    }

    @Test
    void domainUser_withAdminEmail_getsBothRoles() {
        JwtAuthenticationConverter converter = ReflectionTestUtils.invokeMethod(securityConfig, "domainRestrictedConverter");
        Jwt jwt = buildJwt("admin@cinqict.nl", "cinqict.nl");

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .filteredOn(a -> a.startsWith("ROLE_"))
                .containsExactlyInAnyOrder("ROLE_VIEWER", "ROLE_ADMIN");
    }

    @Test
    void nonDomainUser_getsNoRoles() {
        JwtAuthenticationConverter converter = ReflectionTestUtils.invokeMethod(securityConfig, "domainRestrictedConverter");
        Jwt jwt = buildJwt("outsider@other.com", "other.com");

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .filteredOn(a -> a.startsWith("ROLE_"))
                .isEmpty();
    }

    @Test
    void missingHdClaim_getsNoRoles() {
        JwtAuthenticationConverter converter = ReflectionTestUtils.invokeMethod(securityConfig, "domainRestrictedConverter");
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("email", "user@cinqict.nl")
                .build();

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .filteredOn(a -> a.startsWith("ROLE_"))
                .isEmpty();
    }

    private Jwt buildJwt(String email, String hostedDomain) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("email", email)
                .claim("hd", hostedDomain)
                .build();
    }
}

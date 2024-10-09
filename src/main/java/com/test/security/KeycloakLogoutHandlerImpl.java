package com.test.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@Component
@AllArgsConstructor
public class KeycloakLogoutHandlerImpl implements LogoutHandler {

    private static final String END_SESSION_POSTFIX = "/protocol/openid-connect/logout";
    private final WebClient webClient;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        OidcUser loggedInUser = (OidcUser) authentication.getPrincipal();
        String logoutEndpoint = loggedInUser.getIssuer() + END_SESSION_POSTFIX;
        UriComponents builder = UriComponentsBuilder.fromUriString(logoutEndpoint).queryParam("id_token_hint", loggedInUser.getIdToken().getTokenValue()).build();
        ResponseEntity<String> logoutResponse = webClient.get()
                .uri(builder.toUri())
                .exchangeToMono(x -> x.toEntity(String.class)).block();
        if (!Objects.requireNonNull(logoutResponse).getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Unable to read from keycloack");
        }
    }
}

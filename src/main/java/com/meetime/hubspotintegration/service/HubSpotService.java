package com.meetime.hubspotintegration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.dto.AuthenticationResponseDto;
import com.meetime.hubspotintegration.dto.CreateContactRequestDto;
import com.meetime.hubspotintegration.dto.CreateContactResponseDto;
import com.meetime.hubspotintegration.error.BadRequestException;
import com.meetime.hubspotintegration.error.HubSpotServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class HubSpotService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String authorizationUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUrl;

    public HubSpotService(RestClient.Builder builder,
                          ObjectMapper objectMapper,
                          @Value("${hubspot.authorization-url}") String authorizationUrl,
                          @Value("${hubspot.client-id}") String clientId,
                          @Value("${hubspot.client-secret}") String clientSecret,
                          @Value("${hubspot.redirect_url}") String redirectUrl
    ) {
        this.restClient = builder.baseUrl("https://api.hubapi.com").build();
        this.objectMapper = objectMapper;
        this.authorizationUrl = authorizationUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
    }

    public String getAuthorizationUrl() {
        return this.authorizationUrl;
    }

    public AuthenticationResponseDto exchangeCodeForToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", this.clientId);
        body.add("client_secret", this.clientSecret);
        body.add("redirect_uri", this.redirectUrl);
        body.add("code", code);

        return restClient
                .post()
                .uri("/oauth/v1/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (httpRequest, clientHttpResponse) -> {
                            throw new BadRequestException(this.readErrorBody(clientHttpResponse));
                        }
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (httpRequest, clientHttpResponse) -> {
                            throw new HubSpotServerErrorException(this.readErrorBody(clientHttpResponse));
                        }
                )
                .body(AuthenticationResponseDto.class);
    }

    public CreateContactResponseDto createContact(CreateContactRequestDto createContactRequestDTO,
                                                  String authorizationParam) {
        String body;
        try {
            body = this.objectMapper.writeValueAsString(createContactRequestDTO);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Request body cannot be converted to JSON: " + e.getMessage());
        }

        return restClient
                .post()
                .uri("/crm/v3/objects/contacts")
                .header("Authorization", authorizationParam)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (httpRequest, clientHttpResponse) -> {
                            throw new BadRequestException(this.readErrorBody(clientHttpResponse));
                        }
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (httpRequest, clientHttpResponse) -> {
                            throw new HubSpotServerErrorException(this.readErrorBody(clientHttpResponse));
                        }
                )
                .body(CreateContactResponseDto.class);
    }

    private String readErrorBody(ClientHttpResponse clientHttpResponse) throws IOException {
        try (clientHttpResponse; InputStream responseBodyStream = clientHttpResponse.getBody()) {
            Charset charset = Optional
                    .ofNullable(clientHttpResponse.getHeaders().getContentType())
                    .map(MediaType::getCharset)
                    .orElse(StandardCharsets.UTF_8);
            return new String(responseBodyStream.readAllBytes(), charset);
        }
    }
}

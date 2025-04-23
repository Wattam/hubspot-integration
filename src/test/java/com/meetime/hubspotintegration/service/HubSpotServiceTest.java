package com.meetime.hubspotintegration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.dto.AuthenticationResponseDto;
import com.meetime.hubspotintegration.dto.CreateContactRequestDto;
import com.meetime.hubspotintegration.dto.CreateContactResponseDto;
import com.meetime.hubspotintegration.error.BadRequestException;
import com.meetime.hubspotintegration.error.HubSpotServerErrorException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.time.LocalDateTime;
import java.util.UUID;

@RestClientTest(HubSpotService.class)
class HubSpotServiceTest {

    @Autowired
    private HubSpotService hubSpotService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Value("${hubspot.authorization-url}")
    private String authorizationUrl;

    @Test
    void getAuthorizationUrl_shouldReturnConfiguredUrl() {
        Assertions.assertThat(this.hubSpotService.getAuthorizationUrl()).isEqualTo(this.authorizationUrl);
    }

    @Test
    void exchangeCodeForToken_shouldReturnAuthenticationResponseDto() throws JsonProcessingException {
        AuthenticationResponseDto responseDto =
                new AuthenticationResponseDto(UUID.randomUUID().toString(), 1800);
        String responseDtoAsString = this.objectMapper.writeValueAsString(responseDto);

        this.mockRestServiceServer.expect(MockRestRequestMatchers.anything())
                .andRespond(MockRestResponseCreators.withSuccess(responseDtoAsString, MediaType.APPLICATION_JSON));

        Assertions.assertThat(this.hubSpotService.exchangeCodeForToken(UUID.randomUUID().toString()))
                .isEqualTo(responseDto);
    }

    @Test
    void exchangeCodeForToken_shouldThrowBadRequestException() {
        this.mockRestServiceServer.expect(MockRestRequestMatchers.anything())
                .andRespond(MockRestResponseCreators.withBadRequest());

        Assertions.assertThatThrownBy(() -> this.hubSpotService.exchangeCodeForToken(UUID.randomUUID().toString()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void exchangeCodeForToken_shouldThrowHubSpotServerErrorException() {
        this.mockRestServiceServer.expect(MockRestRequestMatchers.anything())
                .andRespond(MockRestResponseCreators.withServerError());

        Assertions.assertThatThrownBy(() -> this.hubSpotService.exchangeCodeForToken(UUID.randomUUID().toString()))
                .isInstanceOf(HubSpotServerErrorException.class);
    }

    @Test
    void createContact_shouldReturnCreateContactResponseDto() throws JsonProcessingException {
        CreateContactResponseDto responseDto =
                new CreateContactResponseDto(UUID.randomUUID().toString(), LocalDateTime.now().toString());
        String responseDtoAsString = this.objectMapper.writeValueAsString(responseDto);

        this.mockRestServiceServer.expect(MockRestRequestMatchers.anything())
                .andRespond(MockRestResponseCreators.withSuccess(responseDtoAsString, MediaType.APPLICATION_JSON));

        Assertions.assertThat(this.hubSpotService.createContact(new CreateContactRequestDto(), "Bearer test_token"))
                .isEqualTo(responseDto);
    }

    @Test
    void createContact_shouldThrowBadRequestException() {
        this.mockRestServiceServer.expect(MockRestRequestMatchers.anything())
                .andRespond(MockRestResponseCreators.withBadRequest());

        Assertions.assertThatThrownBy(() -> this.hubSpotService.createContact(
                        new CreateContactRequestDto(),
                        "Bearer test_token"
                ))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createContact_shouldThrowHubSpotServerErrorException() {
        this.mockRestServiceServer.expect(MockRestRequestMatchers.anything())
                .andRespond(MockRestResponseCreators.withServerError());

        Assertions.assertThatThrownBy(() -> this.hubSpotService.createContact(
                        new CreateContactRequestDto(),
                        "Bearer test_token"
                ))
                .isInstanceOf(HubSpotServerErrorException.class);
    }
}

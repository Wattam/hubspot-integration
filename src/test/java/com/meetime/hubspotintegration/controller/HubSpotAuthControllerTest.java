package com.meetime.hubspotintegration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.hubspotintegration.dto.CreateContactRequestDto;
import com.meetime.hubspotintegration.dto.CreateContactResponseDto;
import com.meetime.hubspotintegration.dto.GenerateUrlResponseDto;
import com.meetime.hubspotintegration.dto.AuthenticationResponseDto;
import com.meetime.hubspotintegration.dto.ProcessContactRequestDto;
import com.meetime.hubspotintegration.service.HubSpotService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@WebMvcTest(HubSpotAuthController.class)
class HubSpotAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private HubSpotService hubSpotService;

    private String authorizationUrl;
    private String code;
    private AuthenticationResponseDto authenticationResponseDto;
    private CreateContactRequestDto createContactRequestDto;
    private CreateContactResponseDto createContactResponseDto;
    private ProcessContactRequestDto processContactRequestDto;
    private String authorizationHeader;

    @BeforeEach
    void setUp() {
        this.authorizationUrl = "https://example.com/auth";
        this.code = UUID.randomUUID().toString();
        this.authenticationResponseDto = new AuthenticationResponseDto("access_token", 3600);
        this.createContactRequestDto = new CreateContactRequestDto("test@example.com", "lastname", "firstname");
        this.createContactResponseDto =
                new CreateContactResponseDto(UUID.randomUUID().toString(), LocalDateTime.now().toString());
        this.processContactRequestDto = new ProcessContactRequestDto(1, 2, 3);
        this.authorizationHeader = "Bearer access_token";
    }

    @Test
    void getAuthorizationUrl_shouldReturnAuthorizationUrl() throws Exception {
        Mockito.when(this.hubSpotService.getAuthorizationUrl()).thenReturn(this.authorizationUrl);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/get-authorization-url"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .content()
                        .json(this.objectMapper.writeValueAsString(new GenerateUrlResponseDto(this.authorizationUrl)))
                );
    }

    @Test
    void getAccessTokens_shouldReturnTokens() throws Exception {
        Mockito.when(this.hubSpotService.exchangeCodeForToken(this.code)).thenReturn(this.authenticationResponseDto);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/hubspot-authentication/{code}", this.code))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .content()
                        .json(this.objectMapper.writeValueAsString(this.authenticationResponseDto))
                );
    }

    @Test
    void createContact_shouldReturnCreatedContact() throws Exception {
        Mockito.when(this.hubSpotService.createContact(
                Mockito.any(CreateContactRequestDto.class),
                ArgumentMatchers.eq(this.authorizationHeader)
        )).thenReturn(this.createContactResponseDto);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/create-contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(this.createContactRequestDto))
                        .header("Authorization", this.authorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/create-contact"))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .content()
                        .json(this.objectMapper.writeValueAsString(this.createContactResponseDto))
                );
    }

    @Test
    void processContact_shouldReturnProcessedContact() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/process-contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(this.processContactRequestDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .content()
                        .json(this.objectMapper.writeValueAsString(this.processContactRequestDto))
                );
    }

    @Test
    void rateLimitFallback_shouldReturnTooManyRequests() {
        RequestNotPermitted ex = RequestNotPermitted.createRequestNotPermitted(RateLimiter.ofDefaults("rateLimiter"));
        HubSpotAuthController hubSpotAuthController = new HubSpotAuthController(this.hubSpotService);
        ResponseEntity<Map<String, String>> responseEntity = hubSpotAuthController.rateLimitFallback(ex);

        Assertions.assertThat(responseEntity.getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        Assertions.assertThat(responseEntity.getBody())
                .isEqualTo(Map.of("message", "Rate limit was reach. Wait 10 seconds to try again."));
    }
}

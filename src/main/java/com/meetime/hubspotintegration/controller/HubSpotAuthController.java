package com.meetime.hubspotintegration.controller;

import com.meetime.hubspotintegration.dto.CreateContactRequestDto;
import com.meetime.hubspotintegration.dto.CreateContactResponseDto;
import com.meetime.hubspotintegration.dto.GenerateUrlResponseDto;
import com.meetime.hubspotintegration.dto.AuthenticationResponseDto;
import com.meetime.hubspotintegration.dto.ProcessContactRequestDto;
import com.meetime.hubspotintegration.service.HubSpotService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RateLimiter(name = "controllerRateLimiter", fallbackMethod = "rateLimitFallback")
public class HubSpotAuthController {

    private final HubSpotService hubSpotService;

    public HubSpotAuthController(HubSpotService hubSpotService) {
        this.hubSpotService = hubSpotService;
    }

    @GetMapping("/get-authorization-url")
    public ResponseEntity<GenerateUrlResponseDto> getAuthorizationUrl() {
        return ResponseEntity.ok(new GenerateUrlResponseDto(this.hubSpotService.getAuthorizationUrl()));
    }

    @GetMapping("/hubspot-authentication/{code}")
    public ResponseEntity<AuthenticationResponseDto> getAccessTokens(@PathVariable String code) {
        return ResponseEntity.ok(this.hubSpotService.exchangeCodeForToken(code));
    }

    @PostMapping("/create-contact")
    public ResponseEntity<CreateContactResponseDto> createContact(
            @RequestBody CreateContactRequestDto createContactRequestDto,
            @RequestHeader(value = "Authorization") String authorizationParam
    ) {
        CreateContactResponseDto createContactResponseDTO =
                this.hubSpotService.createContact(createContactRequestDto, authorizationParam);

        URI location = UriComponentsBuilder.fromPath("/create-contact").build().toUri();
        return ResponseEntity.created(location).body(createContactResponseDTO);
    }

    @PostMapping("/process-contact")
    public ResponseEntity<ProcessContactRequestDto> createContact(
            @RequestBody ProcessContactRequestDto processContactRequestDto
    ) {
        log.info(
                "New Contact created [id={}] [subscriptionId={}] [appId={}]",
                processContactRequestDto.getObjectId(),
                processContactRequestDto.getSubscriptionId(),
                processContactRequestDto.getAppId()
        );

        return ResponseEntity.ok(processContactRequestDto);
    }

    @SuppressWarnings("unused")
    public ResponseEntity<Map<String, String>> rateLimitFallback(RequestNotPermitted ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("message", "Rate limit was reach. Wait 10 seconds to try again."));
    }
}

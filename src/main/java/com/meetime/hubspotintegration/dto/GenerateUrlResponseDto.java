package com.meetime.hubspotintegration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateUrlResponseDto {

    private String authorizationUrl;
}

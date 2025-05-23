package com.meetime.hubspotintegration.error;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorDetails {

    private int status;
    private String message;
    private LocalDateTime timestamp;
}

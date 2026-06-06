package com.company.dto;

public record ErrorResponse(int status, String message, String timestamp) {
}

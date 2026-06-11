package org.com.salesagent.controller;

public record ChatResponse(
        String sessionId,
        String reply,
        long durationMs
) {}
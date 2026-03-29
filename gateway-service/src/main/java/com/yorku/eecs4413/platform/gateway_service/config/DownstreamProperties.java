package com.yorku.eecs4413.platform.gateway_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "downstream")
public record DownstreamProperties(
        Service iam,
        Service auction,
        Service catalogue,
        Service payment,
        Service leaderboard
) {
    public record Service(String baseUrl) {}
}

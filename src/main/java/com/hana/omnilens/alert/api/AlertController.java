package com.hana.omnilens.alert.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hana.omnilens.alert.application.AlertStreamingService;
import com.hana.omnilens.alert.domain.AlertEvent;
import com.hana.omnilens.common.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "News and disclosure intelligence event APIs")
public class AlertController {

    private final AlertStreamingService alertStreamingService;

    public AlertController(AlertStreamingService alertStreamingService) {
        this.alertStreamingService = alertStreamingService;
    }

    @PostMapping("/events")
    @Operation(summary = "Publish analyzed news or disclosure event to partner streams")
    public ApiResponse<AlertEvent> publish(@Valid @RequestBody AlertPublishRequest request) {
        return ApiResponse.success(alertStreamingService.publish(request));
    }
}

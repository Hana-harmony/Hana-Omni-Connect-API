package com.hana.omnilens.provider.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.hana.omnilens.config.HannahAiProperties;
import com.hana.omnilens.provider.ExternalProviderResiliencePolicy;

@Component
public class HannahAiAnalysisClient {

    private final RestClient restClient;
    private final ExternalProviderResiliencePolicy resiliencePolicy;

    public HannahAiAnalysisClient(
            RestClient.Builder restClientBuilder,
            HannahAiProperties properties,
            ExternalProviderResiliencePolicy resiliencePolicy) {
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl().toString())
                .build();
        this.resiliencePolicy = resiliencePolicy;
    }

    public HannahAiAnalysisResponse analyze(HannahAiAnalysisRequest request) {
        HannahAiApiResponse<HannahAiAnalysisResponse> response = resiliencePolicy.execute("hannah-ai-analysis", () -> restClient.post()
                .uri("/api/v1/alerts/analyze")
                .body(request)
                .retrieve()
                .body(HannahAiAnalysisEnvelope.TYPE));

        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalStateException("Hannah AI returned an empty analysis response");
        }
        return response.data();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record HannahAiApiResponse<T>(
            boolean success,
            String code,
            String message,
            T data
    ) {
    }

    private static final class HannahAiAnalysisEnvelope {
        private static final org.springframework.core.ParameterizedTypeReference<
                HannahAiApiResponse<HannahAiAnalysisResponse>> TYPE = new org.springframework.core.ParameterizedTypeReference<>() {
                };
    }
}

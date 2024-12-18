package com.github.jelmerbouma1985.stravadatadashboard.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/strava")
public class StravaController {


    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String tokenUrl;
    private final String authorizationUrl;

    private final RestTemplate restTemplate;

    public StravaController(
            @Value("${strava.client.id}") String clientId,
            @Value("${strava.client.secret}") String clientSecret,
            @Value("${strava.redirect.uri}") String redirectUri,
            @Value("${strava.token.url}") String tokenUrl,
            @Value("${strava.authorization.url}") String authorizationUrl,
            RestTemplate restTemplate) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.tokenUrl = tokenUrl;
        this.authorizationUrl = authorizationUrl;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/auth")
    public ResponseEntity<Void> authenticate() {
        String authUrl = authorizationUrl + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri
                + "&approval_prompt=auto"
                + "&scope=read,activity:read";

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", authUrl)
                .build();
    }

    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error) {

        if (error != null) {
            return new RedirectView("/strava/comparator");
        }

        if (code == null) {
            return new RedirectView("/strava/comparator");
        }

        try {
            String accessToken = exchangeCodeForToken(code);

            return new RedirectView("/strava/comparator?token=%s".formatted(accessToken));

        } catch (Exception e) {
            return new RedirectView("/strava/comparator");
        }
    }

    private String exchangeCodeForToken(String code) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("code", code);
        requestBody.put("grant_type", "authorization_code");

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, requestBody, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getString("access_token");
        } else {
            throw new RuntimeException("Failed to exchange code for token: " + response.getBody());
        }
    }
}

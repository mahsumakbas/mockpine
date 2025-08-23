package com.tigristest.mockpine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/mock")
public class MockController {

    private final Map<String, Map<String, Object>> endpointConfigs = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        // Load all mock files in project_root/mocks
        Path mockDir = Path.of(System.getProperty("user.dir"), "mocks");
        if (!Files.exists(mockDir)) {
            return;
        }

        try (Stream<Path> paths = Files.list(mockDir)) {
            paths.filter(path -> path.toString().endsWith(".txt"))
                    .forEach(path -> {
                        try {
                            Map<String, Object> config = MockConfigLoader.loadConfig(path);
                            String endpoint = ((String) config.getOrDefault("Endpoint", "")).trim();
                            if (!endpoint.startsWith("/")) {
                                endpoint = "/" + endpoint;
                            }
                            endpointConfigs.put(endpoint, config);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    @RequestMapping("/**")
    public ResponseEntity<String> handleMock(HttpServletRequest request) {
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        String endpointPath = requestPath.substring("/mock".length());

        Map<String, Object> config = endpointConfigs.get(endpointPath);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }

        int status = Integer.parseInt(config.getOrDefault("ReturnStatus", "200").toString());

        HttpHeaders headers = new HttpHeaders();
        Object headersObj = config.get("ReturnHeaders");
        if (headersObj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) headersObj;
            map.forEach((k, v) -> headers.add(k.toString(), v.toString()));
        }

        String body = config.getOrDefault("ReturnBody", "").toString();

        return new ResponseEntity<>(body, headers, HttpStatus.valueOf(status));
    }
}

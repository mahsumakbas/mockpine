package com.tigristest.mockpine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class SingleMockController {

    private static final String MOCK_FILE = "mocks/single_mock.txt";
    private Map<String, String> parsedMockFileContent = new HashMap<>();

    public SingleMockController() {
        loadMockFile();
        System.out.println("Mock file content is : \n" + parsedMockFileContent);
    }

    private void loadMockFile() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(MOCK_FILE), StandardCharsets.UTF_8);

            String currentSection = null;
            StringBuilder buffer = new StringBuilder();

            for (String line : lines) {
                if (line.startsWith("[") && line.endsWith("]")) {
                    // save previous section
                    if (currentSection != null) {
                        parsedMockFileContent.put(currentSection, buffer.toString().trim());
                        buffer.setLength(0);
                    }
                    currentSection = line.substring(1, line.length() - 1);
                } else {
                    buffer.append(line).append("\n");
                }
            }
            if (currentSection != null) {
                parsedMockFileContent.put(currentSection, buffer.toString().trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/single/**")
    public ResponseEntity<String> mock(HttpServletRequest request) {
        String requestPath = request.getRequestURI(); // e.g. /mock/my/service
        String endpoint = parsedMockFileContent.getOrDefault("Endpoint", "").trim();

        // Ensure endpoint starts with "/"
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        String expectedPath = "/single" + endpoint;
        // If endpoint doesn't match, return 404
        if (!requestPath.equals(expectedPath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No mock defined for: " + requestPath);
        }

        // Status
        int status = HttpStatus.OK.value();
        if (parsedMockFileContent.containsKey("ReturnStatus")) {
            status = Integer.parseInt(parsedMockFileContent.get("ReturnStatus").trim());
        }

        // Headers
        HttpHeaders headers = new HttpHeaders();
        if (parsedMockFileContent.containsKey("ReturnHeaders")) {
            String[] headerLines = parsedMockFileContent.get("ReturnHeaders").split("\n");
            for (String h : headerLines) {
                if (h.contains(":")) {
                    String[] kv = h.split(":", 2);
                    headers.add(kv[0].trim(), kv[1].trim());
                }
            }
        }

        // Body
        String body = parsedMockFileContent.getOrDefault("ReturnBody", "");

        return new ResponseEntity<>(body, headers, HttpStatus.valueOf(status));
    }
}

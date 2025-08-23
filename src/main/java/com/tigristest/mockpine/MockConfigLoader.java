package com.tigristest.mockpine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MockConfigLoader {

    /**
     * Reads a mock file and returns a map with keys:
     * Endpoint, ReturnStatus, ReturnHeaders, ReturnBody
     */
    public static Map<String, Object> loadConfig(Path path) throws IOException {
        Map<String, Object> config = new HashMap<>();
        String currentSection = null;
        StringBuilder bodyBuilder = new StringBuilder();
        Map<String, String> headersMap = new HashMap<>();

        for (String line : Files.readAllLines(path)) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).trim();
                continue;
            }

            if ("ReturnBody".equalsIgnoreCase(currentSection)) {
                bodyBuilder.append(line).append("\n");
            } else if ("ReturnHeaders".equalsIgnoreCase(currentSection)) {
                int sep = line.indexOf(":");
                if (sep != -1) {
                    String key = line.substring(0, sep).trim();
                    String value = line.substring(sep + 1).trim();
                    headersMap.put(key, value);
                }
            } else if (currentSection != null) {
                config.put(currentSection, line);
            }
        }

        if (!headersMap.isEmpty()) {
            config.put("ReturnHeaders", headersMap);
        }

        if (bodyBuilder.length() > 0) {
            config.put("ReturnBody", bodyBuilder.toString().trim());
        }

        return config;
    }
}

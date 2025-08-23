package com.tigristest.mockpine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

@RestController
public class DebuggerController {

    public boolean isProbablyText(byte[] bytes) {
        int len = Math.min(bytes.length, 1000); // only check first 1000 bytes
        int printable = 0;
        for (int i = 0; i < len; i++) {
            byte b = bytes[i];
            if ((b >= 32 && b <= 126) || b == 9 || b == 10 || b == 13) {
                printable++;
            }
        }
        double ratio = (double) printable / len;
        return ratio > 0.95; // if more than 95% printable, consider text
    }

    @RequestMapping(value = "/debug/**", consumes = "*/*", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomResponse> debugRequest(HttpServletRequest request) throws IOException, ServletException {

        CustomResponse myResponse = new CustomResponse();

        String clientIP = Optional.ofNullable(request.getHeader("X-Forwarded-For")).orElse(request.getRemoteAddr());
        String requestURL = request.getRequestURL().toString();
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        String urlQueryString = request.getQueryString();
        String requestFullUrl = urlQueryString != null ? requestURL + "?" + urlQueryString : requestURL;
        String requestContentType = request.getContentType() != null ? request.getContentType().toLowerCase() : null;
        Enumeration<String> headerNames;
        Map<String, String> requestHeaders = new HashMap<>();
        Map<String, String[]> requestParameters;
        List<String> parsedRequestParameters = new ArrayList<>();
        List<String> requestContentText = new ArrayList<>();
        //List<String> fileInfo = new ArrayList<>();
        List<Map<String, String>> multipartFileInfo = new ArrayList<>();
        List<String> rawBinaryFileInfo = new ArrayList<>();
        Map<String, String> extraResponseFields = new HashMap<>();

        String timestampnow = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS'Z'"));

        // --- All query + form parameters ---
        requestParameters = request.getParameterMap();
        if (requestParameters.isEmpty()) {
            parsedRequestParameters.add("No query/form parameters found");
        } else {
            requestParameters.forEach((key, values) -> {
                parsedRequestParameters.add("Param : " + key + " = " + Arrays.toString(values));
            });
        }

        // Headers
        headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            requestHeaders.put(headerName, request.getHeader(headerName));
        }

        // --- Uploaded Files from multipart---
        if (requestContentType != null && requestContentType.startsWith("multipart/")) {
            int fileNum = 1; // counter for files
            for (Part part : request.getParts()) {
                String submittedFileName = part.getSubmittedFileName();
                if (submittedFileName != null && !submittedFileName.isEmpty()) {
                    Map<String, String> fileMap = new HashMap<>();
                    fileMap.put("field name", part.getName());
                    fileMap.put("Original filename", part.getSubmittedFileName());
                    fileMap.put("Size", part.getSize() + " bytes");
                    fileMap.put("Content-Type", part.getContentType());
                    multipartFileInfo.add(fileMap);
                    fileNum++;
                }
            }
        } else if (requestContentType != null && !requestContentType.startsWith("multipart/")) {
            InputStream is = request.getInputStream();
            byte[] contentBytes = is.readAllBytes();
            if (requestContentType.startsWith("text/") || requestContentType.contains("json") || requestContentType.contains("xml") || requestContentType.contains("html")) {

                requestContentText.add("Text size: " + contentBytes.length + " bytes");
                String contentText = new String(contentBytes, StandardCharsets.UTF_8);
                requestContentText.add("Text Content:\n" + contentText);
            } else { //raw binary or unknown text type

                if (isProbablyText(contentBytes)) {
                    requestContentText.add("Text size: " + contentBytes.length + " bytes");
                    String contentText = new String(contentBytes, StandardCharsets.UTF_8);
                    requestContentText.add("Text Content:\n" + contentText);
                } else {

                    // Treat as binary file
                    rawBinaryFileInfo.add("Binary file size: " + contentBytes.length + " bytes");
                    rawBinaryFileInfo.add("Binary file content type: " + request.getContentType());
                }
            }
        }

        // Extra response fields
        extraResponseFields.put("Message: ", "Request received successfully");
        extraResponseFields.put("Date Time: ", timestampnow);

        // Set response class
        myResponse.setClientIP(clientIP);
        myResponse.setRequestURL(requestURL);
        myResponse.setRequestURI(requestURI);
        myResponse.setRequestMethod(requestMethod);
        myResponse.setUrlQueryString(urlQueryString);
        myResponse.setRequestFullUrl(requestFullUrl);
        myResponse.setRequestContentType(requestContentType);
        myResponse.setParsedRequestParameters(parsedRequestParameters);
        myResponse.setRequestHeaders(requestHeaders);
        //myResponse.setRequestBody(requestBody);
        myResponse.setResponseMessage(extraResponseFields);
        myResponse.setMultipartFileInfo(multipartFileInfo);
        myResponse.setRequestContentText(requestContentText);
        myResponse.setRawBinaryFileInfo(rawBinaryFileInfo);

        System.out.println("Client IP: " + clientIP);
        System.out.println("Request URL: " + requestURL);
        System.out.println("Request URI: " + requestURI);
        System.out.println("URL Query String: " + urlQueryString);
        System.out.println("Full Request URL: " + requestFullUrl);
        System.out.println("Content-Type: " + requestContentType);
        System.out.println("Request Parameters: " + parsedRequestParameters);
        System.out.println("Request Headers: " + requestHeaders);
        //System.out.println("Request Body: " + requestBody);
        System.out.println("Multipart File Info: " + multipartFileInfo);
        System.out.println("Raw Binary File Info: " + rawBinaryFileInfo);
        System.out.println("Request Content Text: " + requestContentText);
        System.out.println("Response Message: " + extraResponseFields);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(myResponse);

    }

}

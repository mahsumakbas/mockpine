package com.tigristest.mockpine;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomResponse
{



    String clientIP;

    String requestURL;

    String requestURI;

    String requestMethod;

    String urlQueryString;

    String requestFullUrl;

    String requestContentType;

    List<String> parsedRequestParameters;

    Map<String, String> requestHeaders;

    //Object requestBody;

    Object responseMessage;

    //MediaType responseContentType;

    List<String> requestContentText;

    List<Map<String, String>> multipartFileInfo;

    List<String> rawBinaryFileInfo;
    
    //List<String> jsonBodyInfo;

}
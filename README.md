# Mockpine
Mockpine is a HTTP debugger and mocking application. 

With Mockpine, you can easily define mock responses for HTTP services, making it a breeze to test and debug your applications without relying on real backend services.

## Features
- Define mock responses using simple text files.
- Support for different HTTP methods (GET, POST, etc.).
- Easy integration with Spring Boot applications.
- Customizable response status, headers, and body.

## How to debug HTTP Requests
1. Start application.
2. Make requests to the `/debug/` endpoints.
3. Check the logs for request and response details.

## How to Mock HTTP response
1. Create a mock file(any file with .txt extension) in the `mocks` directory.
2. Define the mock response using the following sections:
   - `[Endpoint]`: The request path to match.
   - `[ReturnStatus]`: The HTTP status code to return.
   - `[ReturnHeaders]`: Any headers to include in the response.
   - `[ReturnBody]`: The body of the response.
3. Start your application and make requests to the defined endpoints.
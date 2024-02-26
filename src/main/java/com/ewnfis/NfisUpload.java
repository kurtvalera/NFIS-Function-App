package com.ewnfis;

import com.bapcb.remote.BAPCBConnector;
import com.bapcb.remote.EventListenerInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import java.util.Base64;
import java.util.Base64.Decoder;

/**
 * Azure Functions with HTTP Trigger.
 */
public class NfisUpload {

    private static boolean isValidBase64(String str) {
        try {
            // Attempt to decode the Base64 string
            Base64.getDecoder().decode(str);
            return true; // If decoding succeeds, it's a valid Base64 value
        } catch (IllegalArgumentException e) {
            return false; // If decoding fails, it's not a valid Base64 value
        }
    }

   
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it
     * using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     * @throws Exception 
     */
    @FunctionName("NfisUpload")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET,
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {
        
        context.getLogger().info("NFIS Upload invoked");
        final String requestFinsurge = request.getBody().get();
        context.getLogger().info("Request: " + requestFinsurge);
        if (isValidBase64(requestFinsurge)) {
            byte[] requestInByte = Base64.getDecoder().decode(requestFinsurge);
        
            String requestInString = Base64.getEncoder().encodeToString(requestInByte);
            BAPCBConnector remote = BAPCBConnector.getInstance();
            String fileName = "ewnfis" + new Date(); 

            // Initialize connection
            String username = "ewb.api01";
            String password = "Credit@01";
            context.getLogger().info("Connection invoked at: " + new Date());
            boolean isConnected = remote.connect(username, password, "api7");
            if(isConnected) {
                context.getLogger().info("Connection successful.");
                if(requestFinsurge != null) {
                    context.getLogger().info("Valid Base 64. Proceed to NFIS Upload.");
                    context.getLogger().info("String value: " + requestInString);
                    remote.upload(null, fileName, requestInByte, "INDIVIDUAL");

                    // After file upload
                    remote.disconnect();
                    return request.createResponseBuilder(HttpStatus.OK).body("Request received.").build();  
                } else {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Incorrect request.").build();
                }
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to connect to NFIS Servers. Kinldy inform KOValera@eastwestbanker.com").build();
            }
        } else {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Incorrect request.").build();
        }
    }
}

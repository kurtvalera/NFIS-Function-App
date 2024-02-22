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
        
        context.getLogger().info("NFIS Upload");
        
        // final String query = request.getQueryParameters().get("method");
        // final String method = request.getBody().orElse(query);
        // context.getLogger().info("Method test: "+ method);
        
        
        // byte array to be converted to CSV
        final String requestFinsurge = request.getBody().get();
        context.getLogger().info(requestFinsurge);

        byte[] requestInByte = Base64.getDecoder().decode(requestFinsurge);

        // Encode the bytes in Base64
        String requestInString = Base64.getEncoder().encodeToString(requestInByte);


        
        BAPCBConnector remote = BAPCBConnector.getInstance();
        String fileName = "ewnfis" + new Date(); 


        if(requestFinsurge != null) {
            context.getLogger().info("Valid Base 64. Proceed to NFIS Upload.");
            context.getLogger().info("String value: " + requestInString);
            // remote.upload(null, fileName, requestInByte, "INDIVIDUAL");
            
            return request.createResponseBuilder(HttpStatus.OK).body("Request received.").build();
            
            
        } else {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Empty request.").build();
        }

        


        
        

    }
}

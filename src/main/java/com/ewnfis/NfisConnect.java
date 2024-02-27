package com.ewnfis;

import com.bapcb.remote.BAPCBConnector;
// import com.bapcb.remote.EventListenerInterface;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class NfisConnect {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it
     * using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     * @throws Exception 
     */
    @FunctionName("NfisConnect")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET,
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {
                context.getLogger().info("Java HTTP trigger processed a request. nfis connect");
        
        

        
       
        // User credentials 
        String username = "ewb.api01";
        String password = "Credit@01";

        int connectionTimeout = 28800000;
        int readTimeout = 28800000;

        BAPCBConnector remote = BAPCBConnector.getInstance(connectionTimeout, readTimeout);


        boolean isConnected = remote.connect(username, password,"api7");
       
        context.getLogger().info("isConnected? " + isConnected);
        if (isConnected) {
            return request.createResponseBuilder(HttpStatus.OK).body("Successfully connected to remote server").build();
        } else {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to remote server").build();
        }
    }
}

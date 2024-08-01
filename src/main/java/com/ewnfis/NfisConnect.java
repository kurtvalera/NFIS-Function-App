package com.ewnfis;

import com.bapcb.remote.BAPCBConnector;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.util.Optional;

public class NfisConnect {

    private static final String USERNAME = "ewb.api01";
    private static final String PASSWORD = "Credit@111";
    private static final int CONNECTION_TIMEOUT = 28800000;
    private static final int READ_TIMEOUT = 28800000;

    @FunctionName("NfisConnect")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processing request for NFIS connect.");

        try {
            BAPCBConnector remote = BAPCBConnector.getInstance(CONNECTION_TIMEOUT, READ_TIMEOUT);
            boolean isConnected = remote.connect(USERNAME, PASSWORD, "api7");
            context.getLogger().info("Connection status: " + (isConnected ? "Connected" : "Not connected"));
            
            if (isConnected) {
                return request.createResponseBuilder(HttpStatus.OK)
                        .body("Successfully connected to remote server")
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to connect to remote server")
                        .build();
            }
        } catch (Exception e) {
            context.getLogger().severe("An error occurred: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while connecting to remote server.")
                    .build();
        }
    }
}

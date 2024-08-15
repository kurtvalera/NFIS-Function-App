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

import org.json.JSONObject;

public class NfisConnect {

    private static final String USERNAME = "ewb.api01";
    private static final String PASSWORD = "Credit@333";
    private static final int CONNECTION_TIMEOUT = 28800000;
    private static final int READ_TIMEOUT = 28800000;

    @FunctionName("NfisConnect")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processing request for NFIS connect.");

        String requestBody = request.getBody().orElse("");

        // Check if the request body is empty and return a specific message if true
        if (requestBody.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("You cannot invoke Connect method. Please send an email to KOValera@eastwestbanker.com, VBMamuyac@eastwestbanker.com, BTCerdenajr@eastwestbanker.com, or RBLuzada@eastwestbanker.com. Thank you!")
                    .build();
        }

        context.getLogger().info("Request Body: " + requestBody);

        String requestor = new JSONObject(requestBody).getString("requestor");

        // Check if the requestor is "LOS" and return the specific message if true
        if ("LOS".equalsIgnoreCase(requestor)) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("You cannot invoke Connect method. Please send an email to KOValera@eastwestbanker.com, VBMamuyac@eastwestbanker.com, BTCerdenajr@eastwestbanker.com, or RBLuzada@eastwestbanker.com. Thank you!")
                    .build();
        }

        context.getLogger().info("Requestor: " + requestor);

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

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

public class NfisUpload {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * This function listens at endpoint "/api/NfisUpload".
     */
    @FunctionName("NfisUpload")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET,
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        String timestamp = currentDateTime.format(formatter);

        context.getLogger().info("Processing request for NFIS Upload.");

        if (!request.getBody().isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Request body is missing.").build();
        }

        String requestFinsurge = request.getBody().get();

        if (!isValidBase64(requestFinsurge)) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid Base64 encoding.").build();
        }

        byte[] requestInByte = Base64.getDecoder().decode(requestFinsurge);
        String fileName = "ewapinfis_" + timestamp + ".csv";
        BAPCBConnector remote = BAPCBConnector.getInstance();

        try {
            context.getLogger().info("Proceeding to NFIS Upload. File name: " + fileName);
            remote.upload(null, fileName, requestInByte, "INDIVIDUAL");
            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Request received. " + fileName + " has now been uploaded to BAP Servers.")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Failed to upload file: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file to BAP Servers.")
                    .build();
        }
    }

    private static boolean isValidBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

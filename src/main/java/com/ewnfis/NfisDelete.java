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

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NfisDelete {

    @FunctionName("NfisDelete")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {

        context.getLogger().info("Processing request for NFIS Download invoked.");

        BAPCBConnector remote = BAPCBConnector.getInstance();
        String referenceId = remote.generateReferenceID();
        Map<String, String> queryParams = request.getQueryParameters();
        String fileName = queryParams.get("fileName");
        context.getLogger().info(("File Name: " + fileName));

        try {
            String deleteFile = new String(remote.delete(null, fileName, "REPORT"));
            String errorCode = extractErrorCode(deleteFile);

            if(errorCode == "0") {
                return request.createResponseBuilder(HttpStatus.OK).body("Successful deletion.").build();
            } else {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid request.").build();
            }
            
        } catch (Exception e) {
            e.printStackTrace(); // Or log the error for debugging purposes
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("").build();
        }
    }

    public static String extractErrorCode(String xmlString) {
        Pattern pattern = Pattern.compile("ERROR_CODE=\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(xmlString);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null; // Or handle if error code not found
        }
    }
}
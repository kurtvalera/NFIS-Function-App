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

public class NfisDownload {

    @FunctionName("NfisDownload")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {

        context.getLogger().info("Processing request for NFIS Download invoked.");

        BAPCBConnector remote = BAPCBConnector.getInstance();
        String referenceId = remote.generateReferenceID();
     
        Map<String, String> queryParams = request.getQueryParameters();
       
        String transCode = queryParams.get("transCode");
        String fileName = queryParams.get("fileName");
        context.getLogger().info(("Trans Code: " + transCode));
        context.getLogger().info(("File Name: " + fileName));

        try {
            String csvContent = new String(remote.download(null, fileName, "REPORT", "CSV"));
            
            if (!csvContent.isEmpty()) {
                // remote.delete(null, fileName, "REPORT");
                JSONObject responseJson = new JSONObject();
                responseJson.put("csvContent", csvContent);
                responseJson.put("fileName", fileName);
                String formattedJson = responseJson.toString(4);
                return request.createResponseBuilder(HttpStatus.OK).body(formattedJson).build();
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("").build();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Or log the error for debugging purposes
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("").build();
        }
    }
}
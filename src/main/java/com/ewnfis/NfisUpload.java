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

import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class NfisUpload {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter formatterLog = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
    //                                             
    /**
     * This function listens at endpoint "/api/NfisUpload".
     */
    @FunctionName("NfisUpload")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        LocalDateTime currentDateTime = LocalDateTime.now();
        String timestamp = currentDateTime.format(formatter);

        Instant currentTime = Instant.now();
        String timeUtc = formatterLog.format(currentTime);

        context.getLogger().info("Processing request for NFIS Upload.");

        if (!request.getBody().isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Request body is missing.").build();
        }

        String requestFinsurge = request.getBody().get();
        context.getLogger().info("Request: " + requestFinsurge);
        if (!isValidBase64(requestFinsurge)) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid Base64 encoding.").build();
        }

        byte[] requestInByte = Base64.getDecoder().decode(requestFinsurge);
        context.getLogger().info("Request in byte: " + new String(requestInByte));
        String fileName = "ewapinfis_" + timestamp + ".csv";
        BAPCBConnector remote = BAPCBConnector.getInstance();

        try {
            
            context.getLogger().info("Proceeding to NFIS Upload. File name: " + fileName);
            String xmlResponse = remote.upload(null, fileName, requestInByte, "INDIVIDUAL");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));
            NodeList fileList = document.getElementsByTagName("FILE");
           
                Element fileElement = (Element) fileList.item(0);
                String resFileName = fileElement.getAttribute("NAME");
                String resTransCode = fileElement.getAttribute("TRANSCODE");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fileName", resFileName);
                // jsonObject.put("csvContent", csvContent);
                jsonObject.put("transCode", resTransCode);
               

               
                String formattedJson = jsonObject.toString(4);
        
            
            // return request.createResponseBuilder(HttpStatus.OK)
            //         .body("Request received. " + fileName + " has now been uploaded to BAP Servers.")
            //         .build();
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(formattedJson)
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Failed to upload file: " + e.getMessage());

            
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to connect to NFIS Servers. Kindly inform KOValera@eastwestbanker.com of the timestamp when the error occurred. Timestamp: " + timeUtc)
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

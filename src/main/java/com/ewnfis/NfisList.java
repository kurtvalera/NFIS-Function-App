package com.ewnfis;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bapcb.remote.BAPCBConnector;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
// import java.io.IOException;
import java.io.StringReader;
// import java.net.HttpURLConnection;
// import java.net.URL;
import java.util.Optional;

public class NfisList {

    /**
     * This function listens at endpoint "/api/NfisList".
     */
    @FunctionName("NfisList")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {

        context.getLogger().info("Processing request for NFIS List invoked.");
        BAPCBConnector remote = BAPCBConnector.getInstance();
        String referenceId = remote.generateReferenceID();
        String xmlRemoteList = remote.list(referenceId, "REPORT");
        byte[] downloadResult = null;
        boolean loopExecuted = false;
        context.getLogger().info("List: \n" + xmlRemoteList);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlRemoteList)));
        NodeList fileList = document.getElementsByTagName("FILE");


// Assuming fileList is NodeList
JSONArray jsonArray = new JSONArray();
for (int i = 0; i < fileList.getLength(); i++) {
    Element fileElement = (Element) fileList.item(i);
    String fileName = fileElement.getAttribute("NAME");
    String transCode = fileElement.getAttribute("TRANSCODE");
    if (!fileName.endsWith(".izp")) {
        downloadResult = remote.download(null, fileName, "REPORT", "CSV");
        String csvContent = new String(downloadResult);
        
        // Create JSON object for each file
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", fileName);
        jsonObject.put("csvContent", csvContent);
        jsonObject.put("transCode", transCode);
        // Add JSON object to array
        jsonArray.put(jsonObject);
        
        context.getLogger().info("File Name: " + fileName + System.lineSeparator() + "TransCode: " + transCode + System.lineSeparator() + "CSV Content" + csvContent);
        // remote.delete(null, fileName, "REPORT");
    } else {
        remote.delete(null, fileName, "REPORT");
    }
        loopExecuted = true;
        context.getLogger().info("Loop executed? " + loopExecuted);
    }

    if (loopExecuted) {
        JSONObject responseJson = new JSONObject();
        responseJson.put("files", jsonArray);
        String formattedJson = responseJson.toString(4); // 4 is the number of spaces for indentation
        return request.createResponseBuilder(HttpStatus.OK).body(formattedJson).build();
    } else {
        return request.createResponseBuilder(HttpStatus.NO_CONTENT).body("No files found.").build();
    }
    

    }


}

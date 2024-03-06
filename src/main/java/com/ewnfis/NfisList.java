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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;


import java.util.Base64;
import java.util.Base64.Decoder;

/**
 * Azure Functions with HTTP Trigger.
 */
public class NfisList {

   
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it
     * using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     * @throws Exception 
     */
    @FunctionName("NfisList")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET,
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {
        
                context.getLogger().info("NFIS List invoked");

                BAPCBConnector remote = BAPCBConnector.getInstance();
                String referenceId = remote.generateReferenceID();
            
                // "LIST Method return: " + remote.list(referenceId, "NEW") + "ReferenceID : " + referenceId
            
                String xmlRemoteList = remote.list(referenceId, "REPORT");
                Thread.sleep(60000); 
            
                byte[] downloadResult = null;
                boolean loopExecuted = false;
                context.getLogger().info(xmlRemoteList);
            
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(xmlRemoteList)));
         
            
                NodeList fileList = document.getElementsByTagName("FILE");
                for (int i = 0; i < fileList.getLength(); i++) {
                    Element fileElement = (Element) fileList.item(i);
                    String fileName = fileElement.getAttribute("NAME");
                    context.getLogger().info("File name: " + fileName);
                    if(!fileName.endsWith(".izp")) {
                        downloadResult = remote.download(null, fileName, "REPORT", "CSV");
                        String stringDownloadResult = new String(downloadResult);
                        context.getLogger().info(stringDownloadResult);
                        remote.delete(null, fileName, "REPORT");
                    } else if (fileName.endsWith(".izp")) {
                        remote.delete(null, fileName, "REPORT");
                    }
                    // Send json to finsurge
                    loopExecuted = true;
                    context.getLogger().info("Loop executed? " + loopExecuted);
                }
                if (loopExecuted) {
                    return request.createResponseBuilder(HttpStatus.OK).body("Download: " + downloadResult).build();
                } else {
                    return request.createResponseBuilder(HttpStatus.NO_CONTENT).body("No files found.").build();
                }
        
       

    }
}

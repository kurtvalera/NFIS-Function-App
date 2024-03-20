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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class NfisListCopy {

    /**
     * This function listens at endpoint "/api/NfisListCopy".
     */
    @FunctionName("NfisListCopy")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET,
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {

        context.getLogger().info("Processing request for NFIS List/Upload invoked.");
        BAPCBConnector remote = BAPCBConnector.getInstance();
        String referenceId = remote.generateReferenceID();
        String xmlRemoteList = remote.list(referenceId, "REPORT");
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
            if (!fileName.endsWith(".izp")) {
                downloadResult = remote.download(null, fileName, "REPORT", "CSV");
                String stringDownloadResult = new String(downloadResult);
                sendHttpPostRequest(stringDownloadResult);
                context.getLogger().info(stringDownloadResult);
                remote.delete(null, fileName, "REPORT");
            } else {
                remote.delete(null, fileName, "REPORT");
            }
            loopExecuted = true;
            context.getLogger().info("Loop executed? " + loopExecuted);
        }
        if (loopExecuted) {
            return request.createResponseBuilder(HttpStatus.OK).body("File sent to ").build();
        } else {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).body("No files found.").build();
        }
    }

    private void sendHttpPostRequest(String data) throws IOException {
        String urlString = "https://prod-04.eastasia.logic.azure.com:443/workflows/09f45c3771cb47f5ae256f317a4bbdf2/triggers/manual/paths/invoke?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=AYhyGmiMoX1mRqq3gTl4VjZpPaHwOLYQglpsw93YkC0";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setDoOutput(true);
        connection.getOutputStream().write(data.getBytes());
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        connection.disconnect();
    }
}

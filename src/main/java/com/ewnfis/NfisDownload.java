package com.ewnfis;

import com.bapcb.remote.BAPCBConnector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.util.Map;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;

public class NfisDownload {
   @FunctionName("NfisDownload")
   public HttpResponseMessage run(
       @HttpTrigger(name = "req", methods = {HttpMethod.POST},
           authLevel = AuthorizationLevel.ANONYMOUS)
       HttpRequestMessage<Optional<String>> request,
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
         String csvContent =
             new String(remote.download(null, fileName, "REPORT", "CSV"));

         if (!csvContent.isEmpty()) {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            ObjectNode objectNode = csvMapper.readerFor(ObjectNode.class)
                                        .with(schema)
                                        .readValue(csvContent);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString =
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                    objectNode);

            JSONObject jsonObj = new JSONObject(jsonString);

            // return
            // request.createResponseBuilder(HttpStatus.OK).body(jsonString).build();
            JSONObject responseJson = new JSONObject();

            responseJson.put("csvContent", jsonObj);
            responseJson.put("fileName", fileName);
            String formattedJson = responseJson.toString(4);
            return request.createResponseBuilder(HttpStatus.OK)
                .body(formattedJson)
                .build();
         } else {
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("")
                .build();
         }
      } catch (Exception e) {
         // context.getLogger().info(e);
         return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
             .body(e)
             .build();
      }
   }
}
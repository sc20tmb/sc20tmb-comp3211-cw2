package com.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import com.microsoft.azure.functions.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Task3Results {
    /**
     * This function listens at endpoint "/api/Task3Results". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/Task3Results
     * 2. curl {your host}/api/Task3Results?name=HTTP%20Query
     */
    @FunctionName("Task3Results")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Task3Results HTTP trigger processed a request.");

        try
        {
            // Retrieve table from DB
            Connection connection = DriverManager.getConnection(System.getenv("DB_CONNECTION_STRING"));

            PreparedStatement query = connection.prepareStatement("SELECT * FROM sensorDataStatistics;");

            ResultSet results = query.executeQuery();

            // Process the result set
            ObjectMapper objectMapper = new ObjectMapper();
            // Make the JSON output more readable
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            List<Map<String, Object>> resultList = new ArrayList<>();

            // Construct JSON response and return to the client
            while (results.next())
            {
                Map<String, Object> row = new HashMap<>();
                row.put("id", results.getObject("id"));
                row.put("minTemperature", results.getObject("minTemperature"));
                row.put("maxTemperature", results.getObject("maxTemperature"));
                row.put("avgTemperature", results.getObject("avgTemperature"));
                row.put("minWind", results.getObject("minWind"));
                row.put("maxWind", results.getObject("maxWind"));
                row.put("avgWind", results.getObject("avgWind"));
                row.put("minRHumidity", results.getObject("minRHumidity"));
                row.put("maxRHumidity", results.getObject("maxRHumidity"));
                row.put("avgRHumidity", results.getObject("avgRHumidity"));
                row.put("minCo2", results.getObject("minCo2"));
                row.put("maxCo2", results.getObject("maxCo2"));
                row.put("avgCo2", results.getObject("avgCo2"));
                resultList.add(row);
            }

            String response = objectMapper.writeValueAsString(resultList);

            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(response).build();
        }
        catch (SQLException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error connecting to database.").build();
        }
        catch (JsonProcessingException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Error processing query results.").build();
        }
    }
}

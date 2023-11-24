package com.function;

import java.sql.*;
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
public class Task2 {
    /**
     * This function listens at endpoint "/api/Task2". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/Task2
     */
    @FunctionName("Task2")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        final String connectionString = System.getenv("DB_CONNECTION_STRING");

        Connection connection;
        PreparedStatement query;

        try 
        {
            connection = DriverManager.getConnection(connectionString);

            // ASSUME TABLE ALREADY CREATED

            // Get min, max and average of each field for each sensor
            query = connection.prepareStatement("SELECT id, MIN(temperature) AS minTemperature, MAX(temperature) AS maxTemperature, AVG(temperature) AS avgTemperature, " + //
                                                            "MIN(wind) AS minWind, MAX(wind) AS maxWind, AVG(wind) AS avgWind," + //
                                                            "MIN(rHumidity) AS minRHumidity, MAX(rHumidity) AS maxrHumidity, AVG(rHumidity) AS avgRHumidity," + //
                                                            "MIN(co2) AS minCo2, MAX(co2) AS maxCo2, AVG(co2) AS avgCo2" + //
                                                             " FROM sensorData GROUP BY id");

            query.executeQuery();

            ResultSet queryResults = query.getResultSet();
            // Process the result set
            ObjectMapper objectMapper = new ObjectMapper();
            // Make the JSON output more readable
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            List<Map<String, Object>> resultList = new ArrayList<>();

            // Iterate through the query result and add each row to the JSON object
            while (queryResults.next())
            {
                Map<String, Object> row = new HashMap<>();
                row.put("id", queryResults.getObject("id"));
                row.put("minTemperature", queryResults.getObject("minTemperature"));
                row.put("maxTemperature", queryResults.getObject("maxTemperature"));
                row.put("avgTemperature", queryResults.getObject("avgTemperature"));
                row.put("minWind", queryResults.getObject("minWind"));
                row.put("maxWind", queryResults.getObject("maxWind"));
                row.put("avgWind", queryResults.getObject("avgWind"));
                row.put("minRHumidity", queryResults.getObject("minRHumidity"));
                row.put("maxRHumidity", queryResults.getObject("maxRHumidity"));
                row.put("avgRHumidity", queryResults.getObject("avgRHumidity"));
                row.put("minCo2", queryResults.getObject("minCo2"));
                row.put("maxCo2", queryResults.getObject("maxCo2"));
                row.put("avgCo2", queryResults.getObject("avgCo2"));
                resultList.add(row);
            }

            String response = objectMapper.writeValueAsString(resultList);

            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(response).build();

        }
        catch (SQLException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Encountered a SQL error.").build();
        }
        catch (JsonProcessingException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Encountered an error processing query results.").build();

        }
    }
}

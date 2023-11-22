package com.function;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import com.microsoft.azure.functions.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Task3Database {
    /**
     * This function listens at endpoint "/api/Task3Database". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/Task3Database
     */
    @FunctionName("Task3Database")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        final String body = request.getBody().get();
        ObjectMapper mapper = new ObjectMapper();
        // Jackson cannot deserialise Java Time types by default (timestamp)
        // Add extra module to be able to do so
        mapper.registerModule(new JavaTimeModule());
        ArrayList<SensorData> sensorData;

        // Desrialise the request body
        try
        {
            sensorData = mapper.readValue(body, new TypeReference<ArrayList<SensorData>>() {});
        }
        catch (Exception e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
        }

        // Write values to the Database
        Connection connection;
        PreparedStatement query;
        final String connectionString = System.getenv("DB_CONNECTION_STRING");

        try
        {
            connection = DriverManager.getConnection(connectionString);

            // Assume table already created
            query = connection.prepareStatement("INSERT INTO sensorData VALUES (?, ?, ?, ?, ?, ?)");

            // Write each sensor sent over the network into the database
            for (SensorData sensor : sensorData) {
                query.setString(1, String.valueOf(sensor.getId()));
                query.setString(2, String.valueOf(sensor.getTemperature()));
                query.setString(3, String.valueOf(sensor.getWind()));
                query.setString(4, String.valueOf(sensor.getRHumidity()));
                query.setString(5, String.valueOf(sensor.getCO2()));
                query.setString(6, String.valueOf(sensor.getTimestamp()));

                query.addBatch();
            }

            query.executeBatch();

            connection.close();
            query.close();
        }
        catch (SQLException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return request.createResponseBuilder(HttpStatus.OK).build();
    }
}

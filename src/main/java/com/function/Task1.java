package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;

import java.sql.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Task1 {
    public static class SensorData
    {
        private int id;
        private int temperature;
        private int wind;
        private int rHumidity;
        private int co2;
        private LocalDateTime timestamp;

        public SensorData(int sensorId, int sensorTemperature, int sensorWind, int sensorRHumidity, int sensorCo2)
        {
            this.id = sensorId;
            this.temperature = sensorTemperature;
            this.wind = sensorWind;
            this.rHumidity = sensorRHumidity;
            this.co2 = sensorCo2;
            this.timestamp = LocalDateTime.now();
        }

        public int getId()
        {
            return this.id;
        }
        public int getTemperature()
        {
            return this.temperature;
        }
        public int getWind()
        {
            return this.wind;
        }
        public int getRHumidity()
        {
            return this.rHumidity;
        }
        public int getCO2()
        {
            return this.co2;
        }
        public LocalDateTime getTimestamp()
        {
            return this.timestamp;
        }
    }

    protected static int GenerateRandomValue(Random seed, int start, int end)
    {
        return seed.nextInt(start, end+1);
    }

    protected static boolean CreateTable(Connection connection, ExecutionContext context)
    {
        try
        {
            // Drop the table if it already exists
            PreparedStatement query = connection.prepareStatement("IF OBJECT_ID('sensorData', 'U') IS NOT NULL DROP TABLE sensorData;" + //
                                                                  "CREATE TABLE sensorData (id int NOT NULL, temperature int NOT NULL, wind int NOT NULL, rHumidity int NOT NULL, co2 int NOT NULL, timestamp datetime NOT NULL)");
            query.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    /**
     * This function listens at endpoint "/api/Task1". Two ways to invoke it using "curl" command in bash:
     * 1. curl -X POST --data "HTTP Body" {your host}/api/Task1
     */
    @FunctionName("Task1")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Task 1 HTTP trigger processed a request.");

        long executionStartTime = System.currentTimeMillis();

        // Check a request body is present
        if (!request.getBody().isPresent())
        {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a number of sensors in the request body").build();
        }

        final String body = request.getBody().get();

        ObjectMapper mapper = new ObjectMapper();
        final String numIterationsString;
        final String simulateDelayedReadingsString;

        // Check the numSensors parameter exists
        try
        {
            JsonNode node = mapper.readTree(body);
            if (node.has("numIterations"))
            {
                numIterationsString = node.get("numIterations").asText();
            }
            else
            {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a number of sensors in the request body").build();
            }

            if (node.has("simulateDelayedReadings"))
            {
                simulateDelayedReadingsString = node.get("simulateDelayedReadings").asText();
            }
            else
            {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass whether you would like to simulate realistic readings in the request body").build();
            }
        }
        catch (Exception e)
        {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Bad Request.").build();
        }


        // Check the numIterations parameter is a number
        int numIterations = 0;
        try
        {
            numIterations = Integer.parseInt(numIterationsString);
        }
        catch (NumberFormatException e)
        {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a number of sensors in the request body").build();
        }

        // Get the value of simulateDelayedReadings
        boolean simulateDelayedReadings = false;

        simulateDelayedReadings = Boolean.parseBoolean(simulateDelayedReadingsString);

        Random seed = new Random();

        // Write the sensor data to the db
        String connectionString = System.getenv("DB_CONNECTION_STRING");
        Connection connection;
        PreparedStatement query;

        try
        {
            // Get connection to DB and create the table
            connection = DriverManager.getConnection(connectionString);

            if (!CreateTable(connection, context))
            {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while creating table.").build();
            }

            // For the number of specified iterations
            query = connection.prepareStatement("INSERT INTO sensorData VALUES (?, ?, ?, ?, ?, ?)");
            for (int iteration = 0; iteration < numIterations; iteration++)
            {
                // For each set of readings, generate 20 random readings
                // Insert each reading as a new row in the database
                for (int id = 1; id <= 20; id++)
                {
                    SensorData currentSensor = new SensorData(id, GenerateRandomValue(seed, 8, 15),
                                                GenerateRandomValue(seed, 15, 25),
                                                GenerateRandomValue(seed, 40, 70),
                                                GenerateRandomValue(seed, 500, 1500));

                    query.setString(1, String.valueOf(currentSensor.getId()));
                    query.setString(2, String.valueOf(currentSensor.getTemperature()));
                    query.setString(3, String.valueOf(currentSensor.getWind()));
                    query.setString(4, String.valueOf(currentSensor.getRHumidity()));
                    query.setString(5, String.valueOf(currentSensor.getCO2()));
                    query.setTimestamp(6, Timestamp.valueOf(currentSensor.getTimestamp()));
                    
                    query.addBatch();
                }
                // Pause for 2 seconds to simulate staggered readings
                if (simulateDelayedReadings)
                {
                    try
                    {
                        TimeUnit.SECONDS.sleep(2);
                    }
                    catch (InterruptedException e)
                    {
                        context.getLogger().log(Level.SEVERE, e.getMessage());
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Encountered an unexpected error when simulating delayed readings.").build();
                    }
                    
                }
                query.executeBatch();
                query.clearBatch();
            }  
            connection.close();
            query.close();
        }
        catch (SQLException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Encountered an SQL error during the database connection.").build();
        }

        long executionTotalTime = System.currentTimeMillis() - executionStartTime;
        return request.createResponseBuilder(HttpStatus.OK).body("Successfully Generated and Stored Sensor Data. The total execution time was: " + executionTotalTime).build();

    }
}

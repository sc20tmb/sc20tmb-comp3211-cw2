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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Task1 {
    public static class SensorData
    {
        int id;
        int temperature;
        int wind;
        int rHumidity;
        int co2;
        LocalDateTime timestamp;

        public SensorData(int sensorId, int sensorTemperature, int sensorWind, int sensorRHumidity, int sensorCo2)
        {
            this.id = sensorId;
            this.temperature = sensorTemperature;
            this.wind = sensorWind;
            this.rHumidity = sensorRHumidity;
            this.co2 = sensorCo2;
            this.timestamp = LocalDateTime.now();
        }
    }

    protected static int GenerateRandomValue(Random seed, int start, int end)
    {
        return seed.nextInt(start, end+1);
    }

    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
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

        // Check a request body is present
        if (!request.getBody().isPresent())
        {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a number of sensors in the request body").build();
        }
        final String body = request.getBody().get();

        ObjectMapper mapper = new ObjectMapper();
        final String numSensorsString;

        // Check the numSensors parameter exists
        try
        {
            JsonNode node = mapper.readTree(body);
            if (node.has("numSensors"))
            {
                numSensorsString = node.get("numSensors").asText();
            }
            else
            {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a number of sensors in the request body").build();
            }    
        }
        catch (Exception e)
        {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Bad Request.").build();
        }


        // Check the numSensors parameter is a number
        int numSensors = 0;
        try
        {
            numSensors = Integer.parseInt(numSensorsString);
        }
        catch (NumberFormatException e)
        {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a number of sensors in the request body").build();
        }

        ArrayList<SensorData> sensorData = new ArrayList<SensorData>();
        Random seed = new Random();

        for (int id = 1; id <= numSensors; id++)
        {
            sensorData.add(new SensorData(id, GenerateRandomValue(seed, 8, 15),
                                        GenerateRandomValue(seed, 15, 25),
                                        GenerateRandomValue(seed, 40, 70),
                                        GenerateRandomValue(seed, 500, 1500)));
        }

        return request.createResponseBuilder(HttpStatus.OK).body("Hello").build();

    }
}

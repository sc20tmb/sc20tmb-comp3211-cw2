package com.function;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import com.microsoft.azure.functions.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer trigger.
 */
public class Task3Generation {
    protected static int GenerateRandomValue(Random seed, int start, int end)
    {
        return seed.nextInt(start, end+1);
    }

    /**
     * This function will be invoked every 5 seconds, generating the sensor data.
     */
    @FunctionName("Task3Generation")
    public void run(
        @TimerTrigger(name = "timerInfo", schedule = "*/5 * * * * *") String timerInfo,
        final ExecutionContext context
    ) {
        
        ArrayList<SensorData> data = new ArrayList<>();
        Random seed = new Random();
        ObjectMapper mapper = new ObjectMapper();
        // Jackson cannot serialise Java Time types by default (timestamp)
        // Add extra module to be able to do so
        mapper.registerModule(new JavaTimeModule());
        String body;

        // Generate the data for 20 sensors
        for (int id = 1; id <= 20; id++)
        {
            SensorData currentSensor = new SensorData(id, GenerateRandomValue(seed, 8, 15),
                                        GenerateRandomValue(seed, 15, 25),
                                        GenerateRandomValue(seed, 40, 70),
                                        GenerateRandomValue(seed, 500, 1500));

            data.add(currentSensor);
        }

        // Serialise the data
        try
        {
            body = mapper.writeValueAsString(data);
        }
        catch (JsonProcessingException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return; // Do not attempt to send data
        }

        // Send data to Database function through HTTP
        try
        {
            URL url = new URL("https://comp3211-sc20tmb-cw1.azurewebsites.net/api/Task3Database");
            
            HttpURLConnection connection = ((HttpURLConnection)url.openConnection());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream out = connection.getOutputStream();
            out.write(body.getBytes());

            // Check response code
            switch(connection.getResponseCode())
            {
                case 200: // OK
                {
                    context.getLogger().log(Level.INFO, "Successfully generated and stored data.");
                } break;

                case 400: // BAD_REQUEST
                {
                    context.getLogger().log(Level.INFO, "There was a problem with the request.");
                } break;

                case 500: // INTERNAL_SERVER_ERROR
                {
                    context.getLogger().log(Level.INFO, "There was a problem connecting to the database.");
                } break;

                default:
                {
                    context.getLogger().log(Level.SEVERE, "Unexpected http code: " + connection.getResponseCode());
                }
            }

            connection.disconnect();
        }
        catch (Exception e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
        }

    }
}

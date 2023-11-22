package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.sql.annotation.SQLTrigger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

// This code is utilised from the Microsoft Learn Tutorial for the
// Azure SQL Trigger for Functions
// Available: https://learn.microsoft.com/en-us/azure/azure-functions/functions-bindings-azure-sql-trigger?tabs=isolated-process%2Cportal&pivots=programming-language-java
public class Task3Statistics {

    protected static boolean CreateStatisticsTable(Connection connection, ExecutionContext context)
    {
        try
        {
            // Drop the table if it already exists
            PreparedStatement query = connection.prepareStatement("IF OBJECT_ID('sensorDataStatistics', 'U') IS NOT NULL DROP TABLE sensorDataStatistics;" + //
                                                                  "CREATE TABLE sensorDataStatistics " + //
                                                                  "(id int not null, minTemperature int not null, maxTemperature int not null, avgTemperature int not null, " + //
                                                                  "minWind int not null, maxWind int not null, avgWind int not null, " + //
                                                                  "minRHumidity int not null, maxRHumidity int not null, avgRHumidity int not null, " + //
                                                                  "minCo2 int not null, maxCo2 int not null, avgCo2 int not null);");
            query.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    @FunctionName("Task3Statistics")
    public void run(
            @SQLTrigger(
                name = "sensorDataItems",
                tableName = "[dbo].[sensorData]",
                connectionStringSetting = "DB_CONNECTION_STRING_TRIGGER")
                SqlChangeSensorData[] sensorDataItems,
            ExecutionContext context) {
        
                final String connectionString = System.getenv("DB_CONNECTION_STRING");

                Connection connection;
                PreparedStatement query, insert;


                try 
                {
                    connection = DriverManager.getConnection(connectionString);

                    CreateStatisticsTable(connection, context);

                    query = connection.prepareStatement("SELECT id, MIN(temperature) AS minTemperature, MAX(temperature) AS maxTemperature, AVG(temperature) AS avgTemperature, " + //
                                                                    "MIN(wind) AS minWind, MAX(wind) AS maxWind, AVG(wind) AS avgWind," + //
                                                                    "MIN(rHumidity) AS minRHumidity, MAX(rHumidity) AS maxRHumidity, AVG(rHumidity) AS avgRHumidity," + //
                                                                    "MIN(co2) AS minCo2, MAX(co2) AS maxCo2, AVG(co2) AS avgCo2" + //
                                                                    " FROM sensorData GROUP BY id");

                    query.executeQuery();

                    ResultSet queryResults = query.getResultSet();
                    // Process the result set

                    insert = connection.prepareStatement("INSERT INTO sensorDataStatistics VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    // Iterate through the query result and add each row to the JSON object
                    while (queryResults.next())
                    {
                        insert.setInt(1, Integer.parseInt(queryResults.getObject("id").toString()));
                        insert.setInt(2, Integer.parseInt(queryResults.getObject("minTemperature").toString()));
                        insert.setInt(3, Integer.parseInt(queryResults.getObject("maxTemperature").toString()));
                        insert.setInt(4, Integer.parseInt(queryResults.getObject("avgTemperature").toString()));
                        insert.setInt(5, Integer.parseInt(queryResults.getObject("minWind").toString()));
                        insert.setInt(6, Integer.parseInt(queryResults.getObject("maxWind").toString()));
                        insert.setInt(7, Integer.parseInt(queryResults.getObject("avgWind").toString()));
                        insert.setInt(8, Integer.parseInt(queryResults.getObject("minRHumidity").toString()));
                        insert.setInt(9, Integer.parseInt(queryResults.getObject("maxRHumidity").toString()));
                        insert.setInt(10, Integer.parseInt(queryResults.getObject("avgRHumidity").toString()));
                        insert.setInt(11, Integer.parseInt(queryResults.getObject("minCo2").toString()));
                        insert.setInt(12, Integer.parseInt(queryResults.getObject("maxCo2").toString()));
                        insert.setInt(13, Integer.parseInt(queryResults.getObject("avgCo2").toString()));
                        
                        insert.addBatch();
                    }

                    insert.executeBatch();

                    connection.close();
                    query.close();
                    insert.close();
                }
                catch (SQLException e)
                {
                    context.getLogger().log(Level.SEVERE, e.getMessage());
                }
    }
}
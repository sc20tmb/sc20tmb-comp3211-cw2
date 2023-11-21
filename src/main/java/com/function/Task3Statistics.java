package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.sql.annotation.SQLTrigger;

import java.util.logging.Level;

// This code is utilised from the Microsoft Learn Tutorial for the
// Azure SQL Trigger for Functions
// Available: https://learn.microsoft.com/en-us/azure/azure-functions/functions-bindings-azure-sql-trigger?tabs=isolated-process%2Cportal&pivots=programming-language-java
public class Task3Statistics {
    @FunctionName("Task3Statistics")
    public void run(
            @SQLTrigger(
                name = "sensorDataItems",
                tableName = "[dbo].[sensorData]",
                connectionStringSetting = "DB_CONNECTION_STRING_TRIGGER")
                SqlChangeSensorData[] sensorDataItems,
            ExecutionContext context) {
        
        try
        {
            context.getLogger().log(Level.INFO, "WORKING");
        }
        catch (Exception e)
        {
            context.getLogger().log(Level.SEVERE, e.getMessage());
        }
        
    }
}
package com.function;

// This code is utilised from the Microsoft Learn Tutorial for the
// Azure SQL Trigger for Functions
// Available: https://learn.microsoft.com/en-us/azure/azure-functions/functions-bindings-azure-sql-trigger?tabs=isolated-process%2Cportal&pivots=programming-language-java
public class SqlChangeSensorData {
    public SensorData sensor;
    public SqlChangeOperation operation;

    public SqlChangeSensorData() {
    }

    public SqlChangeSensorData(SensorData sensor, SqlChangeOperation operation) {
        this.sensor = sensor;
        this.operation = operation;
    }
}

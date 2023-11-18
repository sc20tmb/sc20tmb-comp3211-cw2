package com.function;

import java.time.LocalDateTime;

public class SensorData {
    private int id;
    private int temperature;
    private int wind;
    private int rHumidity;
    private int co2;
    private LocalDateTime timestamp;

    public SensorData()
    {
        this.id = 0;
        this.temperature = 0;
        this.wind = 0;
        this.rHumidity = 0;
        this.co2 = 0;
        this.timestamp = LocalDateTime.MIN;
    }

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

    public void setId(int id)
    {
        this.id = id;
    }
    public void setTemperature(int temperature)
    {
        this.temperature = temperature;
    }
    public void setWind(int wind)
    {
        this.wind = wind;
    }
    public void setRHumidity(int rHumidity)
    {
        this.rHumidity = rHumidity;
    }
    public void setCo2(int co2)
    {
        this.co2 = co2;
    }
    public void setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
    }
}
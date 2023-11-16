package com.function;

import java.time.LocalDateTime;

public class SensorData {
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
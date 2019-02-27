#!/bin/bash
# Environmental Control System start-up script
echo -n -e "\033]0;ECS CONSOLE\007"
echo "Starting TemperatureController Primary"
java TemperatureController &
echo "Starting HumidityController Primary"
java HumidityController &
echo "Starting TemperatureSensor Primary"
java TemperatureSensor &
echo "Starting HumiditySensor Primary"
java HumiditySensor &

sleep 5

echo "Starting TemperatureController Standby"
java TemperatureController &
echo "Starting HumidityController Standby"
java HumidityController &
echo "Starting TemperatureSensor Standby"
java TemperatureSensor &
echo "Starting HumiditySensor Standby"
java HumiditySensor &

sleep 2

echo "Starting ECSConsole"
java ECSConsole
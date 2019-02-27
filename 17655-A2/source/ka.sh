#!/bin/bash

`ps -e | grep "HumidityController" | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
`ps -e | grep "HumiditySensor" | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
`ps -e | grep "TemperatureController" | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
`ps -e | grep "TemperatureSensor" | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
`ps -e | grep "ECSConsole" | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
`ps -e | grep "MessageManager" | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`

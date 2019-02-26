#!/bin/bash
ps -e | grep 'rmiregistry' | awk '{print $1}' | kill -9 $(cat)
rmiregistry &
sleep 10
java -classpath out/production/17655-A2/ MessageManager &
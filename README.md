System operations:

1. Use command "javac *.java" to compile the files.
2. Use command "./EMStart.sh" to start the MessageManager.
3. Use command "./ECStart.sh" to start the Sensors, Controllers and Console.
4. To kill the temperature sensor, use command "./k.sh -ts", and wait for about 10 seconds for the console to detect the sensor failure.
5. To kill the temperature controller, use command "./k.sh -tc", and wait for about 10 seconds for the console to detect the controller failure.
6. To kill the humidity sensor, use command "./k.sh -hs", and wait for about 10 seconds for the console to detect the sensor failure.
7. To kill the humidity controller, use command "./k.sh -hc", and wait for about 10 seconds for the console to detect the controller failure.
8. To kill the Message Manager, use command "./k.sh -mm", the system will detect a connection error immediately and restart the Message Manager. All the other component will reconnect to the new Message Manager afterwards, and keep working.
9. To kill all the processes, use command "./ka.sh".
10. If the command of "./k.sh" doesn't work, please use "ps" to check the running processes and use "kill PID" to kill corresponding processes.

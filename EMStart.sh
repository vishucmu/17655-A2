#!/bin/bash
# RMI registry and message manager start-up script

lsof -i:1099 | grep 'rmi' | awk '{print $2}' | kill -9 $(cat) 2>/dev/null
echo -n -e "\033]0;MESSAGE MANAGER\007"
echo "Starting RMI Registry and the Message Manager"
rmiregistry &
rmi=`lsof -i:1099 | grep 'rmiregist' | awk '{print $2}'`
while [[ -z $rmi ]]
do
    sleep 1
    rmi=`lsof -i:1099 | grep 'rmiregist' | awk '{print $2}'`
done
echo ""
java MessageManager &
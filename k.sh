
if [[ -z "$1" ]]; then
      echo ""
      echo "Usage:"
      echo "    ./k.sh -tc     to kill TemperatureController"
      echo "    ./k.sh -ts     to kill TemperatureSemsor"
      echo "    ./k.sh -hc     to kill HumidityController"
      echo "    ./k.sh -hs     to kill HumiditySensor"
      echo "    ./k.sh -ec     to kill ECSConsole"
      echo "    ./k.sh -mm     to kill MessageManager"
      echo ""
fi

if [ "$1" = "-tc" ]; then
    `ps -e | grep 'TemperatureController' | sort | head -1 | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
fi

if [ "$1" = "-ts" ]; then
    `ps -e | grep 'TemperatureSensor' | sort | head -1 | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
fi

if [ "$1" = "-hc" ]; then
    `ps -e | grep 'HumidityController' | sort | head -1 | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
fi

if [ "$1" = "-hs" ]; then
    `ps -e | grep 'HumiditySensor' | sort | head -1 | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
fi

if [ "$1" = "-ec" ]; then
    `ps -e | grep 'ECSConsole' | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
fi

if [ "$1" = "-mm" ]; then
    `ps -e | grep 'MessageManager' | awk '{print $1}' | kill -9 $(cat) 2>/dev/null`
fi


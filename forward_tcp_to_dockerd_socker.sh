pidsS=$(pgrep socat)
if [ -z "$pidsS" ]; then
    echo "Starting socat processes"
else
    echo "Running socat processes with PIDs: $pidsS"
    echo "Aborting"
    exit 1
fi

socat TCP-LISTEN:2375,reuseaddr,fork UNIX-CONNECT:/var/run/docker.sock &
pid=$!
sleep 3

address=localhost
port=2375

nc -vz $address $port &>/dev/null
if [ $? -eq 0 ]; then
    echo "Connected to ${address}:${port}"
    echo "socat PID: $pid"
else
    echo "Failed to connect"
fi

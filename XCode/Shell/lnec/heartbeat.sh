#!/bin/sh	
#
# $Id: heartbeat.sh 686 2014-10-08 16:40:37Z cattaneo $
#

 #export PACKAGE_HOME=/usr/local/lnec


spoolDir="/var/www/html/data"
lockDir="$spoolDir/locks"


lockFile=${lockDir}/$(hostname -i)

echo "file: $lockFile"

if [ $# != 1 ]; then
    echo "Wrong number of parameters, Usage:  $0  LogFolder" 
    exit -1;
else
    folder=$1
fi

trap '{ echo "killed ... removing lock file: $lockFile"; ssh root@192.41.218.59 "rm -f $lockFile" ; exit 0; }' SIGINT SIGTERM SIGKILL

while true
do
	date +%s | ssh root@192.41.218.59 "cat  > $lockFile"
	sleep 30
done




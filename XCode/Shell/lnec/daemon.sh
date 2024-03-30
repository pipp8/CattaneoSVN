#!/bin/sh	
#
# $Id: daemon.sh 685 2014-10-08 10:10:17Z cattaneo $
#

 #export PACKAGE_HOME=/usr/local/lnec



squidLog=/var/log/squid/access.log


if [ $# != 1 ]; then
    echo "Wrong number of parameters, Usage: " $0 " LogFolder" 
    exit -1;
else

    folder=$1
fi

if [ ! -d $folder ]; then 
    echo "FATAL error, log directory does not exist, terminating. (" $folder ")" 
    exit -1
fi

logFile=log/daemon.log
ffpid=log/firefox.pid
# change dir in the log directory.
cd $folder

# check if browser is running
if ps -p `cat $ffpid` > /dev/null
then 
	echo "OK Firefox is running"
else
	echo "Firefox is not running, terminating."
    exit 1
fi

ctrlDir=netCheck

mkdir $ctrlDir

echo 127.0.0.1 > $ctrlDir/checkedAddresses.txt

echo "Start IP Addresses monitor ..."

declare -a aVar

prevSite=""
# evitare di leggere il file ad ogni ciclo
ff=`cat $ffpid`
tail -n 0 -f $squidLog | while read -r -a aVar
do
    if [ "${aVar[8]}" != "$prevSite" ]
    then
        # a new line has been read
        echo -n "Got site: ${aVar[@]}" 
        prevSite=${aVar[8]}
        if egrep -i "$prevSite\$" $ctrlDir/checkedAddresses.txt > /dev/null
        then
            echo " already checked"
        else
            echo " new site, checking it."
            echo "${prevSite}" >> $ctrlDir/checkedAddresses.txt
            dstAddr=$(echo ${aVar[8]} | awk -F / '{ print $2 }')
            # asynchronously call the script to check the new connection
            nohup sh $PACKAGE_HOME/netCheck.sh $ctrlDir $dstAddr >> $logFile &
        fi
        # in this case no delay to speed up the log reading
    fi
done			




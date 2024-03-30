#!/bin/sh	
#
# $Id: netCheck.sh 402 2013-04-02 17:46:28Z cattaneo $
#


if [ $# != 2 ]; then
    echo "Wrong number of parameters, Usage: $0 LogFolder DstIPAddress"
    exit -1;
else
    folder=$1
    dstAddr=$2
    
    if [ ! -d $folder ]
    then 
        echo "FATAL control directory " $folder " does not exist !"
        exit -1
    else 
        cd $folder
    
        echo "$$ Checking address: " $dstAddr "..."

        nslookup $dstAddr 8.8.8.8
        
        traceroute $dstAddr >> traceroute.$dstAddr
        
        whois $dstAddr >> whois.$dstAddr
        
        echo $dstAddr >> checked.txt

        echo "$$ Done." 
    fi
fi


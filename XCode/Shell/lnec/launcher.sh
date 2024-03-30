#!/bin/sh
#
# $Id: launcher.sh 685 2014-10-08 10:10:17Z cattaneo $
#

 #export  PACKAGE_HOME=/usr/local/lnec

  



folder=$1

nic=$(netstat -i | fgrep eth | awk ' { print $1 } ')
netLog=$folder/netLog-${nic}.cap

screenLog=$folder/screenLog.ogv
logFolder=$folder/log
logFile=$folder/session.log
ffmpegLog=$logFolder/ffmpeg.log

if [ ! -d $folder ]
then 
	echo "FATAL directory " $folder " does not exist !"
	exit -1
else 
	echo "Started new session on: $(date)"
    	echo "Log file: " $logFile

	if [ ! -d $logFolder ]; then 
		mkdir $logFolder
	fi
	# set some font path (necessary for xmessage cf terminate.sh)
	xset +fp /etc/X11/fontpath.d/fonts-default

	echo -n "Starting network traffic monitoring ... "

	#tcpcomm="nohup tcpdump -x -X -v -e -s0 -i $nic -w $netLog tcp and #not port 5900 "
	
	tcpcomm="nohup tcpdump -x -X -v -e -s0 -i $nic -w $netLog tcp and not port 3128 "

	$tcpcomm &

	echo $! > $logFolder/tcpdump.pid
	echo "done, PID: " $!

	echo -n "Starting browser screen capture ... "

  # [ k {  M ] m } `
        size=`xdpyinfo | grep 'dimensions' | awk ' { print $2 } '`
  #	ffcomm="nohup ffmpeg -f x11grab -r 5 -s $size -i $DISPLAY -sameq -b 400k $screenLog"
  	ffcomm="nohup recordmydesktop --no-sound -fps 10 --on-the-fly-encoding -o  $screenLog"
	$ffcomm &> $ffmpegLog &

	echo $! > $logFolder/ffmpeg.pid
	echo "done, PID: " $!


    echo -n "Starting FireFox browser ... "
	w=$((`echo $size | awk -Fx ' { printf $1 } '` - 4))
	h=$((`echo $size | awk -Fx ' { printf $2 } '` - 8))

    firefox --width $w --height $h &



    echo $! > $logFolder/firefox.pid
    FireFoxPID=`cat $logFolder/firefox.pid`
#   FireFoxPID=`echo $!`
    echo "done, PID: " $!

	
	echo -n 'Starting heartbeat process ... '
	nohup sh $PACKAGE_HOME/heartbeat.sh $folder > $logFolder/heartbeat.log &
	echo $! > $logFolder/heartbeat.pid

	echo -n 'Starting net path monitor (running in backgroud) ... '
	nohup sh $PACKAGE_HOME/daemon.sh $folder > $logFolder/daemon.log &
	echo $! > $logFolder/daemon.pid

    echo "done, PID: " $!

    echo "Launcher is waiting for browser termination"

    wait $FireFoxPID
    while true; 
    do
        if ps -p $FireFoxPID > /dev/null
        then
#            echo "Firefox running"
            echo -n .
        else
            echo -
            echo "FireFox has been terminated by the user"  
            echo "Finalizing the current session"
            #we shuold here wait for the clean exit of all calls to checkNet.sh
            # synchronously call the finalize script and wait for its termination
            sh $PACKAGE_HOME/terminate.sh $folder >> $logFile
            echo "Launcher terminated without errors"
# pkill -KILL -u $user
            exit 0
        fi
        sleep 1
    done
fi



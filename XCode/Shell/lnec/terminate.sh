#!/bin/sh	
#
# $Id: terminate.sh 685 2014-10-08 10:10:17Z cattaneo $
#
#
# script per la terminazione dei processi e
# la costruzione del package firmato da scaricare
#

 #export  PACKAGE_HOME=/usr/local/lnec

TSA="http://timestamping.edelweb.fr/service/tsp"

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

logFolder=$folder/log
# change dir in the father of log directory.

echo "Starting script to build the data package and clean up $(pwd) ..."

xmessage -title 'End of the session' -timeout 8 -font '-urw-century schoolbook l-medium-r-normal--20-0-0-0-p-0-iso8859-1' -center -geometry 760x250 -file $PACKAGE_HOME/LogoutMessage.txt &

sleep 2

kill $(cat $logFolder/tcpdump.pid)
kill $(cat $logFolder/daemon.pid)

# occorre uccidere anche il processo tail lanciato da daemon
kill $(ps aux | fgrep 'tail -n 0' | awk '{ print $2 }')

SCPID=$(cat $logFolder/ffmpeg.pid)
# send a terminate message to the screencast program
kill -s TERM $SCPID

# now it starts saving the video so we wait until its termination ... this can take several minutes
    while true;
    do
        if ps -p $SCPID > /dev/null
        then
#            echo "still running"
            echo -n .
        else
            echo -
            echo "screencast has saved the file"  
	    break
        fi
        sleep 1
    done


if [ -d $folder ]
then
	echo "Building zip archive for" $folder
	zip -5 $folder.zip -r $folder #$folder.log 
fi

if [ -e "$folder.zip" ]
then
               
	#recupero la chiave pubblica RSA dell'utente
        #usercertpubkey=$(mysql -u proxydb -p lnec -D proxy -e "select pubkey from account where userid='$USER'") 
        #usercertpubkey=$(mysql -uuser -pnetforensic -h192.41.218.59 -Dproxy -e "select pubkey from account where userid='$USER'")
	mysql -uuser -pnetforensic -h192.41.218.59 --skip-column-names --batch --raw -Dproxy -e "select pubkey from accounts where userid='$USER'" > usercert.pem
	
	echo "Signing zip archive ..."
	
	# il prossimo comando produce un file con estensione .p7m -> $folder.zip.p7m
    	${PACKAGE_HOME}/p7signer sign $folder.zip $PACKAGE_HOME/lnec.crt $PACKAGE_HOME/lnec.key
	
	#produco il file .pkcs7
        #openssl smime -binary -sign -in $folder.zip -signer $PACKAGE_HOME/lnec.crt -inkey $PACKAGE_HOME/lnec.key -out $folder.pkcs7
       
	#TRGT=$folder.zip.p7m
        #TRGT=$folder.pkcs7
	#TRGT=$folder.zip

	echo "Calculate digest ..."
	# calcolo l'hash dell'intero archivio firma inclusa.
	openssl dgst -sha1 -out $folder.sha1 $folder.zip.p7m
    
	echo "Verify sign ..."
        #verifica firma p7signer
	#${PACKAGE_HOME}/p7signer verify $TRGT
	
	#verifica firma openssl
        #cat $folder.pkcs7 | ( echo -----BEGIN PKCS7-----; sed -e '1,/^Content-Disposition:/d;/^$/d'; echo -----END PKCS7-----) > $folder.signature.pem
	#openssl smime -verify -in $folder.signature.pem -inform pem -content $folder.zip –Cafile $PACKAGE_HOME/cacert.pem
	
	echo "Time Stamping ..."
	# calcolo della richiesta per la marca temporale
	export HASH=`awk ' { print $2 } ' $folder.sha1 `
	openssl asn1parse -genconf $PACKAGE_HOME/tsa.conf -out $folder.tsprequest
	
	# richiesta della marca temporale
	curl --data-binary @$folder.tsprequest --header "Content-Type: application/timestamp-request" -o $folder.tspresp $TSA
	TSR=$folder.tspresp
	
	#echo "Verify time stamping"
	#verifica timestamp con p7signer
	#${PACKAGE_HOME}/p7signer verifyTS $TRGT $TSR Clepsydre.cert
	#verifica timestamp con openssl
	#openssl ts -verify -data $TRGT -in $TSR -CAfile Clepsydre.cert 
#fi	
	# se l'utente ha uploadato un certificato con la sua chiave pubblica RSA
	CERT=usercert.pem
	DIMENSIONE=$(stat -c%s $CERT)
	#DIMENSIONE=$(stat -c%s usercert.pem)


	if [ $DIMENSIONE -gt 2 ] 
	then
		#echo "Encrypt!"
		#lo ridirigo in un file formato pem
		#echo $usercertpubkey > usercertpubkey.pem
		#cifro con la chiave pubblica dell'utente
		${PACKAGE_HOME}/p7signer encrypt $folder.zip.p7m usercert.pem        	
    		#rm usercert.pem	        
		#rm usercertpubkey.pem
		#metto in TRGT il package cifrato
		TRGT=$folder.zip.p7m.enc
	else    
	
		#echo "Nessun certificato caricato!"
		TRGT=$folder.zip.p7m
	       
	fi

fi

if [ -e "$TRGT" ] && [ -e "$folder.zip" ]

then
	echo "Deleting temporary files ..."
	rm $folder.zip
	# rm $folder.sign
	#rm $folder.log
    	#rm -r $folder*
	rm usercert.pem

	echo "Publishing output package: " $TRGT " ..."
	
	spoolDir="/var/www/html/doc/spool/"
	homeDir=${spoolDir}$USER
	lockDir=${spoolDir}"locks"
	 
	
	if [ ! -d  "$homeDir" ]
 	then
		mkdir $homeDir
	        chmod 776 $homeDir
		chgrp lnec $homeDir
    	fi
	randName="$(echo -n "$folder$RANDOM" | md5sum )"
	randName="$(echo $randName | cut -c1-32 )"
	#cp $PACKAGE_HOME/lnec.crt $PACKAGE_HOME/Clepsydre.crt $PACKAGE_HOME/cacert.pem
	zip -5 $randName.zip $TRGT $folder.tspresp $folder.tsprequest $folder.sha1 #lnec.crt Clepsydre.cert cacert.pem
	
	chmod 776 $randName.zip	
	chgrp apache $randName.zip
	 
	cp $randName.zip $homeDir/
	rm $randName.zip

	machine=$( hostname -i)
    	echo "$machine $USER $folder $randName.zip" >> $homeDir/lastlog.log
    	echo "${machine} $USER $folder $randName.zip" > $lockDir/${machine}.log
	# ferma il processo heartbeat per avviare il cleanup sul server
	kill $(cat $logFolder/heartbeat.pid)

	exit 0
fi

# in ogni caso ferma il processo heartbeat per avviare il cleanup sul server
kill $(cat $logFolder/heartbeat.pid)


exit -2

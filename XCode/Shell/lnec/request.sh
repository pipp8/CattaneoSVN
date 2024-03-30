#!/bin/sh
#
# $Id: request.sh 685 2014-10-08 10:10:17Z cattaneo $
#

if [ $# -lt 2 ]; then
	echo "Utente e/o password non inseriti"
	exit -1;
fi

user=$1
pass=$2
name=$3
tipo=$4

hostFree="none"

LOCKDIR="/var/www/html/doc/spool/locks"
scriptDir="/usr/local/lnec"
hostDir="${scriptDir}/HostStatus"
logFile="/var/www/html/logs/request.log"

echo "$(date) Starting Session for: user = $user - pass = $pass - name = $name" >> $logFile

#inserisce gli indirizzi delle macchine nella variabile list
cd $hostDir
list=$(ls)
cd $scriptDir

echo "List: $list" >> $logFile
for a in $list
do
	# prende lo stato dal file (0 oppure 1)
	state=$(head -1 ${hostDir}/${a})

	echo "Host $a: status = $state" >> $logFile
	if [ "$state" -eq "0" ]
	then
		# macchina libera esce dal ciclo
		hostFree=$a
		break
	fi
done

echo "Found host: $hostFree" >> $logFile
#stampa l'indirizzo (verrà visualizzato da php)
echo -n "$hostFree<br><br>"

if [ "$hostFree" ==  "192.41.218.61" ]
then
echo -n "Per accedere al servizio &egrave possibile utilizzare Connessione Desktop Remoto di Windows o scaricare (click tasto destro del mouse e selezionare salva link con nome) <a href="LNEC.rdp">rdp per Windows</a>. Per gli utenti Mac utilizzare <a href="http://cord.sourceforge.net/" target="_blank">Cord</a>  
<p>In entrambi i casi occorre fornire le stesse credenziali utilizzate per la registrazione al sito."
fi

#se hostFree è none allora non c'è nessuna macchina disponibile
if [ "$hostFree" !=  "none" ]
then
	#cambia lo stato della macchina da 0 a 1
	sed -i "1s/0/1/" ${hostDir}/${hostFree}
	
	#questa	riga viene inserita nel	file di	stato della macchina
	echo "$user $(date)" >>  "${hostDir}/${hostFree}";

	cl=$(echo -n $hostFree | tail -c 1)
	vmID='[Store1] NFClnt0/NFClnt0.vmx'
	vmID="$(echo -n $vmID | sed 's/t0/t'$cl'/g')"
	cmd1="vim-cmd vmsvc/power.getstate \"$vmID\"" 
	cmd2="vim-cmd vmsvc/power.on \"$vmID\"" 

	# controlla lo stato della macchina selezionata
	status=$(ssh root@172.16.16.130 "$cmd1")
	if echo $status | fgrep -i "powered on" > /dev/null ; then
		echo "VM $hostFree status: $status" >> $logFile
	else
		# accende la macchina virtuale
		ssh root@172.16.16.130 "$cmd2" >> $logFile
		sleep 40
	fi

	# viene creato il nuovo utente (non occorre verificarne l'esistenza)
    	echo "$(date) Creating user: $user on host: $hostFree" >> $logFile
#ssh root@${hostFree} "useradd --comment '$name' --gid lnec --groups apache,squid --skel /etc/skel/lnecUserHome --create-home --password $pass $user; (echo $pass; echo $pass) | passwd $user " >> $logFile

if [ "$tipo" ==  "proxy" ]
then
    	ssh root@${hostFree} "useradd --comment '$name' --gid lnec --groups apache,squid --password $pass $user; (echo $pass; echo $pass) | passwd $user " >> $logFile
fi
if [ "$tipo" ==  "kiosk" ]
then
    	ssh root@${hostFree} "useradd --comment '$name' --gid lnec --groups apache,squid --skel /etc/skel/lnecUserHome --create-home --password $pass $user; (echo $pass; echo $pass) | passwd $user " >> $logFile
fi
if [ "$tipo" ==  "expert" ]
then
    	ssh root@${hostFree} "useradd --comment '$name' --gid lnec --groups apache,squid --skel /etc/skel/lnecUserHome2 --create-home --password $pass $user; (echo $pass; echo $pass) | passwd $user " >> $logFile
fi


	if [ $? != 0 ]
	then
		echo "user account has not been created"
    		echo "$(date) Creation failed on host: $hostFree" >> $logFile
	else
	
		# avvia il cleaner
		nohup sh cleaner.sh $user ${hostFree} >> $logFile &
	fi

fi


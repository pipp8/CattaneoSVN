#!/bin/sh 
#
#
# $Id: cleaner.sh 686 2014-10-08 16:40:37Z cattaneo $
#

if [ $# != 2 ]; then
    echo "Wrong number of parameters, Usage:  $0  User Machine" 
    exit -1;
else
    user=$1
    machine=$2
fi

lockDir="/var/www/html/data/locks"
homeDir="/var/www/html/data/$user"
lockFile="$lockDir/$machine"
resultFile="${homeDir}/lastlog.log"
timeout=300

echo "$(date) Starting cleaner for case running on $machine"

#crea inizialmente il file ... se viene rimosso parte il cleanup
date +%s > $lockFile
chmod g+rw $lockFile
chgrp lnec $lockFile

while  true
do
	if [ -e $lockFile ]
	then
		msg=$(cat $lockFile)
		now=$(date +%s)
		diff=$(( $now - $msg ))
		echo $now $msg $diff
		if [ $diff -gt $timeout ]
		then
			echo "timeout cleanup"
			break
		fi
	else
	#il file e' stato rimosso dal client
		echo "finito ... clean up"
		break
	fi
	sleep 30
done

echo "$(date) Investigation Terminated, starting clean up"

if [ -e $resultFile ]
then
	result=$(tail -1 $resultFile)
	machine2=$( echo $result | cut -d ' ' -f 1 -)
	user2=$( echo $result | cut -d ' ' -f 2 -)
	folder=$( echo $result | cut -d ' ' -f 3 -)
	randName=$( echo $result | cut -d ' ' -f 4 -)

	if [ $user != $user2 ]
	then 
		echo warning "Condizione anomala: $user != $user2"

	elif [ $machine != $machine2 ]
	then 
		echo "Condizione anomala:  warning $machine != $machine2"

	else
		fine=$(date +%s)
		durata=$fine-$folder

		# aggiorna il log dell'utente
		echo "insert data into database"
		mysql -uroot -pnetforensic -Dproxy <<< "insert into files (dataFolder,nomeFile,account,durata) values ($folder,'$randName','$user',$durata);"

		# send mail
		email=$(mysql -uroot -pnetforensic -Dproxy --skip-column-names -e "select email from accounts where userid='$user'")

		echo "send mail to $email"
		nohup sh /usr/local/lnec/inviaMail.sh $email $randName $user

		rm -f $resultFile
		echo "$(date) The End"
	fi
else

	echo "Condizione anomala, file result non trovato. skipping"
fi

# viene rimosso l'utente i file sono stati già copiati in spool
echo "removing user: $user from host: $machine"
# ssh root@${machine} "userdel -f -r $user"

sleep 5
# spegne la macchina virtuale
cl=$(echo -n $machine | tail -c 1)
vmID='[Store1] NFClnt0/NFClnt0.vmx'
vmID="$(echo -n $vmID | sed 's/t0/t'$cl'/g')"
cmd1="vim-cmd vmsvc/power.getstate \"$vmID\""
cmd2="vim-cmd vmsvc/power.suspend \"$vmID\""

# controlla lo stato della macchina selezionata
status=$(ssh root@172.16.16.130 "$cmd1")
if echo $status | fgrep -i "powered on" > /dev/null ; then
	# spegne la macchina virtuale
        # ssh root@172.16.16.130 "$cmd2" 
        echo "VM $machine NON sospesa cf script cleaner.sh" 
	sleep 30
else
        echo "VM $machine status: $status" 
fi

# solo alla fine reset lo status della macchina
sed -i "1s/1/0/" /usr/local/lnec/HostStatus/${machine}

exit 0


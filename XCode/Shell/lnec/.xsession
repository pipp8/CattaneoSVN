# /bin/sh

export  PACKAGE_HOME=/usr/local/lnec

folder=$(date +%s)


if [ -d $folder ]
then 
	echo "FATAL La cartella " $folder " esiste !"
	exit -1
else 
	
	mkdir $folder 
fi

nohup sh $PACKAGE_HOME/launcher.sh $folder  > $folder/session.log


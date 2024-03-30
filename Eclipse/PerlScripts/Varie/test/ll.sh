#! /bin/sh

if (expr $# != 1) then
    echo "Errore nei parametri. Usage $0 len"
    exit 1
fi

LEN=$1

awk -F: "{ printf \"%${LEN}.${LEN}s\n\", \$1 }" selRS.ord  | uniq -d -c > selRS${LEN}.txt


NLINE=`wc -l selRS${LEN}.txt`

echo "Ci sono $NLINE ragioni sociali da controllare"
echo "con prefisso di $LEN caratteri uguali"


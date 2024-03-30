#/bin/sh

# uso 
# ./script.sh NomeFileTXT

wget --no-check-certificate -P images -i $1 -p -nd -t5 -H -A.jpg,.jpeg,.jpg.1,.jpg.2,.jpeg.1,.jpeg.2 -erobots=off 

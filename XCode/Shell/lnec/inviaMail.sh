#!/bin/bash
#
# $Id: inviaMail.sh 686 2014-10-08 16:40:37Z cattaneo $
#

tmp=./temp.mail
TO=$1
FROM="lnec@netforensic.dia.unisa.it"
SUBJECT="Download evidenze"
MIMEVersion="1.0"
CONTENTType="text/html; charset=us-ascii"
BODY="
<p>Per scaricare il pacchetto contenente le evidenze raccolte cliccare sul link sequente: 
<a href=\"http://netforensic.dia.unisa.it/data/$3/$2\">netforensic</a>
</p>"

echo -e "to: <$TO>" > $tmp
echo -e "from: LNEC <$FROM>" >> $tmp
echo -e "Content-Type: $CONTENTType" >> $tmp
echo -e "MIME-Version: $MIMEVersion" >> $tmp
echo -e "subject: $SUBJECT" >>$tmp
echo -e "$BODY" >> $tmp

sendmail -t  < $tmp
#rm -rf $tmp


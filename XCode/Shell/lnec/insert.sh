#!/bin/bash
#
# $Id: insert.sh 397 2013-03-16 22:36:09Z cattaneo $
#

mysql -uroot -pnetforensic -Dproxy <<< 'update account set active=1 where id=4;' 


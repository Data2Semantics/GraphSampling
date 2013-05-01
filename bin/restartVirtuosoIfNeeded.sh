#!/bin/bash

source /home/OpenPHACTS-Virtuoso/virtuoso-environment.sh;

processes=`ps aux | grep [v]irtuoso | wc -l`
if [ "$processes" = "0" ]; then
	rm /home/OpenPHACTS-Virtuoso/database/virtuoso.lck;
	virtuoso-start.sh;
else
	echo "virtuoso already running"
fi

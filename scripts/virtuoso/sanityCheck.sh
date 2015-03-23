#!/bin/bash

source /home/OpenPHACTS-Virtuoso/virtuoso-environment.sh;
isqlFile="${HOME}/.isqlCmdFile.sql"

if [ -z $1 ]; then
	echo "add dir as argument"
	exit;
fi
for dir in "$@"; do
	numLines=`head $dir/* | wc -l`;
	if [ "$numLines" -le "2" ]; then
		echo "$dir has under 2 lines"
	fi
	exit
done
echo "done"


#!/bin/bash

if [ -z "$1" ];then
        echo "at least 1 argument required (directory path to check)"
        exit;
fi
instances=`listInstances.sh`
for dir in "$@"; do 
	if [ -d $dir ]; then
		absDir=$(readlink -f $dir)
		basename=`basename $dir`
		if [[ "$instances" == *"$absDir"* ]]; then
			echo "already imported: $basename";
		else
			echo "NOT imported: $basename";
		fi
	fi
done

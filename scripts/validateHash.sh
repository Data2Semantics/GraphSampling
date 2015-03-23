#!/bin/bash
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset). We'll check just 1 rewrite method (the most verbose one) for hashes"
	exit;
fi
dataset=$1



pig $PIG_SCRIPTS/utils/validateHash.py $dataset;

echo "Counts:";
hadoop fs -cat tmp/**/**

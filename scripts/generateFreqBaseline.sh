#!/bin/bash
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;
#rewriteMethods=(spo-spo.py)
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset)"
	exit;
fi
dataset=$1

echo "generating frequency baseline"
pig $PIG_SCRIPTS/stats/freqBaseline.py $dataset



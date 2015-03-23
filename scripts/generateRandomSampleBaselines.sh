#!/bin/bash
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;
#rewriteMethods=(spo-spo.py)
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset)"
	exit;
fi
dataset=$1

iterations=10
 
for (( it=1; it<=$iterations; it++ ))
do
	echo "generating random sample for iteration $it"
	pig $PIG_SCRIPTS/stats/randomSampleBaseline.py $dataset $it
done
 

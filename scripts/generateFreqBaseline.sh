#!/bin/bash

#rewriteMethods=(spo-spo.py)
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset)"
	exit;
fi
dataset=$1

echo "generating frequency baseline"
pig pigAnalysis/stats/freqBaseline.py $dataset



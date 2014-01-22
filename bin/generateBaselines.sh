#!/bin/bash

#rewriteMethods=(spo-spo.py)
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset)"
	exit;
fi
dataset=$1

#we don't generate these anymore. We generate it on-the-go when fetching weights for query triples
#generateRandomSampleBaselines.sh $dataset;
generateFreqBaseline.sh $dataset;

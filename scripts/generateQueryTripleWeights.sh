#!/bin/bash
function hadoopLs {
	hadoopLs=()
	echo "hadoop fs -ls $1";
	dirListing=`hadoop fs -ls $1`;
	for word in ${dirListing} ; do
 		if [[ $word =~ ^/ ]];then 
	    	hadoopLs+=(${word})
	    fi
	done
}
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;
if [ -z "$1" ];then
        echo "at least 1 argument required (dataset). Optional second arg is pattern used in hadoop ls"
        exit;
fi
dataset=$1
pattern="*"
if [ -n "$2" ]; then
	pattern="$2"
fi


hadoopLs "$dataset/roundtrip/";
for dir in "${hadoopLs[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
		continue
	fi
	if [[ ! "$dir" == *_long ]]; then
		pig $PIG_SCRIPTS/evaluation/fetchTripleWeights.py $dir;
	fi
done;


echo "fetching query triples weights for random samples"
iterations=10
for (( it=1; it<=$iterations; it++ ))
do
	echo "generating random sample for iteration $it"
	pig $PIG_SCRIPTS/evaluation/fetchTripleWeightsRandom.py $dataset $it
done

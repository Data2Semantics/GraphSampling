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
roundtripSamples="${hadoopLs[@]}"
hadoopLs "$dataset/baselines/";
baselineSamples="${hadoopLs[@]}"
allSamples=(`echo ${roundtripSamples[@]}` `echo ${baselineSamples[@]}`)

for dir in "${allSamples[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
		continue
	fi
	if [[ ! "$dir" == *_long ]]; then
		if [[ ! "$dir" == *_dict ]]; then
                	#hmm, we want to skip longs! 
			#dirBasename=`basename $dir`
			pig pigAnalysis/evaluation/fetchWeightDistribution.py $dir;
		fi
	fi
done;

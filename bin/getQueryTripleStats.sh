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
        echo "at least 1 argument required (rewrite methods as directories to get query triples stats for)"
        exit;
fi


for dir in "$@"; do
	dirBasename=`basename $dir`
	IFS=_ read -a delimited <<< "$dirBasename"
	dataset=${delimited[0]}
	hadoopLs $dataset/queryStatsInput/
	if [ ${#hadoopLs[@]} == "0" ]; then
		echo "Did not find query files to get stats for. ($dataset/queryStatsInput/)"
		exit;
	fi
	echo "running pig to get weights for query triples $dirBasename"
	for queryFile in "${hadoopLs[@]}"; do
		pig pigAnalysis/stats/getQueryTripleWeights.py $dataset/roundtrip/$dirBasename $queryFile;
	done
done

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
        echo "at least 1 argument required (dataset to cat query stats for)"
        exit;
fi

dataset=$1

echo "catting all results locally"
hadoopLs $dataset/queryStats/
if [ ${#hadoopLs[@]} == "0" ]; then
	echo "Did not find any files to cat locally. something wrong with pig?"
	exit;
fi
resultsDir="$HOME/stats/queryTripleWeights/$dataset/"
if [ ! -d $resultsDir ]; then
	echo "$resultsDir does not exist locally. creating it"
	mkdir $resultsDir;
fi
for queryFile in "${hadoopLs[@]}"; do
	queryFileBasename=`basename $queryFile`
	hadoop fs -cat $queryFile/part* > $resultsDir/$queryFileBasename
done

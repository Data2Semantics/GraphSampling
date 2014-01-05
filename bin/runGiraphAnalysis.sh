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
function hadoopPagerankLs {
	hadoopPagerankLs=()
	echo "hadoop fs -ls $1";
	dirListing=`hadoop fs -ls $1`;
	for word in ${dirListing} ; do
 		if [[ $word =~ ^/ ]];then 
	    	hadoopPagerankLs+=(${word})
	    fi
	done
}
if [ -z "$1" ];then
        echo "at least 1 argument required (dataset). Optional second arg is pattern used in hadoop ls"
        exit;
fi
dataset=$1
pattern="*_long"
if [ -n "$2" ]; then
	pattern="$2"
fi

analysisMethods=(runGiraphPagerank.sh)
#analysisMethods=(pageRank.py)

hadoopLs "$dataset/rewrite";
for dir in "${hadoopLs[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
		continue
	fi
	
	echo "running analysis for $dir"
	for analysisMethod in "${analysisMethods[@]}"; do
		echo "running $analysisMethod"
		$analysisMethod $dir;
	done
done;

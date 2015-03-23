#!/bin/bash
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;
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

#analysisMethods=(runGiraphPagerank.sh)
#analysisMethods=(pageRank.py)

hadoopLs "$dataset/analysis/";
for dir in "${hadoopLs[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
                continue
        fi
        #add our own custom pattern here as well. only want to perform this on longs
        if [[ ! "$dir" == *_long ]]; then
                continue
        fi	
	echo "running giraph analysis to string for $dir"
	pig $PIG_SCRIPTS/utils/giraphAnalysisToString.py $dir;
done;

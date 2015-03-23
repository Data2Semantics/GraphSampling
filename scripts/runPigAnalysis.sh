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


hadoopLs "$dataset/rewrite/";
for dir in "${hadoopLs[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
		continue
	fi
	if [[ ! "$dir" == *_long ]]; then
		if [[ ! "$dir" == *_dict ]]; then
			#hmm, we want to skip the 'long' stuff for giraph
			pig $PIG_SCRIPTS/analysis/indegree.py $dir;
			pig $PIG_SCRIPTS/analysis/outdegree.py $dir;
		fi
	fi
done;

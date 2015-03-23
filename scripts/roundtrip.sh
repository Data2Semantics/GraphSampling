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


hadoopLs "$dataset/analysis/";
for dir in "${hadoopLs[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
		continue
	fi
	if [[ ! "$dir" == *_long ]]; then
                #hmm, we want to skip longs! we only use the long stuff for giraph
		dirBasename=`basename $dir`
		IFS=_ read -a delimited <<< "$dirBasename"
		rewriteMethod=${delimited[0]}
		unset IFS
		pig $PIG_SCRIPTS/roundtrip/$rewriteMethod.py $dir;
	fi
done;

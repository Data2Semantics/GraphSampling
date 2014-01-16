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
weightDistDir="weightDistribution";
mkdir -p $weightDistDir;
targetDir="$weightDistDir/$dataset"
echo "Storing to $targetDir"
rm -rf $targetDir;
mkdir $targetDir;


hadoopLs "$dataset/evaluation/weightDistribution";
for dir in "${hadoopLs[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
		continue
	fi
	dirBasename=`basename $dir`
	hadoop fs -cat $dir/* >> ${targetDir}/$dirBasename;
done;

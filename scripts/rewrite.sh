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



rewriteMethods=(resourceContext.py resourceSimple.py resourceUnique.py resourceWithoutLit.py path.py)
if [ -z "$1" ];then
	echo "at least 1 argument required (input). (second arg enables catting locally)"
	exit;
fi
dataset=$1
enableCat=$2

#rewrite stuff
for rewriteMethod in "${rewriteMethods[@]}"; do
pig pigAnalysis/rewrite/${rewriteMethod} $dataset/$dataset.nt;
done

#get all rewritten stuff locally
if [[ $enableCat ]]; then
	hadoopLs "$dataset/rewrite";
	for dir in "${hadoopLs[@]}"; do
		basename=`basename "$dir"`;
        targetDir="${HOME}/rProject/$basename"
        if [ ! -d "$targetDir" ]; then
                echo "dir $targetDir does not exist. initializing"
                initRDir.sh $targetDir;
        fi
        targetFile="$targetDir/input"
        echo "Catting for rewrite method $basename";
        hadoop fs -cat $dir/part* > $targetFile;
	done
fi





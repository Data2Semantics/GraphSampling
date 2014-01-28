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
qTripleDir="/var/scratch/lrd900/qTripleWeights";
mkdir -p $qTripleDir;
targetDir="$qTripleDir/$dataset"
echo "Storing to $targetDir"
rm -rf $targetDir;
mkdir $targetDir;

echo "getting number of triples used as input (for validation purposes)"
numRequiredTriples=`hadoop fs -cat $dataset/evaluation/qTriples | wc -l`

hadoopLs "$dataset/evaluation/qTripleWeights";
for dir in "${hadoopLs[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
		continue
	fi
	dirBasename=`basename $dir`
	hadoop fs -cat $dir/* >> $targetDir/$dirBasename;
	#fetchedTriples=`wc -l $targetDir/$dirBasename`;
	fetchedTriples=`wc -l $targetDir/$dirBasename | cut -f1 -d' '`
	#echo $fetchedTriples;
	#echo $numRequiredTriples;
	if [ "$fetchedTriples" -ne "$numRequiredTriples" ]; then
		echo "IMPORTANT!!!!!!!!!!!!!!"
		echo "number of fetched triples for $dirBasename does not match the triples we presented as input!!!"
		echo "input: $numRequiredTriples";
		echo "number of fetched triples: $fetchedTriples";
	fi
done;

echo "also fetching our randomly weighted qtriples"
rm -f /var/scratch/lrd900/randomlyWeightedQtriples/$dataset;
hadoop fs -cat $dataset/evaluation/randomlyWeightedQtriples* >> /var/scratch/lrd900/randomlyWeightedQtriples/$dataset;


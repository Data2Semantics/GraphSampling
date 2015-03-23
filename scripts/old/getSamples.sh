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
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;

#rewriteMethods=(spo-spo.py)
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset). (second arg is number of runs)"
	exit;
fi
dataset=$1
numRuns=$2
samples=(0.2 0.5)
if [ -z "$numRuns" ];then
	numRuns=10
fi
for (( run=1; run<=$numRuns; run++ )); do
	for sample in "${samples[@]}"; do
		outputPath="$dataset/roundtrip/"
		outputFile="$dataset"
		outputFile+="_"
		outputFile+="sample-$run"
		outputFile+="_"
		outputFile+="$sample.nt"
		outputPath+="$outputFile"
		
		pig $PIG_SCRIPTS/stats/sample.py $dataset/$dataset.nt $outputPath $sample;
		
		localTargetDir="load/subgraphs/$outputFile"
		if [ ! -d "$localTargetDir" ]; then
                echo "dir $localTargetDir does not exist. making"
                mkdir $localTargetDir
        fi
		hadoop fs -cat $outputPath/part* > $localTargetDir/$outputFile;
		putDirInVirtuoso.sh $localTargetDir;
	done
done





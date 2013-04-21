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
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1
pigAnalysisDir="$HOME/pigAnalysis/analysis"
analysisMethods=(pageRank.py indegree.py outdegree.py)


hadoopLs "$dataset/rewrite";
for dir in "${hadoopLs[@]}"; do
	echo "running analysis for $dir"
	for analysisMethod in "${analysisMethods[@]}"; do
		echo "running $analysisMethod"
		pig $pigAnalysisDir/$analysisMethod $dir;
	done
done;
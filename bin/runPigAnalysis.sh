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
pattern=""
if [ -n "$2" ]; then
	pattern="$2"
fi
pigAnalysisDir="$HOME/pigAnalysis/analysis"
analysisMethods=(pageRank.py indegree.py outdegree.py)


hadoopLs "$dataset/rewrite/$pattern";
for dir in "${hadoopLs[@]}"; do
	echo "running analysis for $dir"
	for analysisMethod in "${analysisMethods[@]}"; do
		echo "running $analysisMethod"
		if [ $analysisMethod == "pageRank.py" ]; then
			echo "cleaning all previously ran pagerank files in tmp dir"
			hadoop fs -rmr $dataset/tmp*
			
			echo "preprocessing pagerank"
			pig $pigAnalysisDir/pageRankPreProcess.py $dir
			preProcessedPagerankFile=$dir
			preProcessedPagerankFile+="_"
			preProcessedPagerankFile+="directed"
			preProcessedPagerankFile+="_"
			preProcessedPagerankFile+="pagerank"
			echo "running pagerank"
			pig $pigAnalysisDir/$analysisMethod $dataset/tmp/$preProcessedPagerankFile;
			
			echo "postprocessing pagerank"
			hadoopPagerankLs $dataset/tmp/pagerank_data_*;
			maxPagerankIteration=-1
			pagerankOutputFile=""
			for pagerankTmpDir in "${hadoopPagerankLs[@]}"; do
				if [ ${pagerankTmpDir:-1} > "maxPagerankIteration" ]; then
					maxPagerankIteration=${pagerankTmpDir:-1}
					pagerankOutputFile="$pagerankTmpDir"
				fi
			done
			if [ -n $pagerankOutputFile ];then
				pig $pigAnalysisDir/pageRankPostProcess $dataset/tmp/$pagerankOutputFile;
			else
				echo "Something wrong with pagerank? couldnt find any tmp pagerank dirs"
			fi 
		else 
			pig $pigAnalysisDir/$analysisMethod $dir;
		fi
		
	done
done;
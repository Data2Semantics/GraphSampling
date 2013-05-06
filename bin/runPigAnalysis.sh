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
pattern="*"
if [ -n "$2" ]; then
	pattern="$2"
fi
pigAnalysisDir="$HOME/pigAnalysis/analysis"
analysisMethods=(indegree.py outdegree.py)
#analysisMethods=(pageRank.py)

hadoopLs "$dataset/rewrite";
for dir in "${hadoopLs[@]}"; do
	if [[ ! "$dir" == $pattern ]]; then
		continue
	fi
	echo "running analysis for $dir"
	for analysisMethod in "${analysisMethods[@]}"; do
		echo "running $analysisMethod"
		if [ $analysisMethod == "pageRank.py" ]; then
			echo "cleaning all previously ran pagerank files in tmp dir"
			#hadoop fs -rmr $dataset/tmp/*
			
			echo "preprocessing pagerank"
			pig $pigAnalysisDir/pageRankPreProcess.py $dir
			preProcessedPagerankFile=`basename $dir`
			preProcessedPagerankFile+="_"
			preProcessedPagerankFile+="directed"
			preProcessedPagerankFile+="_"
			preProcessedPagerankFile+="pagerank"
			echo "running pagerank"
			pig $pigAnalysisDir/$analysisMethod $dataset/tmp/$preProcessedPagerankFile;
			
			echo "postprocessing pagerank"
			prLsPattern="*$dataset/tmp/$preProcessedPagerankFile"
			prLsPattern+="pagerank_data_*"
			hadoopPagerankLs $dataset/tmp;
			maxPagerankIteration=-1
			pagerankOutputFile=""
			for pagerankTmpDir in "${hadoopPagerankLs[@]}"; do
				if [[ "$pagerankTmpDir" == $prLsPattern ]]; then
					if [ ${pagerankTmpDir:-1} > "maxPagerankIteration" ]; then
						maxPagerankIteration=${pagerankTmpDir:-1}
						pagerankOutputFile="$pagerankTmpDir"
					fi
				fi
			done
			if [ -n $pagerankOutputFile ];then
				echo " pig $pigAnalysisDir/pageRankPostProcess.py $pagerankOutputFile"
				pig $pigAnalysisDir/pageRankPostProcess.py $pagerankOutputFile;
			else
				echo "Something wrong with pagerank? couldnt find any tmp pagerank dirs"
			fi 
		else 
			echo "pig $pigAnalysisDir/$analysisMethod $dir"
			pig $pigAnalysisDir/$analysisMethod $dir;
		fi
		
	done
done;

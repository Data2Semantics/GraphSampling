#!/bin/bash
function hadoopLs {
	hadoopLs=()
	dirListing=`hadoop fs -ls $1`;
	for word in ${dirListing} ; do
 		if [[ $word =~ ^/ ]];then 
	    	hadoopLs+=(${word})
	    fi
	done
}  

if [ -z "$1" ];then
        echo "at least 1 argument required (rewrite methods as directories to get query triples stats for)"
        exit;
fi
dataset=""
aggregateMethods=(min max avg)
for dir in "$@"; do
	dirBasename=`basename $dir`
	IFS=_ read -a delimited <<< "$dirBasename"
	dataset=${delimited[0]}
	hadoopLs $dataset/queryStatsInput/
	if [ ${#hadoopLs[@]} == "0" ]; then
        	echo "Did not find query files to get stats for. ($dataset/queryStatsInput/)"
        	exit;
	fi
	analysisFiles=`find $dir/output/*`
    while read -r analysisFile; do
	for aggregateMethod in "${aggregateMethods[@]}"; do
	    	basenameAnalysisFile=`basename $analysisFile`;
    		inputHadoopFile="$dirBasename"
    		inputHadoopFile+="_"
    		inputHadoopFile+="$basenameAnalysisFile"
		inputHadoopFile+="_"
		inputHadoopFile+="$aggregateMethod"
		echo "running pig to get weights for query triples $inputHadoopFile"
		for queryFile in "${hadoopLs[@]}"; do
			if [ ${queryFile: -3} == ".nt" ]; then
				pig pigAnalysis/stats/getQueryTripleWeights.py $dataset/roundtrip/$inputHadoopFile $queryFile;
				queryFileBasename=`basename $queryFile`
				IFS=_ read -a delimited <<< "$queryFileBasename"
        			queryId=${delimited[1]}
				resultFilename="$inputHadoopFile"
				resultFilename+="_"
				resultFilename+="$queryId"
				resultFilename=${resultFilename%.*} #remove .nt extension
				resultsPath="$dataset/queryStats/$resultFilename"
				processQueryTripleStats.sh $resultsPath;
			fi
		done
	done
   done <<< "$analysisFiles"
done


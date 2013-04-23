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
        echo "at least 1 argument required (rewrite methods as directories to get query triples stats for)"
        exit;
fi

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
    	basenameAnalysisFile=`basename $analysisFile`;
    	inputHadoopFile="$dirBasename"
    	inputHadoopFile+="_"
    	inputHadoopFile+="$basenameAnalysisFile"
		echo "running pig to get weights for query triples $inputHadoopFile"
		for queryFile in "${hadoopLs[@]}"; do
			if [ ${file: -3} == ".nt" ]; then
				pig pigAnalysis/stats/getQueryTripleWeights.py $dataset/roundtrip/$inputHadoopFile $queryFile;
			fi
		done
    done <<< "$analysisFiles"
done

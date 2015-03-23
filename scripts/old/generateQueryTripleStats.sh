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

[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;

if [ -z "$1" ];then
        echo "at least 1 argument required (rewrite methods as directories to get query triples stats for). as optional first argument -skipPig. as optional second argument: -queryPattern <pattern>"
        exit;
fi
skipPig=0;
queryPattern=0;
skipAggregateFor="so-so"
for arg in "$@"; do
        if [ "$arg" == "-skipPig" ]; then
		skipPig=1;
                shift;
                continue;
        fi
        if [ "$arg" == "-queryPattern" ]; then
                echo "query pattern detected!"
                queryPattern=1;
                shift;
                continue;
        fi
        if [ $queryPattern == 1 ]; then
                queryPattern="$arg";
                echo "query pattern is: $queryPattern"
                shift;
        fi
done
if [ ${#queryPattern} == 1 ]; then
	queryPattern=""
fi
dataset=""
#aggregateMethods=(min max avg)
aggregateMethods=(max)
for dir in "$@"; do
	dirBasename=`basename $dir`
	IFS=_ read -a delimited <<< "$dirBasename"
	dataset=${delimited[0]}
	hadoopLs $dataset/queryStatsInput/$queryPattern
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
		if [[ "$inputHadoopFile" =~ "$skipAggregateFor" ]]; then  
			echo "dont append aggregator thing to input hadoop string"
		else
			inputHadoopFile+="_"
	                inputHadoopFile+="$aggregateMethod"
		fi
		echo "running pig to get weights for query triples $inputHadoopFile"
		for queryFile in "${hadoopLs[@]}"; do
			if [ ${queryFile: -3} == ".nt" ]; then
				if [ $skipPig == 0 ]; then
					pig $PIG_SCRIPTS/stats/getQueryTripleWeights.py $dataset/roundtrip/$inputHadoopFile $queryFile;
				fi
				queryFileBasename=`basename $queryFile`
				#IFS=_ read -a delimited <<< "$queryFileBasename"
        			#queryId=${delimited[1]}
				resultFilename="$inputHadoopFile"
				resultFilename+="_"
				resultFilename+="$queryFileBasename"
				resultFilename=${resultFilename%.*} #remove .nt extension
				resultsPath="$dataset/queryStats/$resultFilename"
				processQueryTripleStats.sh $resultsPath;
			fi
		done
	done
   done <<< "$analysisFiles"
done


#!/bin/bash

if [ -z "$1" ];then
	echo "at least 1 argument required (the analysis file). Optional arg: skip pig script invocation"
	exit;
fi
#last one is strange: it's actually: half the graph, and only retrieve weights...

#topKVariants=(0.2 0.5 1000n "1w")
skipPig=$2
topKVariants=(0.5 "1w")
#topKVariants=("1w")
aggregateMethods=(min max avg)
skipAggregateFor="so-so"
#topKVariants=(0.5)
analysisFile=$(readlink -f $1)
pigRoundtripDir="$HOME/pigAnalysis/roundtrip"
analysisBasename=`basename $analysisFile`;
rewriteDir1=`dirname $analysisFile`;
rewriteDir=`dirname $rewriteDir1`;#quick and ugly
rewriteBasename=`basename $rewriteDir`
IFS=_ read -a delimited <<< "$rewriteBasename"
dataset=${delimited[0]}
hadoopAnalysisDir="$dataset/analysis/"
hadoopRoundtripDir="$dataset/roundtrip/"
targetFilename=$rewriteBasename
targetFilename+="_"
targetFilename+=$analysisBasename

localSubgraphDir="$HOME/load/subgraphs/"
statsDir="$HOME/stats/top1000/triples/"
tripleWeightsDir="$HOME/stats/tripleWeights"

tmpDir="$HOME/tmp"
for aggregateMethod in "${aggregateMethods[@]}"; do
	if [[ "$targetFilename+=" =~ "$skipAggregateFor" ]]; then  
		hadoopRoundtripFile="$hadoopRoundtripDir/$targetFilename"
		aggregateMethod=""
	else 
		hadoopRoundtripFile="$hadoopRoundtripDir/$targetFilename"
		hadoopRoundtripFile+="_"
		hadoopRoundtripFile+="$aggregateMethod"
	fi
	
	for topK in "${topKVariants[@]}"; do
		echo "selecting top-k $topK"
		echo "pig $pigRoundtripDir/selectMaxTopK.py $hadoopRoundtripFile $topK;"
		if [ -z $skipPig ]; then 
			pig $pigRoundtripDir/selectMaxTopK.py $hadoopRoundtripFile $topK;
		fi
		
	
		echo "catting results locally"
		
		topKFile="$targetFilename"

		if [ -n "$aggregateMethod" ]; then
			topKFile+="_"
			topKFile+="$aggregateMethod";
		fi
		topKFile+="_"
		topKFile+="max$topK.nt"
		
		tmpFile="$tmpDir/$targetFilename"
		if [ -n "$aggregateMethod" ]; then
			tmpFile+="_"
			tmpFile+="$aggregateMethod";
		fi
		tmpFile+="_"
		tmpFile+="$topK.tmp"
		hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $tmpFile;
		
		
		if [[ $topK =~ n$ ]];then
			targetTopKFilename="$targetFilename"
			if [ -n "$aggregateMethod" ]; then
				targetTopKFilename+="_"
				targetTopKFilename+="$aggregateMethod";
			fi
			targetTopKFilename+="_"
			targetTopKFilename+="$topK.nt"
			mv $tmpFile $statsDir/$targetTopKFilename;
		elif [[ $topK =~ w$ ]];then
			targetTopKFilename="$targetFilename"
			if [ -n "$aggregateMethod" ]; then
				targetTopKFilename+="_"
				targetTopKFilename+="$aggregateMethod";
			fi
			targetTopKFilename+="_"
			targetTopKFilename+="$topK.nt"
			mv $tmpFile $tripleWeightsDir/$targetTopKFilename;
			echo "just catted a file with just the triple weights. Now plot them"
			getTripleStatsForFile.sh $tripleWeightsDir/$targetTopKFilename;
		else
			newFileSize=`cat $tmpFile | wc -l`;
            origFileSize=`hadoop fs -cat $hadoopRoundtripFile/part* | wc -l`
            relSize=$(echo "($newFileSize/$origFileSize) * 100" | bc -l)
            relSize=`printf %.0f $relSize`
            if [ $relSize == 0 ]; then
                    echo "Relative size is 0?? $topKFile"
                    continue
            fi
            topKPercentage=$(echo "$topK * 100" | bc -l)
            topKPercentage=`printf %.0f $topKPercentage`
			if [ "$topKPercentage" -gt "100" ]; then
				echo "WRONG CALCULATION OF PERCENTAGE!!"
				echo "precentage: $topKPercentage"
				echo "new file size: $newFileSize"
				echo "orig file size: $origFileSize"
				exit;
			fi
		if [ ${#topKPercentage} == 1 ]; then
        		topKPercentage="0$topKPercentage"
		fi
			targetTopKFilename="$targetFilename"
			if [ -n "$aggregateMethod" ]; then
				targetTopKFilename+="_"
				targetTopKFilename+="$aggregateMethod";
			fi
			targetTopKFilename+="_"
			targetTopKFilename+="max-$topKPercentage-$relSize.nt"
			
			localTargetDir="$localSubgraphDir/$targetTopKFilename";
			localTargetFile="$localTargetDir/$targetTopKFilename";
			if [ ! -d $localTargetDir ];then
				mkdir $localTargetDir;
			fi
			mv $tmpFile $localTargetFile;
			putDirInVirtuoso.sh $localTargetDir;
		fi
	done
	checkpoint.sh;
done;



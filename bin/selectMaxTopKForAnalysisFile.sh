#!/bin/bash

if [ -z "$1" ];then
	echo "at least 1 argument required (the analysis file)"
	exit;
fi
#last one is strange: it's actually: half the graph, and only retrieve weights...
topKVariants=(0.2 0.5 1000n "0.5w")
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

hadoopRoundtripFile="$hadoopRoundtripDir/$targetFilename"
for topK in "${topKVariants[@]}"; do
	echo "selecting top-k $topK"
	pig $pigRoundtripDir/selectMaxTopK.py $hadoopRoundtripFile $topK;
	

	echo "catting results locally"
	
	topKFile="$targetFilename"
	topKFile+="_"
	topKFile+="max$topK.nt"
	

	tmpFile="$tmpDir/$targetFilename.tmp"
        hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $tmpFile;
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
	targetTopKFilename="$targetFilename"
	targetTopKFilename+="_"
	targetTopKFilename+="max-$topKPercentage-$relSize.nt"
	if [[ $topK =~ n$ ]];then
		#hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $statsDir/$topKFile;
		mv $tmpFile $statsDir/$targetTopKFilename;
	elif [[ $topK =~ w$ ]];then
		#hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $tripleWeightsDir/$topKFile;
		mv $tmpFile $tripleWeightsDir/$targetTopKFilename;
		echo "just catted a file with just the triple weights. Now plot them"
		getTripleStatsForFile.sh $tripleWeightsDir/$targetTopKFilename;
	else
		localTargetDir="$localSubgraphDir/$targetTopKFilename";
		localTargetFile="$localTargetDir/$targetTopKFilename";
		if [ ! -d $localTargetDir ];then
			mkdir $localTargetDir;
		fi
		mv $tmpFile $localTargetFile;
		#hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $localTargetFile;
		putDirInVirtuoso.sh $localTargetDir;
	fi
done
checkpoint.sh;



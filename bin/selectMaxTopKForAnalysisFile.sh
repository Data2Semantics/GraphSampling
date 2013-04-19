#!/bin/bash

if [ -z "$1" ];then
	echo "at least 1 argument required (the analysis file)"
	exit;
fi
#last one is strange: it's actually: half the graph, and only retrieve weights...
topKVariants=(0.2 0.5 1000n "0.5w")
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

	sizeFile="$hadoopRoundtripFile"
	sizeFile+="_"
	sizeFile+="size"
	origFileSize=`hadoop fs -cat $sizeFile/part*`
	
	echo "catting results locally"
	
	tmpFile="$tmpDir/$targetFilename.tmp"
	hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $tmpFile;
	newFileSize=`wc -l $tmpFile`;
	
	
	
	
	exit;
	topKFile="$targetFilename"
	topKFile+="_"
	topKFile+="$topK.nt"
	
	if [[ $topK =~ n$ ]];then
		hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $statsDir/$topKFile;
	elif [[ $topK =~ w$ ]];then
		hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $tripleWeightsDir/$topKFile;
		echo "just catted a file with just the triple weights. Now plot them"
		getTripleStatsForFile.sh $tripleWeightsDir/$topKFile;
	else
		localTargetDir="$localSubgraphDir/$topKFile";
		localTargetFile="$localTargetDir/$topKFile";
		if [ ! -d $localTargetDir ];then
			mkdir $localTargetDir;
		fi

		hadoop fs -cat $hadoopRoundtripDir/$topKFile/part* > $localTargetFile;
		putDirInVirtuoso.sh $localTargetDir;
	fi
done
checkpoint.sh;



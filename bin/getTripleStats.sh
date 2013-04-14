#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi

dataset=$1
rDir="${HOME}/rProject"
tripleWeightsDir="$HOME/stats/tripleWeights"
plotsDir="$HOME/stats/plots/tripleWeightDist"
scriptsFile="$HOME/rProject/scripts/getTripleStats.R"

cmd="find $tripleWeightsDir/$dataset_* -maxdepth 1 -type f"
tripleWeightFiles=`eval $cmd`
outputRunScript="$HOME/.rRunScript.R"
while read -r tripleWeightFile; do
	getTripleStatsForFile.sh $tripleWeightFile;
done <<< "$tripleWeightFiles"

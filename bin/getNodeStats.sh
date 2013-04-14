#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi

scriptsFile="$HOME/rProject/scripts/getNodeStats.R"
dataset=$1
rDir="$HOME/rProject"
top100Dir="$HOME/stats/100n/nodes"
plotsDir="$HOME//stats/plots/nodeWeightDist"
cmd="find $rDir -maxdepth 1 -type d -regex '^$rDir/$dataset"
cmd+="_"
cmd+=".*'"
rewriteDirs=`eval $cmd`
outputRunScript="$HOME/.rRunScript.R"
while read -r rewriteDir; do
	rewriteMethod=`basename $rewriteDir`
        echo "getting stats for rewrite method $rewriteDir";
        analysisFiles=`find $rewriteDir/output/*`
        while read -r analysisFile; do
			echo "getting node statistics for $rewriteMethod (as daemon)"
			targetFile="$rewriteMethod"
			targetFile+="_"
			targetFile+=`basename $analysisFile`
			runScript="$outputRunScript"
			runScript+=`date +%s%N`
	        echo "inputFilename <- \"$analysisFile\"" > $runScript;
	        echo "outputTop100 <- \"$top100Dir/$targetFile\"" >> $runScript;
	        echo "outputPdf <- \"$plotsDir/$targetFile.pdf\"" >> $runScript;
	        cat $scriptsFile >> $runScript;
	        R -f $runScript &
        done <<< "$analysisFiles"
done <<< "$rewriteDirs"

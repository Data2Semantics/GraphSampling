#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (dirs containing dataset)"
        exit;
fi

scriptsFile="$HOME/rProject/scripts/getNodeStats.R"
rDir="$HOME/rProject"
top1000Dir="$HOME/stats/top1000/nodes"
plotsDir="$HOME/stats/plots/nodeWeightDist"
outputRunScript="$HOME/tmp/rRunScript.R"
logFile="$HOME/logs/getNodeStats_"
logFile+=`date +"%Y%m%d"`
logFile+=".log"
for rewriteDir in "$@"; do
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
        echo "outputTop1000 <- \"$top1000Dir/$targetFile\"" >> $runScript;
        echo "outputPdf <- \"$plotsDir/$targetFile.pdf\"" >> $runScript;
        cat $scriptsFile >> $runScript;
        R -f $runScript >> $logFile &
    done <<< "$analysisFiles"
done

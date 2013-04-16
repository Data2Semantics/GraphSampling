#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (the file containing the weights)"
        exit;
fi

rDir="${HOME}/rProject"
plotsDir="$HOME/stats/plots/tripleWeightDist"
scriptsFile="$HOME/rProject/scripts/getTripleStats.R"
outputRunScript="$HOME/temp/rRunScript.R"
outputRunScript+=`date +%s%N`
logFile="$HOME/logs/getTripleStats_"
logFile+=`date +"%Y%m%d"`
logFile+=".log"

for tripleWeightsFile in "$@"; do
	basename=`basename $tripleWeightsFile`
	targetFile="$plotsDir/$basename.pdf"
	echo "filename <- \"$tripleWeightsFile\"" > $outputRunScript;
	echo "outputPdf <- \"$targetFile\"" >> $outputRunScript;
	cat $scriptsFile >> $outputRunScript;
	R -f $outputRunScript > $logFile &
done




#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (the file containing the weights)"
        exit;
fi

rDir="${HOME}/rProject"
plotsDir="$HOME/stats/plots/tripleWeightDist"
scriptsFile="$HOME/rProject/scripts/getTripleStats.R"
outputRunScript="$HOME/tmp/rRunScript_$RANDOM.R"
logFile="$HOME/logs/getTripleStats_"
logFile+=`date +"%Y%m%d"`

for tripleWeightsFile in "$@"; do
	if [ ! -f $tripleWeightsFile ]; then
		echo "File $tripleWeightsFile not found"
		exit;
	fi
	absFile=$(readlink -f $tripleWeightsFile)
	basename=`basename $absFile`
	echo "Plotting triple weights distribution for $basename"
	targetFile="$plotsDir/$basename.pdf"
	echo "filename <- \"$absFile\"" > $outputRunScript;
	echo "outputPdf <- \"$targetFile\"" >> $outputRunScript;
	cat $scriptsFile >> $outputRunScript;
	R -f $outputRunScript >> $logFile.log 2>> $logFile.err &
done




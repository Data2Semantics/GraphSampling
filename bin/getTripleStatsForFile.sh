#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (the file containing the weights)"
        exit;
fi

tripleWeightsFile=$1
rDir="${HOME}/rProject"
plotsDir="$HOME/stats/plots/tripleWeightDist"
scriptsFile="$HOME/rProject/scripts/getTripleStats.R"
outputRunScript="$HOME/.rRunScript.R"
outputRunScript+=`date +%s%N`
basename=`basename $tripleWeightsFile`
targetFile="$plotsDir/$basename.pdf"
echo "filename <- \"$tripleWeightsFile\"" > $outputRunScript;
echo "outputPdf <- \"$targetFile\"" >> $outputRunScript;
cat $scriptsFile >> $outputRunScript;
R -f $outputRunScript &




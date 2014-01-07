#!/bin/bash

if [ -z "$1" ];then
        echo "1 argument required (dataset)"
        exit;
fi
dataset=$1


rewrite.sh $dataset;
createDict.sh $dataset;
runAnalysis.sh $dataset;
runGiraphAnalysisToString.sh $dataset;
roundtrip.sh $dataset;
fetchWeightDistribution.sh $dataset;
validateHash.sh $dataset;
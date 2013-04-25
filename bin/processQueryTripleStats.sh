#!/bin/bash
function hadoopLs {
	hadoopLs=()
	echo "hadoop fs -ls $1";
	dirListing=`hadoop fs -ls $1`;
	for word in ${dirListing} ; do
 		if [[ $word =~ ^/ ]];then 
	    	hadoopLs+=(${word})
	    fi
	done
}  

if [ -z "$1" ];then
        echo "at least 1 argument required (the hadoop query stats file)"
        exit;
fi
queryStatsFile=$1

IFS=/ read -a delimited <<< "$queryStatsFile"
dataset=${delimited[0]}


resultsDir="$HOME/stats/queryTripleWeights/$dataset/"
if [ ! -d $resultsDir ]; then
	echo "$resultsDir does not exist locally. creating it"
	mkdir $resultsDir;
fi
echo "catting query stats locally"
queryFileBasename=`basename $queryStatsFile`
hadoop fs -cat $queryStatsFile/part* > $resultsDir/$queryFileBasename

filesize=$(stat -c%s "$resultsDir/$queryFileBasename")
if [ $filesize == 0 ]; then
	echo "did not retrieve any results from hadoop for $queryFileBasename. exiting"
	exit
fi

echo "plotting results"

scriptsFile="$HOME/rProject/scripts/getQueryTripleStats.R"

# tripleWeightsFile <- 'tripleWeights/df_s-o-litAsNode_unweighted_directed_betweenness-4_avg_1w.nt'
# queryTriplesFile <- 'queryTripleWeights/df/df_s-o-litAsNode_unweighted_directed_betweenness-4_avg_q81.weights'
# pdffile <- paste('plots/queryScores/df_s-o-litAsNode_unweighted_directed_betweenness-4_avg_q81.weights')
workingDir="$HOME/stats"

tripleWeightsFile="$HOME/stats/tripleWeights/$queryFileBasename"
#IFS=_ read -a delimited <<< "$tripleWeightsFile"
#dataset=${delimited[0]}
tripleWeightsFile=`echo $tripleWeightsFile | sed 's/\(.*\)_.*/\1/g'`
tripleWeightsFile+="_"
tripleWeightsFile+="1w.nt"
queryTriplesFile="$resultsDir/$queryFileBasename"
plotsDir="$HOME/stats/plots/queryScores/$dataset"

pdffile="$plotsDir/$queryFileBasename.pdf"
outputRunScript="$HOME/tmp/rRunScript.R$RANDOM"
logFile="$HOME/logs/getQueryTripleStats_"
logFile+=`date +"%Y%m%d"`
logFile+=".log"

echo "tripleWeightsFile <- '$tripleWeightsFile'" > $outputRunScript;
echo "queryTriplesFile <- '$queryTriplesFile'" >> $outputRunScript;
echo "pdffile <- '$pdffile'" >> $outputRunScript;
cat $scriptsFile >> $outputRunScript;
 R -f $outputRunScript >> $logFile &

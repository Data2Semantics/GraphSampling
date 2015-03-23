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
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;

#rewriteMethods=(spo-spo.py)
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset)"
	exit;
fi
dataset=$1
#samples=(0.2 0.5)
samples=(0.5)
for sample in "${samples[@]}"; do
	outputPath="$dataset/roundtrip/"
	outputFile="$dataset"
	outputFile+="_"
	outputFile+="freqBaseline"
	outputFile+="_"
	outputFile+="$sample.nt"
	outputPath+="$outputFile"
	
	#pig $PIG_SCRIPTS/stats/freqBaseline.py $dataset/$dataset.nt $outputPath $sample;
	echo "hadoop fs -cat $outputPath/part*"
	hadoop fs -cat $outputPath/part* > tmp/$outputFile;
	newFileSize=`cat tmp/$outputFile | wc -l`
	origFileSize=`cat load/$dataset/* | wc -l`
        relSize=$(echo "($newFileSize/$origFileSize) * 100" | bc -l)
        relSize=`printf %.0f $relSize`
        if [ $relSize == 0 ]; then
                echo "Relative size is 0?? $topKFile"
		echo "orig file size: $origFileSize"
		echo "catted file size: $newFileSize"
                continue
        fi
        if [ ${#relSize} == 1 ]; then
            relSize="0$relSize"
        fi
        topKPercentage=$(echo "$sample * 100" | bc -l)
        topKPercentage=`printf %.0f $topKPercentage`
        if [ "$topKPercentage" -gt "100" ]; then
            echo "WRONG CALCULATION OF PERCENTAGE!!"
            echo "precentage: $topKPercentage"
            echo "new file size: $newFileSize"
            echo "orig file size: $origFileSize"
            exit;
        fi
	
	localTargetDir="load/subgraphs/$outputFile"
	newOutputFile="$dataset"
        newOutputFile+="_"
        newOutputFile+="freqBaseline"
        newOutputFile+="_"
        newOutputFile+="max-$topKPercentage-$relSize.nt"
	if [ ! -d "load/subgraphs/$newOutputFile" ]; then
               echo "dir load/subgraphs/$newOutputFile does not exist. making"
               mkdir load/subgraphs/$newOutputFile
        fi
	mv tmp/$outputFile load/subgraphs/$newOutputFile

	#hadoop fs -cat $outputPath/part* > $localTargetDir/$outputFile;
	
	putDirInVirtuoso.sh load/subgraphs/$newOutputFile;
done





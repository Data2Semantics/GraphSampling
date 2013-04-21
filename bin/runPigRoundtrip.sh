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
        echo "at least 1 argument required (dataset)"
        exit;
fi


dataset=$1
analysisDir="$dataset/analysis"

hadoopLs "$analysisDir";
for dir in "${hadoopLs[@]}"; do
	runPigRoundtripForAnalysisFile.sh $dir
done;

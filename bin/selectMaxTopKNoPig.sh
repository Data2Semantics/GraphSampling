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
        echo "at least 1 argument required (directory path for roundtrips)"
        exit;
fi


for dir in "$@"; do
        echo "selecting topk for $dir";
        analysisFiles=`find $dir/output/*`
        while read -r analysisFile; do
                selectMaxTopKForAnalysisFile.sh $analysisFile noPig; 
        done <<< "$analysisFiles"
done


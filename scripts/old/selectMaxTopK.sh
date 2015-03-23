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
        echo "at least 1 argument required (directory path for roundtrips). optional (first) argument: -skipPig"
        exit;
fi
skipPig=0;
if [ "$1" == "-skipPig" ]; then
    echo "skipping pig part"
	skipPig=1;
        shift;
fi


for dir in "$@"; do
        echo "selecting topk for $dir";
        analysisFiles=`find $dir/output/*`
        while read -r analysisFile; do
		if [ $skipPig == 1 ]; then
			selectMaxTopKForAnalysisFile.sh $analysisFile skipPig; 
		else 
	                selectMaxTopKForAnalysisFile.sh $analysisFile; 
		fi
        done <<< "$analysisFiles"
done


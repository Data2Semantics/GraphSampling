#!/bin/bash


if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)."
        exit;
fi
dataset=$1
pattern="*"
if [ -n "$2" ]; then
	pattern="$2"
fi
hadoopDir="$dataset/evaluation/degreeDist"
distFile="degreeDist${dataset}";
echo "Storing to $distFile"
rm -f $distFile;


hadoop fs -cat $hadoopDir/* >> $distFile;



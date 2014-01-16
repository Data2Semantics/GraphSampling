#!/bin/bash

if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1

targetHdfsDir="$dataset/evaluation/qTriples"
hadoop fs -rm $targetHdfsDir

srcLocalFile="qTriples/$dataset"

hadoop fs -put $srcLocalFile $targetHdfsDir

echo "done"

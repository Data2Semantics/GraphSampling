#!/bin/bash
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset)"
	exit
fi

hadoop fs -mkdir $1;
hadoop fs -mkdir $1/rewrite;
hadoop fs -mkdir $1/analysis;
hadoop fs -mkdir $1/roundtrip;
hadoop fs -mkdir $1/stats;
hadoop fs -mkdir $1/tmp;
hadoop fs -mkdir $1/queryStats;
hadoop fs -mkdir $1/queryStatsInput;
hadoop fs -mkdir $1/evaluation;
hadoop fs -mkdir $1/evaluation/qTripleWeights;
hadoop fs -mkdir $1/evaluation/weightDistribution;
echo "done";

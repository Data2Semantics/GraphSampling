#!/bin/bash


if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1
localTargetDir="~/code/subgraphAnalysis/input/"
echo "fetching hdfs data for das4 master node"
ssh fs0.das4.cs.vu.nl fetchWeightDistribution.sh $dataset;
#rsync -avz fs0.das4.cs.vu.nl:qTripleWeights .
echo "rsyncing locally"
rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;


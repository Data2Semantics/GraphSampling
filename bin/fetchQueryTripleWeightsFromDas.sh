#!/bin/bash

if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1

echo "fetching hdfs data for das4 master node"
ssh fs0.das4.cs.vu.nl fetchQueryTripleWeights.sh $dataset;

localTargetDir="~/code/subgraphSelection/input/"
echo "rsyncing locally"
eval rsync -avz fs0.das4.cs.vu.nl:/var/scratch/lrd900/qTripleWeights $localTargetDir;
#rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;


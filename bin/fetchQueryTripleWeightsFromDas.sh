#!/bin/bash

if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1

echo "fetching hdfs data for das4 master node"
ssh fs0.das4.cs.vu.nl fetchQueryTripleWeights.sh $dataset;
host=`hostname -f`

localTargetDir="/virdir/Scratch/subgraphSelection/input/"
if [ "$host" == "note632" ];then
	localTargetDir="/home/lrd900/code/subgraphSelection/input/"
fi


echo "rsyncing locally"#also dels on the dest side
eval rsync -rtvz --del fs0.das4.cs.vu.nl:/var/scratch/lrd900/qTripleWeights $localTargetDir;
eval rsync -rtvz --del fs0.das4.cs.vu.nl:/var/scratch/lrd900/randomlyWeightedQtriples $localTargetDir;
#rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;


#!/bin/bash

if [ -z "$1" ];then
        echo "at least 1 arguments required: dataset"
        exit;
fi
dataset=$1
echo "compressing"
cd /home/lrd900/code/subgraphSelection;
qTripleDir="output/queryTriples/$dataset"

tar -zcvf output/queryTriples/$dataset.tar.gz $qTripleDir;
src="output/queryTriples/*.tar.gz"
target="hpc:/virdir/Scratch/subgraphSelection/output/queryTriples"
eval rsync -rtvz $src $target
#rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;

